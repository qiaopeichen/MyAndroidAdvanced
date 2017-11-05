//重入锁ReentrantLock是JavaSE 5.0引入的，表示该锁能够支持一个线程对资源的重复枷锁.
Lock mLock = new ReentrantLock();
mLock.lock();
try{
	...
}
finally{
	mLock.unlock();
}

/**临界区就是在同一时刻只能有一个任务访问的代码区，
	进入临界区时，却发现在某一个条件满足之后，他才能执行。
	这是可以使用一个条件对象来管理那些已经获得了一个锁但却不能做有用工作的线程。
	通过下面的例子来说明为何需要条件对象。
*/

//构造方法需要传入账户的数量和每个账户的账户金额
public class Alipay {
	private double[] accounts;
	private Lock alipaylock;
	public Alipay(int n, double money) {
		acounts = new double[n];
		alipaylock = new ReentrantLock();
		for (int i = 0; i < accounts.length; i++) {
			accounts[i] = money;
		}
	}
	
	//转账的方法
	/*
		转账方余额不足，如果有其他线程给转账方足够的钱，就可以转账成功了。
		但这个线程已经获取了锁，它具有排他星，别的线程无法进行存款操作。
		这就是我们需要引入条件对象的原因。
	*/
	public void transfer(int from, int to, int amount) {
		alipaylock.lock();
		try{
			while (accounts[from] < amount) {
				//wait
			}
		} finally{
			alipaylock.unlock();
		}
	}
}

/*
	一个锁对象拥有多个相关的条件对象，可以用newCondition方法获得一个条件对象，我们得到条件对象后调用await方法，当前线程就被阻塞了并放弃了锁。
	整理以上代码，加入条件对象，如下所示：
*/
public class Alipay {
	private double[] accounts;
	private Lock alipaylock;
	private Condition condition;
	public Alipay(int n, double money) {
		accounts = new double[n];
		alipaylock = new ReentrantLock();
		//得到条件对象
		condition = alipaylock.newCondition();
		for (int i = 0; i < accounts.length; i++) {
			accounts[i] = money;
		}
	}
	public void transfer(int from, int to, int amount) throws InterruptedException {
		alipaylock.lock();
		try {
			while (accounts[from] < amount) {
				//阻塞当前线程，并放弃锁
				condition.await();
			}
			//转账的操作
			accounts[from] = accounts[from] - amount;
			accounts[to] = accounts[to] + amount;
			conditon.signalAll();
		} finally { 
			alipaylock.unlock();
		}
	}
}

/*同步方法
	Java找那个的每一个对象都有一个内部所。如果一个方法用synchronized关键字声明，那么对象的锁将保护整个方法。
	也就是说，要调用该方法，线程必须获得内部的对象锁。也就是如下代码
*/
public synchronized void method() {
	...
}
//等价于：
Lock mLock = new ReentrantLock();
public void method() {
	mLock.lock();
	try {
		...
	} finally {
		mLock.unlock();
	}
}

/*
	上面例子中的transfer方法也可以这样写：
	可以看到使用synchronized关键字来编写代码要简洁很多。
	必须要了解每一个对象有一个内部锁，并且该锁有一个内部条件。
	由该锁来管理那些试图进入synchronized方法的线程，由该锁的条件来管理那些调用wait的线程。
*/
public synchronized void transfer(int from, int to, int amount) throws InterruptedException {
	while (accounts[from] < amount) {
		wait();
	}
	//转账的操作
	accounts[from] = accounts[from] - amount;
	accounts[to] = accounts[to] + amount;
	notifyAll();
}

//同步代码块
/*
	上面说过每一个Java对象都有一个锁，线程可以调用同步方法来获得锁。
	还有另一种机制可以获得锁，那就是使用一个同步代码块，如下所示：
*/

//其获得了obj的锁，obj指的是一个对象。
synchronized(obj) {
}

//再来看看Alipay类，我们用同步代码块进行改写。

/*
	同步代码块是非常脆弱的，通常不推荐使用。
	一般实现同步最好用java.util.concurrent包下提供的类,比如阻塞队列。
	如果同步方法适合你的程序，尽量使用同步方法。
*/
public class Alipay {
	private double[] accounts;
	private Object lock = new Object();
	
	public Alipay(int n, double money) {
		accounts = new double[n];
		for (int i = 0; i < accounts.length; i++) {
			accounts[i] = money;
		}
	}
	
	public void transfer(int from, int to, int amount) {
		synchronized(lock) {
			//转账的操作
			accounts[from] = accounts[from] - amount;
			accounts[to] = accounts[to] + amount;
		}
	}
}










