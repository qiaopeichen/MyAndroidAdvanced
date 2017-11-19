/*
	行为型模式主要处理类或对象如何交互及如何分配职责。
	它共有11中模式：策略模式、模板方法模式、观察者模式、迭代器模式、责任链模式、命令模式、备忘录模式、状态模式、访问者模式、中介者模式和解释器模式。
*/

/*
	策略模式
	定义：定义一系列的算法，把每一个算法封装起来，并且使它们可相互替换。策略模式使得算法可独立于使用它的客户而独立变化。

	在策略模式中有如下角色：
	Context：上下文角色，用来操作策略的上下文环境，起到承上启下的作用，屏蔽高层模块对策略、算法的直接访问。
	Stragety：抽象策略角色，策略、算法的抽象，通常为接口。
	ConcreteStragety：具体的策略实现。
*/

/*
	策略模式的简单实现：
	这里举武侠的例子：
	张无忌作为一个大侠会遇到很多对手，如果每遇到一个对手他都用自己最厉害的武功去迎战，这显然是不明智的。
	于是张无忌想出了3中应战的策略，分别对付3个实力层次的对手。
*/

/*
	1.定义策略接口
	策略接口有一个fighting方法用于战斗：
*/
public interface FightStrategy {
	public void fighting();
}

/*
	具体策略实现
	分别定义3个策略来实现策略接口，用来对付3个实力层次的对手，代码如下所示：
*/
public class WeakRivalStrategy implements FightStrategy {
	@Override
	public void fighting() {
		System.out.println("遇到了较弱的对手，张无忌使用太极剑");
	}
}

public class CommonRivalStrategy implements FightStrategy {
	@Override
	public void fighting() {
		System.out.println("遇到了普通的对手，张无忌使用圣火令神功");
	}
}

public class StrongRivalStrategy implements FightStrategy {
	@Override
	public void fighting() {
		System.out.println("遇到了强大的对手，张无忌使用乾坤大挪移");
	}
}

/*
	上下文角色
	上下文角色的构造方法包含了策略类，通过传进来不同的具体策略来调用不同策略的fighting方法，如下所示：
*/
public class Context {
	private FightStrategy fightingStrategy;
	public Context(FightingStrategy fightingStrategy) {
		this.fightingStrategy = fightingStrategy;
	}
	public void fighting() {
		fightingStrategy.fighting();
	}
}

/*
	客户端调用
	张无忌对不同实力层次的对手，采用了不同的策略来应战。为了举例，这里省略了对不同实力层次进行判断的条件语句。
*/
public class ZhangWuJi {
	public static void main(String[] args) {
		Context context;
		// 张无忌遇到对手宋青书，采用对较弱对手的策略
		context = new Context(new WeakRivalStrategy());
		context.fighting();
		// 张无忌遇到对手灭绝师太，采用对普通对手的策略
		context = new Context(new CommonRivalStrategy());
		context.fighting();
		// 张无忌遇到对手成昆，采用对强大对手的策略
		context = new Context(new StrongRivalStrategy());
		context.fighting();
	}
}
/*
	上面只是举了一个简单的例子，其实情况会很多：
	比如遇到普通的对手，也不能完全用圣火令神功；
	比如遇到周芷若和赵敏时需要手下留情，采用太极剑；
	又比如遇到强劲的对手张三丰，由于是自己师公，也不能使用乾坤大挪移。
	类似这样的情况会很多，这样在每个策略类中可能会出现很多条件语句。
	但是试想一下如果我们不用策略模式来封装这些条件语句，那么可能会导致一个条件语句中又包含了多个条件语句，这样会使代码变得臃肿，维护的成本也会加大。
*/

/*
	策略模式的使用场景和优缺点
	使用场景：
		对客户隐藏具体策略（算法）的实现细节，彼此完全独立。
		针对同一类型的多种处理方式，仅仅是具体行为有差别时。
		在一个类中定义了很多行为，而且这些行为在这个类里的操作以多个条件语句的形式出现。策略模式将相关的条件分支移入它们各自的Strategy类中，以代替这些条件语句。
	优点：
		使用策略模式可以避免使用多重条件语句。多重条件语句不易维护，而且容易出错。
		易于拓展。当需要添加一个策略时，只需要实现接口就可以了。
	缺点：
		每一个策略都是一个类，复用性小。如果策略过多，类的数量会增多。
		上层模块必须知道有哪些策略，才能够使用这些策略，这与迪米特法则相违背。
*/