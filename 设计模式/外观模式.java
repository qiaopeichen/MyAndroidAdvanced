/*
	外观模式也被称为门面模式。当我们开发Android的时候，无论是做SDK还是封装API，大多会用到外观模式，它通过一个外观类使得整个系统的结构只有一个统一的高层接口，这样能降低用户的使用成本。
	定义：要求一个子系统的外部与内部的通信必须通过一个统一的对象进行。此模式提供一个高层的接口，使得子系统更易于使用。
	
	在外观模式中有如下角色：
	Facade：外观类，知道哪些子系统类负责处理请求，将客户端的请求代理给适当的子系统对象。
	Subsystem：子系统类，可以有一个或者多个子系统。实现子系统的功能，处理外观类指派的任务，注意子系统类不含有外观类的引用。
*/

/*
	外观模式的简单实现
	举一个武侠的例子。首先，我们把武侠张无忌当做一个系统。张无忌作为一个武侠，他本身分为3个系统，分别是招式、内功和经脉。
*/

1.子系统类
/**
 * 子系统招式
 */
public class ZhaoShi {
	public void TaiJiQuan() {
		System.out.println("使用招式太极拳");
	}
	public void QiShangQuan() {
		System.out.println("使用招式七伤拳");
	}
	public void ShengHuo() {
		System.out.println("使用招式圣火令");
	}
}

/**
 * 子系统内功
 */
public class NeiGong {
	public void JiuYang() {
		System.out.println("使用九阳神功");
	}
	public void QianKun() {
		System.out.println("使用乾坤大挪移");
	}
}

/**
 * 子系统经脉
 */
public class JingMai {
	public void jingmai() {
		System.out.println("开启经脉");
	}
}

张无忌有很多武学和内功，怎么将它们搭配并对外界隐藏呢？接下来查看外观类。

2.外观类
这里的外观类就是张无忌，他负责将自己的招式、内功和经脉通过不同的情况合理地运用，代码如下所示：

/**
 * 外观类张无忌
 */
public class ZhangWuJi {
	private JingMai jingMai;
	private ZhaoShi zhaoShi;
	private NeiGong neiGong;
	public ZhangWuJi() {
		jingMai = new JingMai();
		zhaoShi = new ZhaoShi();
		neiGong = new NeiGong();
	}
	/**
	 * 使用乾坤大挪移
	 */
	public void Qiankun() {
		jingMai.jingmai(); // 开启经脉
		neiGong.QianKun(); // 使用内功乾坤大挪移
	}
	/**
	 * 使用七伤拳
	 */
	public void QiShang() {
		jingMai.jingmai(); // 开启经脉
		neiGong.JiuYang(); // 使用内功九阳神功
		zhaoShi.QiShangQuan(); // 使用招式七伤拳
	}
}

/*
	初始化外观类的同时将各个子系统类创建好。很明显，张无忌很好地将自身的各个系统进行了搭配。如果其使用七伤拳，就需要开启经脉、使用内功九阳神功，接下来使用七伤拳。
	否则七伤拳的威力会大打折扣。
*/

3.客户端调用
public class Client {
	public static void main(String[] args) {
		ZhangWuJi zhangWuJi = new ZhangWuJi();
		// 张无忌使用乾坤大挪移
		zhangWuJi.QianKun();
		// 张无忌使用七伤拳
		zhangWuJi.QiShang();
	}
}
/*
	当张无忌使用乾坤大挪移或者七伤拳的时候，比武的对手显然不知道张无忌本身运用了什么，同时张无忌也无须重新计划使用七伤拳的时候要怎么做（他已经早就计划好了）。
	如果每次使用七伤拳都要计划怎么做很显然会增加成本并贻误战机。另外张无忌也可以改变自己的内功、招式和静脉，这些都是对比武的对手有所隐藏的。
	外观模式本身就是将子系统的逻辑和交互隐藏起来，为用户提供一个高层次的接口，使得系统更加易用，同时也隐藏了具体的实现，这样即使具体的子系统发生了变化，用户也不会感知到。
*/

/*
	外观模式的使用场景和优缺点
	使用场景：
		构建一个有层次结构的子系统时，使用外观模式定义子系统中每层的入口点。如果子系统之间是相互依赖的，则可以让其通过外观接口进行通信，减少子系统之间的依赖关系。
		子系统往往会因为不断地重构演化而变得越来越复杂，大多数的模式使用时也会产生很多很小的类，这给外部调用它们的用户程序带来了使用上的困难。我们可以使用外观类提供一个简单的接口，对外隐藏子系统的具体实现并隔离变化。
		当维护一个遗留的大型系统时，可能这个系统已经非常难以维护和拓展；但因为它含有重要的功能，所以新的需求必须依赖于它，这时可以使用外观类，为设计粗糙或者复杂的遗留代码提供一个简单的接口，让新系统和外观类交互，而外观类负责与遗留的代码进行交互。
	优点：
		减少系统的相互依赖，所有的依赖都是对外观类的依赖，与子系统无关。
		对用户隐藏了子系统的具体实现，减少用户对子系统的耦合。这样即使具体的子系统发生了变化，用户也不会感知到。
		加强了安全性，子系统中的方法如果不在外观类中开通，就无法访问到子系统中的方法。
	缺点：
		不符合开放封闭原则。如果业务出现变更，则可能要直接修改外观类。
*/





















