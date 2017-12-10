// 在使用RxJava前配置gradle:
dependencies {
	...
	compile 'io.reactivex:rxjava:1.2.0'
	compile 'io.reactivex:rxandroid:1.2.1'
}

// 其中RxAndroid 是 RxJava 在 Android 平台的扩展。它包含了一些能够简化 Android开发的工具，比如特殊的调度器（后文会提到）。
// RxJava的基本用法分为如下 3 个步骤：
// 1.创建 Observer（观察者）
// 它决定事件触发的时候将有怎样的行为，代码如下所示：
Subscriber subscriber = new Subscriber() {
    @Override
    public void onCompleted() {
        Log.d(TAG, "onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG, "onError");
    }

    @Override
    public void onNext(String s) {
        Log.d(TAG, "onNext" + s);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
    }
}

/*
	其中 onCompleted、onError 和 onNext 是必须要实现的方法，其含义如下：

	onCompleted：事件队列完结。RxJava 不仅把每个事件单独处理，其还会把它们看做一个队列。当不会再有新的 onNext 发出时，需要触发 onCompleted() 方法作为完成标志。
	onError：事件队列异常。在事件处理过程中出现异常时，onError()会被触发，同时队列自动终止，不允许再有事件发出。
	onNext：普通的事件。将要处理的事件添加到事件队列中。
	onStart：它会在事件还未发送之前被调用，可以用于做一些准备工作。例如数据的清零或重置。这是一个可选方法。默认情况下它的实现为空。

	当然，如果要实现简单的功能，也可以用到 Observer 来创建观察者。Observer 是一个接口，而上面用到的 Subscriber 也会先被转换为 Subscriber 来使用。用 Obsever 创建观察者，如下所示：
*/
Observer<String> observer = new Observer<String>() {
    @Override
    public void onCompleted() {
        Log.d(TAG, "onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG, "onError");
    }

    @Override
    public void onNext(String s) {
        Log.d(TAG, "onNext" + s);
    }
}

// 2.创建 Observable（被观察者）
// 它决定什么时候出发时间以及触发怎样的事件。RxJava 使用 create 方法来创建 Observable，并为它定义事件触发规则，如下所示：
Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
    @Override
    public void call(Subscriber<? super String> subscriber) {
        subscriber.onNext("杨影枫");
        subscriber.onNext("月眉儿");
        subscriber.onCompleted();
    }
});
// 通过调用 Subscriber 的方法，不断地将事件添加到任务队列中。也可以用 just 方法来实现：
Observable observable = Observable.just("杨影枫", "月眉儿");

// 上述代码会依次调用 onNext("杨影枫")、onNext("月眉儿")、onCompleted()。还可以用 from 方法来实现，如下所示：
String[] words = {"杨影枫", "月眉儿"};
Observable observable = Observable.from(words);
// 上述代码调用的方法顺序和 just 方法是一样的。

// 3.Subscribe（订阅）
// 订阅只需要一行代码就可以了，如下所示：
observable.subscribe(subscriber);

// 运行代码查看log：
12-10 10:55:17.930 2398-2398/? D/MainActivity: onStart
12-10 10:55:17.930 2398-2398/? D/MainActivity: onNext杨影枫
12-10 10:55:17.930 2398-2398/? D/MainActivity: onNext月眉儿
12-10 10:55:17.931 2398-2398/? D/MainActivity: onCompleted
// 和预想的一样先调用 onStart 方法，接着调用两个 onNext方法，最后调用onCompleted方法。