/*
	RxJava操作符的类型分为创建操作符、变换操作符、过滤操作符、组合操作符、错误处理操作符、辅助操作符、条件和布尔操作符、算术和聚合操作符及连接操作符等，
	而这些操作符类型下又有很多操作符，每个操作符可能还有很多变体。因为篇幅有限，这里只介绍相对常用的操作符，以及这些操作符的常规用法。
	官方文档：http://reactivex.io/RxJava/javadoc/。
*/

/*
	创建操作符：creat/just/from/defer/range/interval/start/repeat/timer等	
*/

// 1.interval
// 创建一个按固定时间间隔发射整数序列的 Observable，相当于定时器，如下所示：
Observable.interval(3, TimeUnit.SECONDS)
        .subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                Log.d(TAG, "interval:" + aLong.intValue());
            }
        });
// 上面的代码每隔 3s 就会调用 call 方法并打印 Log。

// 2.range
// 创建发射指定范围的整数序列的 Observable，可以拿来替代 for 循环，发射一个范围内的有序整数序列。第一个参数是起始值，并且不小于0；第二个参数为整数序列的个数。
Observable.range(0, 5)
        .subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.d(TAG, "range:" + integer.intValue());
            }
        });

// 输出结果为：
12-11 19:19:59.522 11016-11016/? D/MainActivity: range:0
12-11 19:19:59.522 11016-11016/? D/MainActivity: range:1
12-11 19:19:59.522 11016-11016/? D/MainActivity: range:2
12-11 19:19:59.522 11016-11016/? D/MainActivity: range:3
12-11 19:19:59.522 11016-11016/? D/MainActivity: range:4

// 3.repeat
// 创建一个N次重复发射特定数据的Observable，如下所示：
Observable.range(0, 3)
        .repeat(2)
        .subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.d(TAG, "repeat:" + integer.intValue());
            }
        });

// 输出结果为：
12-11 19:24:32.610 12860-12860/? D/MainActivity: repeat:0
12-11 19:24:32.610 12860-12860/? D/MainActivity: repeat:1
12-11 19:24:32.610 12860-12860/? D/MainActivity: repeat:2
12-11 19:24:32.611 12860-12860/? D/MainActivity: repeat:0
12-11 19:24:32.611 12860-12860/? D/MainActivity: repeat:1
12-11 19:24:32.611 12860-12860/? D/MainActivity: repeat:2

/*
	变换操作符
	变换操作符的作用是对 Observable 发射的数据按照一定规则做一些变换操作，然后将变换后的数据发射出去。
	变换操作符有 map/flatMap/concatMap/switchMap/flatMapIterable/buffer/groupBy/cast/window/scan等。
*/

// 1.map
// map操作符通过指定一个 Func 对象，将 Observable 转换为一个新的 Observable 对象并发射，观察者将收到新的 Observable 处理。
// 假设我们要访问网络，Host 地址时常是变化的，它有时是测试服务器地址，有时可能是正式服务器地址，但是具体界面的 URL 地址则是不变的。因此，我们可以用 map 操作符来进行转换字符操作，如下所示：
final String Host = "http://blog.csdn.net/";
Observable.just("itachi85").map(new Func1<String, String>() {
    @Override
    public String call(String s) {
        return Host + s;
    }
}).subscribe(new Action1<String>() {
    @Override
    public void call(String s) {
        Log.d(TAG, "map:" + s);
    }
});
// 输出结果为：
12-11 19:34:48.406 18780-18780/? D/MainActivity: map:http://blog.csdn.net/itachi85

// flatMap、cast
/*
	flatMap 操作符将 Observable 发射的数据集合变换为 Observable 集合，然后将这些 Observable 发射的数据平坦化地放进一个单独的 Observable。
	cast 操作符的作用是强制将 Observable 发射的所有数据转换为指定类型。假设我们仍旧访问网络，但是要访问同一个 Host 的多个界面，我们可以使用 for 循环在每个界面的 URL 前添加 Host。
	但是 RxJava 提供了一个更方便的操作，如下所示：
*/
Observable.from(mlist).flatMap(new Func1<String, Observable<?>>() {
    @Override
    public Observable<?> call(String s) {
        return Observable.just(Host + s);
    }
}).cast(String.class).subscribe(new Action1<String>() {
    @Override
    public void call(String s) {
        Log.d(TAG, "flatMap:" + s);
    }
});

