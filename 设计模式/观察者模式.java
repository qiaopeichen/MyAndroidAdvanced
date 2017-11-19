/*
	观察者模式又称为发布-订阅模式，属于行为型设计模式的一种，是一个在项目中经常使用的模式。
	定义：定义对象间一种一对多的依赖关系，每当一个对象改变状态时，则所有依赖于它的对象都会得到通知并被自动更新。

	在观察者模式中有如下角色：
	Subject:抽象主题（抽象被观察者）。抽象主题角色把所有观察者对象保存在一个集合里，每个主题都可以有任意数量的观察者。抽象主题提供一个接口，可以增加和删除观察者对象。
	ConcreteSubject:具体主题（具体被观察者）。该角色将有关状态存入具体观察者对象，在具体主题的内部状态发生改变时，给所有注册过的观察者发送通知。
	Observer:抽象观察者，是观察者的抽象类。它定义了一个更新接口，使得在得到主题更改通知时更新自己。
	ConcreteObserver:具体观察者，实现抽象观察者定义的更新接口，以便在得到主题更改通知时更新自身的状态。
*/

/*
	观察者模式的简单实现
	关于观察者模式这种发布-订阅的形式，我们可以拿微信公众号来举例。假设微信用户就是观察者，微信公众号是被观察者，有多个微信用户关注了“程序猿”这个公众号，当这个公众号更新时就会通知这些订阅的微信用户。
	接下来用代码实现：
*/

/*
	抽象观察者
	里面只定义了一个更新的方法：
*/
public interface Observer {
	public void update(String message);
}

/*
	具体观察者
	微信用户是观察者，里面实现了更新的方法，如下所示：
*/
public class WeixinUser implements Observer {
	// 微信用户名
	private String name;
	public WeixinUser(String name) {
		this.name = name;
	}
	@Override
	public void update(String message) {
		System.out.println(name + "-" + message);
	}
}

/*
	抽象被观察者
	抽象被观察者，提供了attach、detach、notify三个方法，如下所示：
*/
public interface Subject {
	/**
	 * 增加订阅者
	 * @param observer
	 */
	public void attach(Observer observer);

	/**
     * 删除订阅者
     * @param observer
	 */
	public void detach(Observer observer);

	/**
	 * 通知订阅者更新消息
	 */
	public void notify(String message);
}

/*
	具体被观察者
	微信公众号是具体主题（具体被观察者），里面存储了订阅该公众号的微信用户，并实现了抽象主题中的方法：
*/
public class SubscriptionSubject implements Subject {
	// 存储订阅公众号的微信用户
	private List<Observer> WeixinUserlist = new ArrayList<Observer>();
	@Override
	public void attach(Observer observer) {
		weixinUserlist.add(observer);
	}
	@Override
	public void detach(Observer observer) {
		weixinUserlist.remove(observer);
	}
	@Override
	public void notify(String message) {
		for (Observer observer : weixinUserlist) {
			observer.update(message);
		}
	}
}

// 客户端调用
public class Client {
	public static void main(String[] args) {
		SubscriptionSubject mSubjectriptionSubject = new mSubjectriptionSubject();
		// 创建微信用户
		WeixinUser user1 = new WeixinUser("杨影枫");
		WeixinUser user2 = new WeixinUser("月眉儿");
		WeixinUser user3 = new WeixinUser("紫轩");
		// 订阅公众号
		mSubjectriptionSubject.attach(user1);
		mSubjectriptionSubject.attach(user2);
		mSubjectriptionSubject.attach(user3);
		// 公众号更新发出消息给订阅的微信用户
		mSubjectriptionSubject.notify("刘望舒的专栏更新了");
	}
}

/*
	观察者模式的使用场景和优缺点
	使用场景：
		关联行为场景。需要注意的是，关联行为是可拆分的，而不是“组合”关系。
		事件多级触发场景。
		跨系统的消息交换场景，如消息队列、事件总线的处理机制。
	优点：
		观察者和被观察则和之间是抽象耦合，容易扩展。
		方便建立一套触发机制。
	缺点：
		在应用观察者模式时需要考虑一下开发效率和运行效率的问题。
		程序中包括一个被观察者。多个观察者，开发、调试等内容会比较复杂，
		而且在Java中消息的通知一般是顺序执行的，那么一个观察者卡顿，会影响整体的执行效率，在这种情况下，
		一般会采用异步方式。
*/