//interrupt方法可以用来请求中断线程，将中断标识符置位true。
//测试线程是否被置位
while(!Thread.currentThread().isInterrupted()) {
	//do something
}

//如果线程处于阻塞状态，线程在检查中断标识位时如果发现中断标识位为true，则会在阻塞方法调用处抛出InterruptException异常，并在抛出异常前将标识位重新设置为false。
//不要在底层代码里捕获InterruptedException异常后不做处理，如下所示
void myTask() {
	...
	try{
		sleep(50)
	}catch(InterruptedException e) {
		
	}
	...
}

//两种合理的处理方式
/**1.在catch子句中，调用Thread.currentThread.interrupt()来设置中断状态（因为抛出异常后中断标识符会复位），
 *让外界通过判断Thread.currentThread().isInterrupted()来决定是否终止线程还是继续下去，应该这样做：
*/
void myTask() {
	...
	try{
		sleep(50)
	}catch(InterruptedException e){
		Thread.currentThread().interrupted();
	}
	...
}

//更好的做法是，直接抛出，这样调用者可以捕获这个异常
void myTask() throw InterruptedException {
	sleep(50)
}

//安全地终止线程

//使用中断来终止线程
public class StopThread {
	public static void main(String[] args) throws InterruptedException {
		MoonRunner runnable = new MoonRunner();
		Thread thread = new Thread(runnable, "MoonThread");
		thread.start();
		TimeUnit.MILLISECONDS.sleep(10);//1
		thread.interrupt();
	}
	
	public static class MoonRunner implements Runnable {
		private long i;
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				i++;
				System.out.println("i=" + i);
			}
			System.out.println("stop");
		}
	}
}

//采用boolean变量来控制是否需要停止线程
public class StopThread {
	public static void main(String[] args) throws InterruptedException {
		MoonRunner runnable = new MoonRunner();
		Thread thread = new Thread(runnbale, "MoonThread");
		thread.start();
		TimeUnit.MILLISECONDS.sleep(10);
		runnable.cancel();
	}
	
	public static class MoonRunner implements Runnable {
		private long i;
		private volatile boolean on = true;//1
		
		@Override
		public void run() {
			while(on) {
				i++;
				System.out.println("i=" + i);
			}
			System.out.println("stop");
		}
		public void cancel() {
			on = false;
		}
	}
}





