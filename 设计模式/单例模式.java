// 单例模式：保证一个类仅有一个实例，并提供一个访问它的全局访问点。

// 单例模式的6种写法

// 1.饿汉模式
public class Singleton {
	private static Singleton instance = new Singleton();
	private Singleton() {
	}
	public static Singleton getInstance() {
		return instance;
	}
}
/*
	这种方式在类加载时就完成了初始化，所以类加载较慢，但获取对象的速度快。
	这种方式基于类加载机制，避免了多线程的同步问题。在类加载的时候就完成实例化，没有达到懒加载的效果。
	如果从始至终未使用过这个实例，则会造成内存的浪费。
*/

// 2.懒汉模式（线程不安全）
public class Singleton {
	private static Singleton instance;
	private Singleton() {
	}
	public static Singleton getInstance() {
		if (instance == null) {
			instance = new Singleton();
		}
		return instance;
	}
}
/*
	懒汉模式声明了一个静态对象，在用户第一次调用时初始化，这虽然节约了资源，但第一次加载时需要实例化，反应稍慢一些，
	而且在多线程时不能正常工作。
*/

// 3.懒汉模式（线程安全）
public class Singleton {
	private static Singleton instance;
	private Singleton() {
	}
	public static synchronized Singleton getInstance() {
		if (instance == null) {
			instance = new Singleton();
		}
		return instance;
	}
}
/*
	这种写法能够在多线程中很好地工作，但是每次调用getInstance方法时都需要进行同步。
	这会造成不必要的同步开销，而且大部分时候我们是用不到同步的。所以不建议用这种模式。
*/

// 4.双重检查模式（DCL）
public class Singleton {
	private volatile static Singleton instance;
	private Singleton() {
	}
	public static Singleton getInstance() {
		if (instance == null) {
			synchronized (Singleton.class) {
				if (instance == null) {
					instance = new Singleton();
				}
			}
		}
		return instance;
	}
}
/*
	这种写法在getSingleton方法中对Singleton进行了两次判空：第一次是为了不必要的同步，第二次是在Singleton等于null的情况下才创建实例。
	在这里使用volatile会或多或少地影响性能，但考虑到程序的正确性，牺牲这点性能还是值得的。
	优点：资源利用率高，第一次执行getInstance时单例对象才被实例化，效率高。
	缺点：第一次加载时反应稍慢一些，在高并发环境下也有一定的缺陷。
	DCL虽然在一定程度上解决了资源的消耗和多余的同步、线程安全等问题，但其还是在某些情况下会出现失效的问题，也就是DCL失效。
	这里建议用静态内部类单例模式来替代DCL。
*/

// 5.静态内部类单例模式
public class Singleton {
	private Singleton() {
	}
	public static Singleton getInstance() {
		return SingletonHolder.sInstance;
	}
	private static class SingletonHolder {
		private static final Singleton sInstance = new Singleton();
	}
}
/*
	第一次加载Singleton类时并不会初始化sInstance，只有第一次调用getInstance方法时虚拟机加载SingletonHolder并初始化sInstance。
	这样不仅能确保线程安全，也能保证Singleton类的唯一性。
	所以，推荐使用静态内部类单例模式。
*/

// 6.枚举单例
public enum Singleton {
	INSTANCE; // 枚举写在最前面，否则编译出错。
	public void doSomeThing() {
	}
}

/*
	默认枚举实例的创建是线程安全的，并且在任何情况下都是单例。在上面讲的集中单例模式视线中，有一种情况下其会重新创建对象，那就是反序列化：
	将一个单例实例对象写到磁盘再堵回来，从而获得了一个实例。反序列化操作提供了readResolve方法，这个方法可以让开发人员控制对象的反序列化。
	在上述几个方法示例中，如果要杜绝单例对象被反序列化时重新生成对象，就必须加入如下方法：
*/
private Object readResolve() throws ObjectStreamException {
	return singleton;
}

/*
	枚举单例的优点就是简单，但是大部分应用开发很少用枚举，其可读性并不是很高。
	至于选择哪种形式的单例模式，则取决于你的项目本身情况：
	是否为复杂的并发环境，或者是否需要控制单例对象的资源消耗。
*/

/*
	单例模式的使用场景：
	在一个系统中，要求一个类有且仅有一个对象，它的具体使用场景如下：
	1. 整个项目需要一个共享访问点或共享数据。
	2. 创建一个对象需要耗费的资源过多，比如访问I/O或者数据库等资源。
	3. 工具类对象。
*/















