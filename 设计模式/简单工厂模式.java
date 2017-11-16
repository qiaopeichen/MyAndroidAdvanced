/*
	简单工厂模式（又叫做静态工厂方法模式），其属于创建性设计模式，但并不属于23种GoF设计模式之一。
	提到它是为了让大家能够更好地理解后面讲到的工厂方法模式。
	定义：简单工厂模式属于创建性模式，其又被称为静态工厂方法模式，这是由一个工厂对象决定创建出哪一种产品类的实例。
*/

/*
	我们用生产计算机来举例，假设有一个计算机的代工生产商，它目前已经可以代工生产联想计算机了。
	随着业务的扩展，这个代工生产商还要生产惠普和华硕的计算机。这样我们就需要用一个单独的类来专门生产计算机，
	这就用到了简单工厂模式。
*/

1.抽象产品类
我们创建一个计算机的抽象产品类，其中一个抽象方法用于启动计算机，如下所示：

public abstract class Computer {
	/**
	 * 产品的抽象方法，由具体的产品类实现。 
	 */
	public abstract void start();
}

2.具体产品类
接着我们创建各个品牌的计算机，其都继承了自己的父类Computer，并实现了父类的start方法。
具体的计算机产品分别是联想、惠普和华硕计算机：

public class LenovoComputer extends Computer {
	@Override
	public void start() {
		System.out.println("联想计算机启动");
	}
}

public class HpComputer extends Computer {
	@Override
	public void start() {
		System.out.println("惠普计算机启动");
	}
}

public class AsusComputer extends Computer {
	@Override
	public void start() {
		System.out.println("华硕计算机启动");
	}
}

3.工厂类
接下来创建一个工厂类，它提供了一个静态方法createComputer用来生产计算机。你只需要传入自己想生产的计算机的品牌，
他就会实例化相应品牌的计算机对象，代码如下所示：
public class ComputerFactory {
	public static Computer createComputer(String type) {
		Computer mComputer = null;
		switch (type) {
			case "lenovo":
				mComputer = new LenovoComputer();
				break;
			case "hp":
				mComputer = new HpComputer();
				break;
			case "asus":
				mComputer = NEW AsusComputer();
				break;
		}
		return mComputer;
	}
}

4.客户端调用工厂类
客户端调用工厂类，传入“hp”生产出惠普计算机并调用该计算机对象的start方法，如下所示：
public class CreateComputer {
	public static void main(String[] args) {
		ComputerFactory.CreateComputer("hp").start();
	}
}

使用简单工厂模式的场景和优缺点：
使用场景：
	1.工厂类负责创建的对象比较少。
	2.客户只需知道传入工厂类的参数，而无需关心创建对象的逻辑。
优点：
	使用户根据参数获得对应的类的实例，避免了直接实例化类，降低了耦合性。
缺点：
	可实例化的类型在编译期间已经被锁定，如果增加新类型，则需要修改工厂，这违背了开放封闭原则。简单工厂需要知道所有要生成的类型，其当子类过多或者子类层次过多时不适合使用。













