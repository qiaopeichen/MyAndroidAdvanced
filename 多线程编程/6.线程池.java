/*
	Java 1.5中提供了Executor框架用于把任务的提交和执行解耦，任务的提交交给Runnable或Callable，而Executor框架用来处理任务。
	Executor框架中最核心的成员就是ThreadPoolExecutor，它是线程池的核心实现类。
*/

//可以通过ThreadPoolExecutor来创建一个线程池，ThreadPoolExecutor类一共有4个构造方法。其中拥有最多参数的构造方法如下所示：
public ThreadPoolExecutor(int corePoolSize, // 核心线程数
						int maximumPoolSize, // 线程池允许的最大线程数
						long keepAliveTime, // 非核心线程闲置的超时时间。超过这个时间则回收。
						TimeUnit unit, // keepAliveTime的时间单位
						BlockingQueue<Runnable> workQueue, // 任务队列，如果当前线程数大于corePoolSize，则将任务添加到此任务队列中。
						ThreadFactory threadFactory, // 线程工厂，一般情况下无需设置该参数
						RejectedExecutionHandler handler) { // 饱和策略，默认是AbordPolicy 表示无法处理新任务并抛出RejectedExecutionException异常
							...
						}

/*
	可以通过配置ThreadPoolExecutor的参数来创建不同类型的ThreadPoolExecutor，其中有4中线程池比较常用。
	它们分别是FixedThreadPool、CachedThreadPool、SingleThreadExecutor、ScheduledThreadPool。
	下面分别介绍这4中线程池。
*/
					
//FixedThreadPool是可重用固定线程数的线程池。在Executors类中提供了创建FixedThreadPool的方法，如下所示
public static ExecutorService newFixedThreadPool(int nThreads) {
	return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()); // 无界的阻塞队列
}

//CacheThreadPool是一个根据需要创建线程的线程池
public static ExecutorService newCachedThreadPool() {
	return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
}

//SingleThreadExecutor是一个使用单个工作线程的线程池
public static ExecutorService newSingleThreadExecutor() {
	return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));
}

//ScheduledThreadPool是一个能实现定时和周期性任务的线程池，它的创建源码如下所示：
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
	return new ScheduledThreadPoolExecutor(corePoolSize);
}

//ScheduledThreadPoolExecutor的构造方法如下所示
public ScheduledThreadPoolExecutor(int corePoolSize) {
	super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS, new DelayedWorkQueue());
}