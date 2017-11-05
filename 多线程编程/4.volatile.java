/*
	原子性；对基本数据类型变量的读取和赋值操作是原子性操作，即这些操作是不可被中断的。
	可见性：指线程之间的可见性，一个线程修改的状态对另一个线程是可见的。
	有序性：Java内存模型中允许编译器和处理器对指令进行重排序，虽然重排序过程不会影响到单线程执行的正确性，但会影响到多线程并发执行的正确性。
	
	这时可以通过volatile来保证有序性。
	volatile不保证原子性，使用volatile必须具备以下两个条件：
	1.对变量的写操作不会依赖于当前值。
	2.该变量没有包含在具有其他变量的不变式中。
	
	使用volatile有很多种场景，这里介绍其中的两种：
*/

//1.状态标志
volatile boolean shutdownRequested;
...
public void shutdown() {
	shutdownRequested = true;
}
public void doWork() {
	while(!shutdownRequested) {
		...
	}
}

//2.双重检查模式(DCL) double check locking ?
public class Singleton {
	private volatile static Singleton instance = null;
	public static Singleton getInstance() {
		if (instance == null) {
			synchronized(this) {
				if (instance == null) {
					instance = new Singleton();
				}
			}
		}
		return instance;
	}
}

/*
	变量真正独立于其他变量和自己以前的值，在某些情况下可以使用volatile代替synchronized来简化代码。
	然而，使用volatile的代码往往比使用锁的代码更加容易出错。
*/