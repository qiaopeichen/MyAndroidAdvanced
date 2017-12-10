/*
	Subject 既可以是一个 Observer 也可以是一个 Observerable，它是连接 Observer 和Observerable 的桥梁。
	因此，Subject 可以被理解为 Subject = Observable + Observer。RxJava 提供了以下 4 种 Subject。
*/

/*
	1.PublishSubject
	PublishSubject 只会把在订阅发生的时间点之后来自原始 Observable 的数据发射给观察者。
	需要注意的是，PublishSubject 可能会一创建完就立刻开始发射数据，因此这里会有一个风险：
	在 Subject 被创建后到有观察者订阅它之前的这个时间段内，一个或多个数据可能会丢失。如果要确保来自原始 Observable 的所有数据都被分发，则可以当所有观察者都已经订阅时才开始发射数据，或者改用 ReplaySubject。

	2.BehaviorSubject
	当 Observer 订阅 BehaviorSubject 时，它开始发射原始 Observable 最近发射的数据。如果此时还没有收到任何数据，它会发射一个默认值，然后继续发射其他任何来自原始 Observable 的数据。
	如果原始的 Observable 因为发生了一个错误而终止，BehaviorSubject 将不会发射任何数据，但是会向 Observer 传递一个异常通知。

	3.ReplaySubject
	不管 Observer 何时订阅 ReplaySubject，ReplaySubject 均会发射所有来自原始 Observable 的数据给 Observer。有不同类型的 ReplaySubject，它们用于限定 Replay 的范围，例如设定 Buffer 的具体大小，
	或者设定具体的时间范围。如果使用 ReplaySubject 作为 Observer，注意不要在多个线程中调用 onNext、onComplete和onError方法。这可能会导致顺序错乱，并且违反了Observer规则。

	4.AsyncSubject
	当 Observable 完成时，AsyncSubject 只会发射来自原始 Observable 的最后一个数据。如果原始的 Observable 因为发生了错误而终止，AsyncSubject 将不会发射任何数据，但是会向 Observer 传递一个异常通知。
*/