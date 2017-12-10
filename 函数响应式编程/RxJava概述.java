/*
	函数式编程是一种编程范式。我们常见的编程范式有命令式编程、函数式编程和逻辑式编程。
	我们常见的面向对象编程是一种命令式编程。命令式编程是面向计算机硬件的抽象，有变量、赋值语句、表达式和控制语句。
	而函数式编程是面向数学的抽象，将计算描述为一种表达式求值，函数可以在任何地方定义，并且可以对函数进行组合。
	
	响应式编程是一种面向数据流和变化传播的编程范式，数据更新是相关联的。把函数式编程里的一套思路和响应式编程合起来就是函数响应式编程。
	
	函数响应式编程可以极大地简化项目，特别是处理嵌套回调的异步事件、复杂的列表过滤和变换或者时间相关问题。
	在 Android 开发中使用函数响应式编程的主要有两大框架：一个是 RxJava，另一个是 Google 推出的 Agera。
*/

/*
	RxJava概述

	1.ReactiveX 与 RxJava
	在讲到 RxJava 之前我们首先要了解什么是 ReactiveX，因为 RxJava 是 ReactiveX 的一种 Java 实现。ReactiveX 是 Reactive Extensions 的缩写，一般简写为 Rx。
	微软给的定义是，Rx 是一个函数库，让开发者可以利用可观察序列和 LINQ 风格查询操作符来编写异步和基于事件的程序。
	开发者可以用 Observables 表示异步数据流，用 LINQ 操作符查询异步数据流，用 Schedulers 参数化异步数据流的并发处理，Rx可以这样定义：Rx = Observables + LINQ + Schedulers。

	2.为何要用 RxJava
	说到异步操作，我们会想到 Android 的 AsyncTask 和 Handler，但是随着请求的数量越来越多，代码逻辑会变得越来越复杂而 RxJava却仍旧能保持清晰的逻辑。
	RxJava 的原理就是创建一个 Observable 对象来干活，然后使用各种操作符建立起来的链式操作，就如同流水线一样。把你想要处理的数据一步一步地加工成你想要的成品，然后发射给 Subscriber 处理。

	3.RxJava与观察者模式
	RxJava 的异步操作是通过扩展的观察者模式来实现的。RxJava 有 4 个角色 Observable、Observer、Subscriber和Subject，这 4 个角色后面会具体讲解。
	Observable 和 Observer 通过 subscribe 方法实现订阅关系，Observable 就可以在需要的时候通知 Observer。
*/