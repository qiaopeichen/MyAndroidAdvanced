/*
	代理模式也被称为委托模式，它是结构型设计模式的一种。在现实生活中我们用到类似代理模式的场景有很多，比如代理上网、打官司等。
	
	定义：为其他对象提供一种代理以控制对这个对象的访问。
	
	在代理模式中有如下角色：
	Subject:抽象主题类，声明真是主题与代理的共同接口方法。
	RealSubject:真实主题类，代理类所代表的真实主题。客户端通过代理类间接地调用真实主题类的方法。
	Proxy:代理类，持有对真实主题类的引用，在其所实现的接口方法中调用真实主题类中相应的接口方法执行。
	Client:客户端类。
	
	代理模式的简单实现：
	我多年没有回过哈尔滨了，很是想念哈尔滨秋林红肠的味道。但是本人工作一直很忙抽不开身，不能够亲自回哈尔滨购买，于是就托在哈尔滨的朋友帮我购买秋林红肠。
*/

1.抽象主题类
抽象主题类具有真实主题类和代理的共同接口方法，共同的方法就是购买：
public interface IShop {
	void buy();
}

2.真实主题类
这个购买者，实现了IShop接口提供的buy()方法，如下所示：
public class LiuWangShu implements IShop {
	@Override
	public void buy() {
		System.out.println("购买");
	}
}

3.代理类
我找的代理类同样也要实现IShop接口，并且要持有被代理者，在buy()方法中调用了被代理者的buy()方法：
public class Purchasing implements IShop {
	private IShop mShop;
	public Purchasing(IShop shop) {
		mShop = shop;
	}
	@Override
	public void buy() {
		mShop.buy();
	}
}

4.客户端类
public class Client {
	public static void main(String[] args) {
		IShop liuwangshu = new LiuWangShu();
		purchasing.buy();
	}
}

/*
	客户端类的代码就是代理类包含了真实主题类（被代理者），最终调用的都是真实主题类实现的方法。
	上面的例子中就是LiuWangShu类的buy()方法，所以运行的结果就是“购买”。
*/

2.动态代理的简单实现。
/*
	从编码的角度来说，代理模式分为静态代理和动态代理。上面的例子是景泰代理，在代码运行前就已经存在了代理类的class编译文件；
	而动态代理则是在代码运行时通过反射来动态第生成代理类的对象，并确定到底来代理谁。
	也就是我们在编码截断无需知道代理谁，代理谁将会在代码运行时决定。
	java提供了动态的代理接口InvocationHandler，实现该接口需要重写invoke()方法。下面我们在上面静态代理的例子上做修改。
	首先创建动态代理类，代码如下所示：
*/
public class DynamicPurchasing implements InvocationHandler {
	private Object obj;
	public DynamicPurchasing(Object obj) {
		this.obj = obj;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = method.invoke(obj, args);
		if (method.getName().equals("buy")) {
			System.out.println("LiuWangShu买买买");
		}
		return result;
	}
}

/*
	在动态代理类中我们声明一个Object的引用，该引用指向被代理类，我们调用被代理类的具体方法在invoke()方法中执行。接下来我们修改客户端类代码：
*/
public class Client {
	public static void main(String[] args) {
		// 创建LiuWangShu
		IShop liuwangshu = new LiuWangShu();
		// 创建动态代理
		DynamicPurchasing mDynamicPurchasing = new DynamicPurchasing(liuwangshu);
		// 创建LiuWangShu的ClassLoader
		ClassLoader loader = liuwangshu.getClass().getClassLoader();
		// 动态创建代理类
		IShop purchasing = (IShop) Proxy.newProxyInstance(loader, new Class[] {IShop.class}, mDynamicPurchasing);
		purchasing.buy();
	}
}
/*
	调用Proxy.newProxyInstance()来生成动态代理类，调用purchasing的buy方法会调用DynamicPurchasing的invoke方法。
*/

/*
	代理模式的类型和优点
	代理模式从编码的角度来说可以分为静态代理和动态代理，而从适用范围来讲可以分为以下4种类型。
	远程代理：为一个对象在不同的地址空间提供局部代表，遮阳系统可以将Server部分的实现隐藏。
	虚拟代理：使用一个代理对象表示一个十分耗费资源的对象并在真正需要时才创建。
	安全代理：用来控制真实对象访问时的权限。一般用于真实对象有不同的访问权限时。
	智能指引：当调用真实的对象时，代理处理另外一些事，比如计算机真实对象的引用计数，当该对象没有引用时，可以自动释放它；或者访问一个实际对象时，检查是否已经能够锁定它，以确保其他对象不能改变它。

	代理模式的优点：
	真实主题类就是实现实际的业务逻辑，不用关心其他非本职的工作。
	真实主题类随时都会发生变化；但是因为它实现了公共的接口，所以代理类可以不做任何修改就能使用。
*/














