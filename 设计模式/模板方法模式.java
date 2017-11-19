/*
	在软件开发中，有时会遇到类似的情况：某个方法的实现需要多个步骤，其中有些步骤是固定的；而有些步骤并不固定，存在可变性。
	为了提高代码的复用性和系统的灵活性，可以使用模板方法模式来应对这类情况。
	
	定义：定义一个操作中的算法框架，而将一些步骤延迟到子类中，使得子类不改变一个算法的结构即可重定义算法的某些特定步骤。

	模板方法模式中有如下角色：
	AbstractClass:抽象类，定义了一套算法框架。
	ConcreteClass:具体实现类。
*/

/*
	模板方法的简单实现
	模板方法实际就是封装固定的流程，像模板一样，第一步做什么，第二步又做什么，都在抽象类中定义好。子类可以有不同的算法实现，在算法框架不被修改的前提下实现某些步骤的算法替换。
*/

/*
	创建抽象类，定义算法框架

	接着举武侠的例子：
	一个武侠要战斗的时候，也有一套固定的通用模式，那就是运行内功、开启经脉、准备武器和使用招式。
	我们把这些用代码表示：
*/
public abstract class AbstractSwordsman {
	// 该方法为final，防止算法框架被复写
	public final void fighting() {
		// 运行内功，抽象方法
		neigong();
		// 调整经脉，具体方法
		meridian();
		// 如果有武器，则准备武器
		if (hasWeapons()) { // 2
			weapons();
		}
		// 使用招式
		moves();
		// 钩子方法
		hook(); // 1
	}

	// 空实现方法
	protected void hook() {}
	protected abstract void neigong();
	protected abstract void weapons();
	protected abstract void moves();
	protected void meridian() {
		System.out.println("开启正经与奇经");
	}
	/**
	* 是否有武器，默认是有武器的，钩子方法
	* @return 
	*/
	protected boolean hasWeapons() {
		return true;
	}
}
/*
	这个抽象类包含了3种类型的方法，分别是抽象方法、具体方法和钩子方法。
	抽象方法是交由子类去实现的，具体方法则是父类实现了子类公共的方法。在上面的例子中就是武侠开启经脉的方式都一样，所以就在具体方法中实现。
	钩子方法则分两类：
	第一类在上面代码注释1处，它有一个空实现的方法，子类可以视情况来决定是否要覆盖它；
	第二类在注释2处，这类钩子方法的返回类型通常是boolean类型的，其一般用于对某个条件进行判断，如果条件满足则执行某一步骤，否则将不执行。
*/

/*
	具体实现类
	武侠就拿张无忌、张三丰来作为例子，代码如下所示：
*/
public class ZhangWuJi extends AbstractSwordsman {
	@Override
	protected void neigong() {
		System.out.println("运行九阳神功");
	}
	@Override
	protected void weapons() {
	}
	@Override
	protected void moves() {
		System.out.println("使用招式乾坤大挪移");
	}
	@Override
	protected boolean hasWeapons() {
		return false;
	}
}
/*
	张无忌没有武器，所以hasWeapons方法返回false，这样也不会进入weaponse方法了。
	接下来看张三丰的代码：
*/
public class ZhangSanFeng extends AbstractSwordsman {
	@Override
	protected void neigong() {
		System.out.println("运行纯阳无极功");
	}
	@Override
	protected void weapons() {
		System.out.println("使用真武剑");
	}
	@Override
	protected void moves() {
		System.out.println("使用招式神门十三剑");
	}
	@Override
	protected void hook() {
		System.out.println("突然肚子不舒服，老夫先去趟厕所");
	}
}
/*
	最后，张三丰突然感觉肚子不舒服，所以就实现了钩子方法hook，用来处理一些自定义的逻辑。
*/

/*
	客户端调用
*/
protected class Client {
	public static void main(String[] args) {
		ZhangWuJi zhangWuJi = new ZhangWuJi();
		ZhangWuJi.fighting();
		ZhangSanFeng zhangSanFeng = new ZhangSanFeng();
		zhangSanFeng.fighting(); 
	}
}

/*
	模板方法模式的使用场景和优缺点
	使用场景：
		多个子类有共有的方法，并且逻辑基本相同时。
		面对重要、复杂的算法，可以把核心算法设计为模板方法，周边相关细节功能则由各个子类实现。
		需要通过子类来决定父类算法中的某个步骤是否执行，实现子类对父类的反向控制。
	优点：
		模板方法模式通过把不变的行为迁移到父类，去除了子类中的重复代码。
		子类实现算法的某些细节，有助于算法的扩展。
	缺点：
	每个不同的实现都需要定义一个子类，这会导致类的个数的增加，设计更加抽象。
*/