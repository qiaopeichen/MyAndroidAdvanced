/*
	定义一个用于创建对象的接口，让子类决定实例化哪个类。工厂方法使一个类的实例化延迟到其子类。
	工厂方法模式中有如下角色：
	Product：抽象产品类
	ConcreteProduct：具体产品类，实现Product接口。
	Factory：抽象工厂类，该方法返回一个Product类型的对象。
	ConcreteFactory：具体工厂类，返回ConcreteProduct实例。

	工厂方法模式的抽象产品类与具体产品类的创建和简单工厂模式是一样的，这里就不赘述了。
*/

1.创建抽象工厂
抽象工厂里面有一个createComputer方法，想生产哪个品牌的计算机就生产哪个品牌的，如下所示：
public abstract class ComputerFactory {
	public abstract <T extends Computer> T createComputer(Class<T> clz);
}

2.具体工厂
广达代工厂是一个具体的工厂，其继承抽象工厂，通过反射来生产不同厂家的计算机。
public class GDComputerFactor extends ComputerFactory {
	@Override
	public <T extends Computer> T createComputer(Class<T> clz) {
		Computer computer = null;
		String classname = clz.getName;
		try {
			// 通过反射来生产不同厂家的计算机
			computer = (Computer) Class.forName(classname).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (T) computer;
	}
}

3.客户端调用
客户端创建了GDComputerFactor，并分别生产了联想、惠普和华硕计算机。
public class Client {
	public static void main(String[] args) {
		ComputerFactory computerFactory = new GDComputerFactor();
		LenovoComputer mLenovoComputer = computerFactory.createComputer(LenovoComputer.class);
		mLenovoComputer.start();
		HpComputer mHpComputer = computerFactory.createComputer(HpComputer.class);
		mHpComputer.start();
		AsusComputer mAsusComputer = computerFactory.createComputer(AsusComputer.class);
		mAsusComputer.start();
	}
}

/*
	对于简单工厂模式，我们都知道其在工厂了中包含了必要的逻辑判断，根据不同的条件来动态实例化相关的类。
	对客户端来说，这去除了与具体产品的依赖；但与此同时也会带来一个问题：
	如果我们要增加产品，就需要在工厂类中添加一个Case分支条件，这违背了开放封闭原则。
	而工厂方法模式就没有违背这个原则。
*/










