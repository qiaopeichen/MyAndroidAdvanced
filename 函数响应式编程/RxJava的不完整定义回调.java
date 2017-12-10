/*
	前面介绍了回调的接收主要依赖于 subscribe(Observer) 和 subscribe(Subscriber)。
	此外，RxJava 还提供了另一种回调方式，也就是不完整回调。在讲到不完整回调之前我们首先要了解 Action。
	查看 RxJava 源码，我们发现其提供了一堆Action。
*/

// 	我们打开 Action0 来看看：
public interface Action0 extends Action {
	void call();
}

// 再打开 Action1：
public interface Action1<T> extends Action {
	void call(T t);
}

// 最后看看 Action9:
public interface Action9<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends Action {
	void call(T1 t1,T2 t2 , T3 t3, T4 t4 , T5 t5 , T6 t6, T7 t7, T8 t8, T9 t9);
}

// 很明显，Action 后的数字代表回调的参数类型数量，创建 Observer 和订阅代码也就可以改写为下面的代码：
Action1<String> onNextAction = new Action1<String>() {
    @Override
    public void call(String s) {
        Log.d(TAG, "onNext" + s);
    }
};
Action1<Throwable> onErrorAction = new Action1<Throwable>() {
    @Override
    public void call(Throwable throwable) {
              
    }
};

Action0 onCompletedAction = new Action0() {
    @Override
    public void call() {
        Log.d(TAG, "onCompleted");
    }
};

observable.subscribe(onNextAction, onErrorAction, onCompletedAction);

// 我们定义了 onNextAction 来处理 onNext 的回调，同理，我们还定义了 onErrorAction 和 onCompletedAction 来分别处理 onError 和 onCompleted 的回调，最后我们把它们传给 subscribe 方法。
// 很显然这样写的灵活度更大一些；同时我们也可以只传一个或者两个 Action，如下所示：
observable.subscribe(onNextAction);
observable.subscribe(onNextAction, onErrorAction);

// 第一行只定义了 onNextAction 来处理 onNext的回调；而第二行则定义了 onNextAction 处理 onNext 的回调，onErrorAction 来处理 onError 的回调。