// 首先用 ArrayList 存储要访问的界面 URL，然后通过 flatMap 转换成 String 类型。输出结果为：
12-11 20:23:51.786 14728-14728/com.example.qiaopc.myapplication D/MainActivity: flatMap:http://blog.csdn.net/itachi85
12-11 20:23:51.787 14728-14728/com.example.qiaopc.myapplication D/MainActivity: flatMap:http://blog.csdn.net/itachi86
12-11 20:23:51.787 14728-14728/com.example.qiaopc.myapplication D/MainActivity: flatMap:http://blog.csdn.net/itachi87
12-11 20:23:51.787 14728-14728/com.example.qiaopc.myapplication D/MainActivity: flatMap:http://blog.csdn.net/itachi88

// 另外，flatMap 的合并允许交叉，也就是说可能会交错地发送事件，最终结果的顺序可能并不是原始 Observable 发送时的顺序。

// 3.concatMap
/*
	concatMap 操作符功能与 flatMap 操作符一致；
	不过，它解决了 flatMap 交叉问题，提供了一种能够把发射的值连续在一起的函数，而不是合并它们。
	concatMap 的使用方法和 flatMap 类似，这里就不重复贴代码了。
*/

// 4.flatMapIterable
// flatMapIterable 操作符可以将数据包装成 Iterable，在 Iterable 中我们就可以对数据进行处理了，如下所示：
Observable.just(1, 2, 3).flatMapIterable(new Func1<Integer, Iterable<Integer>>() {
    @Override
    public Iterable<Integer> call(Integer s) {
        List<Integer> mlist = new ArrayList<Integer>();
        mlist.add(s + 1); // 1
        return mlist;
    }
}).subscribe(new Action1<Integer>() {
    @Override
    public void call(Integer integer) {
        Log.d(TAG, "flatMapIterable:" + integer);
    }
});

//在上面代码注释 1 处对每个数都加 1，因此输出结果为：
12-11 20:38:10.161 22800-22800/? D/MainActivity: flatMapIterable:2
12-11 20:38:10.161 22800-22800/? D/MainActivity: flatMapIterable:3
12-11 20:38:10.161 22800-22800/? D/MainActivity: flatMapIterable:4

// 5.buffer
// buffer 操作符将 Observable 变换为一个新的 Observable，这个新的 Observable 每次发射一组列表而不是一个一个发射。和 buffer 操作符类似的还有 window 操作符，只不过 window 操作符发射的是 Observable 而不是数据列表，如下所示：
Observable.just(1, 2, 3, 4, 5, 6)
        .buffer(3)
        .subscribe(new Action1<List<Integer>>() {
            @Override
            public void call(List<Integer> integers) {
                for (Integer i : integers) {
                    Log.d(TAG, "buffer:" + i);
                }
                Log.d(TAG, "----------------");
            }
        });

// buffer(3) 的意思是缓存容量为 3，输出结果为：
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: buffer:1
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: buffer:2
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: buffer:3
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: ----------------
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: buffer:4
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: buffer:5
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: buffer:6
12-11 20:48:48.764 28908-28908/com.example.qiaopc.myapplication D/MainActivity: ----------------

// 6.groupBy
// groupBy 操作符用于分组元素，将源 Observable 变换成一个发射 Observables 的新 Observable（分组后的）。它们中的每一个新 Observable 都发射一组指定的数据，如下所示：
Swordsman s1 = new Swordsman("韦一笑"， "A");
Swordsman s2 = new Swordsman("张三丰"， "SS");
Swordsman s3 = new Swordsman("周芷若"， "S");
Swordsman s4 = new Swordsman("宋远桥"， "S");
Swordsman s5 = new Swordsman("殷梨亭"， "A");
Swordsman s6 = new Swordsman("张无忌"， "SS");
Swordsman s7 = new Swordsman("鹤笔翁"， "S");
Swordsman s8 = new Swordsman("宋青书"， "A");
Observable<GroupedObservable<String, Swordsman>> GroupedObservable = Observable.just(s1, s2, s3, s4, s5, s6, s7, s8)
																			.groupBy(new Func1<Swordsman, String>() {
																				@Override
																				public String call(Swordsman Swordsman) {
																					return Swordsman.getLevel();
																				}
																			});
Observable.concat(GroupedObservable).subscribe(new Action1<Swordsman>() {
	@Override
	public void call(Swordsman swordsman) {
		Log.d(TAG, "groupBy:" + swordsman.getName() + "---" + swordsman.getLevel());
	}
});
// 这里创建了《倚天屠龙记》里的 8 个武侠，对其按照实力等级进行划分，从高到低依次是 SS、S、A。使用 groupBy可以帮助我们对某一个 key 值进行分组，将相同的 key 值数据排在一起。在这里我们的 key 值就是武侠的 Level（实力等级）。
// 其中 concat 是组合操作符，后面会介绍它。
// 输出结果为：
韦一笑---A
殷梨亭---A
宋青书---A
张三丰---SS
张无忌---SS
周芷若---S
宋远桥---S
鹤笔翁---S

