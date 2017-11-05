/*
	常见的阻塞场景：
	1.当队列没有数据的情况下，消费者线程自动阻塞，直到有数据放入队列。
	2.当队列填满数据的情况下，生产者线程自动阻塞，直到队列中有空的位置。
	支持以上两种场景的队列被称为阻塞队列。
*/

//阻塞队列的实现原理
//ArrayBlockingQueue代码
public class ArrayBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
	private static final long serialVersionUID = -817911632652898426L;
	final Object[] items;
	int takeIndex; // 队首
	int putIndex; // 队尾
	int count; // 队列中元素的个数
	final ReentrantLock lock;
	private final Condition notEmpty;
	private final Condition notFull;
	...
}

//关键方法put
public void put (E e) throws InterruptedException {
	checkNotNull(e);
	final ReentrantLock lock = this.lock;
	lock.lockInterruptibly(); // 获取可中断锁
	try {
		while (count == items.length)
			notFull.await();
		enqueue(e); // 通过enqueue(e)插入元素
	} finally {
		lock.unlock();
	}
}

//enqueue()
private void enqueue(E x) {
	final Object[] items = this.items;
	items[putIndex] = x;
	if (++putIndex == items.length) 
		putIndex = 0;
	count++;
	notEmpty.signal();
}

//take方法
public E take() throws InterruptedException {
	final ReentrantLock lock = this.lock;
	lock.lockInterruptibly();
	try {
		while (count == 0) 
			notEmpty.await();
		return dequeue();
	} finally {
		lock.unlock();
	}
}

//dequeue
private E dequeue() {
	final Object[] items = this.items;
	@SuppressWarnings("unchecked")
	E x = (E) items[takeIndex];
	items[takeIndex] = null;
	if (++takeIndex == items.length) 
		takeIndex = 0;
	count--;
	if (itrs != null) 
		itrs.elementDequeued();
	notFull.signal();
	return x;
}

//阻塞队列的使用场景

//非阻塞队列 实现生产者消费者模式
public class Test {
	private int queueSize = 10;
	private PriorityQueue<Integer> queue = new PriorityQueue<Integer> (queueSize);
	public static void main(String[] args) {
		Test test = new Test();
		Producer producer = test.new Producer();
		Consumer consumer = test.new Consumer();
		producer.start();
		consumer.start();
	}
	
	class Consumer extends Thread {
		@Override
		public void run() {
			while (true) {
				synchronized (queue) {
					while (queue.size() == 0) {
						try {
							System.out.println("队列空，等待数据");
							queue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							queue.notify();
						}
					}
					// 每次移走队首元素
					queue.poll();
					queue.notify();
				}
			}
		}
	}
	
	class Producer extends Thread {
		@Override
		public void run() {
			while (true) {
				synchronized (queue) {
					while (queue.size() == queueSize) {
						try {
							System.out.println("队列满，等待有空余空间");
							queue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							queue.notify();
						}
					}
					// 每次插入一个元素
					queue.offer(i);
					queue.notify();
				}
			}
		}
	}
}

//阻塞队列 实现生产者消费者模式
public class Test {
	private int queueSize = 10;
	private ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(queueSize);
	public static void main(String[] args) {
		Test test = new Test();
		Producer producer = test.new Producer();
		Consumer consumer = test.new Consumer();
		producer.start();
		consumer.start();
	}
	
	class Consumer extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Producer extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					queue.put(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

// 很显然，使用阻塞队列实现无需单独考虑同步和线程间通信的问题，其实现起来很简单。







