/*
	装饰模式是结构型设计模式之一，其在不必改变类文件和使用继承的情况下，动态地扩展一个对象的功能，是继承的替代方案之一。
	它通过创建一个包装对象，也就是装饰来包裹真实的对象。
	
	定义：动态地给一个对象添加一些额外的职责，就增加功能来说，装饰模式比生成子类更为灵活。
	
	在装饰模式中有如下角色：
	Component:抽象组件，可以是接口或是抽象类，被装饰的最原始的对象。
	ConcreteComponent:组件具体实现类。Component的具体实现类，被装饰的具体对象。
	Decorator:抽象装饰者，从外类来拓展Component类的功能，但对于Componenet来说无须知道Decorator的存在。在它的属性中必然有一个private变量指向Component抽象组件。
	ConcreteDecorator:装饰者的具体实现类。
*/
/*
	装饰者模式的简单实现
	装饰者模式在现实生活中有许多例子，比如一个人穿上各种衣服，给一幅画涂色、上框等。

	举一个武侠修炼武功的例子：杨过本身就会全真剑法，有洪七公和欧阳锋传授打狗棒法和蛤蟆功。
	洪七公和欧阳锋就起到了“装饰”杨过的作用。
*/

1.抽象组件
作为武侠肯定要会使用武功，我们先定义一个武侠的抽象类，里面有使用武功的抽象方法：
public abstract class Swordsman {
	/**
	 * Swordsman 武侠有使用武功的抽象方法。 
	 */
	public abstract void attackMagic();
}

2.组件具体实现类 
被装饰的具体对象，在这里就是被传授武学的具体武侠，也就是杨过。杨过当然会武学：
public class YangGuo extends Swordsman {
	@Override public void attackMagic() {
		// 杨过初始的武学是全真剑法
		System.out.println("杨过使用全真剑法");
	}
}

3.抽象装饰者
抽象装饰者保持了一个对抽象组件的引用，方便调用被装饰者对象中的方法。在这个例子中就是武学前辈要持有武侠的引用，方便教授他武学并使他融会贯通：
public abstract class Master extends Swordsman {
	private Swordsman mSwordsman;
	public Master(Swordsman mSwordsman) {
		this.mSwordsman = mSwordsman;
	}
	@Override
	public void attackMagic() {
		mSwordsman.attackMagic();
	}
}

4.装饰者具体实现类
这个例子中用两个装饰者具体实现类，分别是洪七公和欧阳锋，他们负责向杨过传授新的武功，如下所示：
public class HongQiGong extends Master {
	public HongQiGong(Swordsman mSwordsman) {
		super(mSwordsman);
	}
	public void teachAttackMagic() {
		System.out.println("洪七公教授打狗棒法");
		System.out.println("杨过使用打狗棒法");
	}
	@Override
	public void attackMagic() {
		super.attackMagic();
		teachAttackMagic();
	}
}

public class OuYangFeng extends Master {
	public OuYangFeng(Swordsman mSwordsman) {
		super(mSwordsman);
	}
	public void teachAttackMagic() {
		System.out.println("欧阳锋教授蛤蟆功");
		System.out.println("杨过使用蛤蟆功");
	}
	@Override
	public void attackMagic() {
		super.attackMagic();
		teachAttackMagic();
	}
}

5.客户端调用
经过洪七公和欧阳锋的教导，杨过除了全真剑法又学会了打狗棒法和蛤蟆功。
public class Client {
	public static void main(String[] args) {
		// 创建杨过
		YangGuo mYangguo = new YangGuo();
		// 洪七公传授打狗棒法，杨过学会打狗棒法
		HongQiGong mHongQiGong = new HongQiGong(mYangguo);
		mHongQiGong.attackMagic();
		// 欧阳锋传授蛤蟆功，杨过学会蛤蟆功
		OuYangFeng mOuYangFeng = new OuYangFeng(mYangguo);
		mOuYangFeng.attackMagic();
	}
}

/*
	装饰者模式的使用场景和优缺点
	使用场景：
		在不影响其他对象的情况下，以动态、透明的方式给单个对象添加职责。
		需要动态地给一个对象增加功能，这些功能可以动态地撤掉。
		当不能采用继承的方式对系统进行扩充或者采用继承不利于系统扩展和维护时。
	优点：
		通过组合而非继承的方式，动态地扩展一个对象的功能，在运行时选择不同的装饰器，从而实现不同的行为。
		有效避免了使用继承的方式扩展对象功能而带来的灵活性差、子类无限制扩张的问题。
		具体组件类与具体装饰类可以独立变化，用户可以根据需要增加新的具体组件类和具体装饰类，在使用时再对其进行组合，原有代码无须改变，符合开放封闭原则。
	缺点：
		因为所有对象均继承于Component，所以如果Component内部结构发生改变，则不可避免地影响所有子类（装饰者和被装饰者）。如果基类改变，则势必影响对象的内部。
		比继承更加灵活机动的特性，也同时意味着装饰模式比继承更加易于出错 ，排错也很困难。对于多次装饰的对象，调试时寻找错误可能需要逐级排查，较为繁琐。所以，只在必要的时候使用装饰模式。
		装饰层数不能过多，否则会影响效率。
*/


























