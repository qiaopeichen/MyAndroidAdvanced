//1.继承Thread类，重写run()方法
public class TestThread extends Thread {
	public void run() {
		System.out.println("Hello World");
	}
	public static void main(String[] args) {
		Thread mThread = new TestThread();
		mThread.start();
	}
}

//2.实现Runnable接口，并实现该接口的run()方法
public class TestRunnable implements Runnable {
	public void run() {
		System.out.println("Hello World");
	}
}
public class TestRunnable {
	public static void main(String[] args) {
		TestRunnable mTestRunnable = new TestRunnable();
		Thread mThread = new Thread(mTestRunnable);
		mThread.start();
	}
}

//3.实现Callable接口，重写call()方法
/**
	1.Callable可以在任务结束后提供一个返回值，Runnable无法提供这个功能
	2.Callable中的call()方法可以抛出异常，而Runnable的run()方法不能抛出异常
	3.运行Callable可以拿到一个Future对象，Future对象表示异步运算的结果，它提供了检查计算是否完成的方法。
	  由于线程属于异步计算模型，因此无法从别的线程中得到函数的返回值，在这种情况下就可以使用Future来监视目标线程调用call()方法的情况。
	  但调用Future的get()方法以获取结果时，当前线程就会阻塞，直到call()方法返回结果。
*/
public class TestCallable {
	//创建线程类
	public static class MyTestCallable implements Callable {
		public String call() throws Exception {
			return "Hello World";
		}
	}
	
	public static void main(String[] args) {
		MyTestCallable mMyTestCallable = new MyTestCallable();
		ExecutorService mExecutorService = Executors.newSingleThreadPool();
		Future mfuture = mExecutorService.submit(mMyTestCallable);
		try {
			//等待线程结束，并返回结果
			System.out.println(mfuture.get());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//3种方式中，一般推荐使用Runnable接口的方式。
//原因：一个雷应该在其需要加强或者修改时才会被集成。因此如果没有必要重写Thread类的其他方法，那么在这种情况下最好用实现Runnable接口的方式。