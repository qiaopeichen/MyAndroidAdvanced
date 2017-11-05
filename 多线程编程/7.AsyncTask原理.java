//Android提供了AsyncTask，使得异步任务实现起来更加简单，代码更简洁。

/*
	首先来看AsyncTask定义：
	@Params 参数类型
	@Progress 后台任务执行进度的类型
	@Result 返回结果的类型
	如果不需要某个参数，可以将其设置为void类型。
*/
public abstract class AsyncTask<Params, Progress, Result> {
	...
}
/*
	AsyncTask有4个核心方法
	onPreExecute()：在主线程中执行，一般在任务执行前做准备工作，比如对UI做一些标记。
	doInBackground(Params...params)：在线程池中执行。在onPreExecute方法执行后运行，用来执行较为耗时的操作。在执行过程中可以调用publishProgress(Progress... values)来更新进度。
	onProgressUpdate(Progress...values)：在主线程中执行。当调用publishProgress(Progress... values)时，此方法会将进度更新到UI组件上。
	onPostExecute(Result result)：在主线程中执行。当后台任务执行完成后，它会被执行。doInBackground方法得到的结果就是返回的result的值。此方法一般做任务执行后的收尾工作，比如更新UI和数据。
*/

//AsyncTask源码分析

/*
	Android3.0版本之前的AsyncTask的部分源码
	3.0版本之前的AsyncTask有一个缺点，就是线程池最大的线程数为128，加上阻塞队列的10个任务，所以AsycTask最多能同时容纳138个任务，
	当提交第139个任务时就会执行饱和策略，默认抛出RejectedExecutionException异常
*/
public abstract class AsyncTask<Params, Progress, Result> {
	private static final String LOG_TAG = "AsyncTask";
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 1;
	private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<Runnable>(10);
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);
		public Thread newThread(Runnable r) {
			return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
		}
	};
	private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sWorkQueue, sThreadFactory);
	...
}

/*
	Android7.0版本的AsyncTask
*/

//首先看AsyncTask的构造方法
public AsyncTask() {
	mWorker = new WorkerRunnable<Params, Result> { //1
		public Result call() throws Exception {
			mTaskInvoked.set(true);
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			Result result = doInBackground(mParams);
			Binder.flushPendingCommands();
			return postResult(result);
		}
	};
	
	mFuture = new FutureTask<Result>(mWorker) { //2
		@Override
		protected void done() {
			try {
				postResultIfNotInvoked(get());
			} catch (InterruptedException e) {
				android.util.Log.w(LOG_TAG, e);
			} catch (ExecutionException e) {
				throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
			} catch (CancellationException e) {
				postResultIfNotInvoked(null);
			}
		}
	};
}

//AsyncTask的execute方法
public final AsyncTask<Params, Progress, Result> execute(Params... params) {
	return executeOnExecutor(sDefaultExecutor, params);
}

//executeOnExecutor方法
public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
	if (mStatus != Status.PENDING) {
		switch (mStatus) {
			case RUNNING:
				throw new IllegalStateException("Cannot execute task:" + " the task is already running.");
			case FINISHED:
				throw new IllegalStateException("Cannot execute task:" + " the task has already been executed " + "(a task can be executed only once)");
		}
	}
	mStatus = Status.RUNNING;
	onPreExecute();
	mWorker.mParams = params; //1
	exec.execute(mFuture); // exec是传进来的参数，它是一个串行的线程池SerialExecutor
	return this;
}

//SerialExecutor代码
private static class SerialExecutor implements Executor {
	final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
	Runnable mActive;
	public synchronized void execute(final Runnable r) {
		mTask.offer(new Runnable() { //1
			public void run() {
				try {
					r.run(); //2
				} finally {
					scheduleNext();
				}
			}
		});
		if (mActive == null) {
			scheduleNext();
		}
	}
	protected synchronized void scheduleNext() {
		if ((mActive = mTask.poll()) != null) {
			THREAD_POOL_EXECUTOR.execute(mActive);
		}
	}
}








