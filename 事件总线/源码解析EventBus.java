/*
	1.EventBus 构造方法
	当我们要使用 EventBus 时， 首先会调用 EventBus.getDefault() 来获取 EventBus 实例。
	现在查看 getDefault 方法做了什么，如下所示：
*/
public static EventBus getDefault() {
	if (defaultInstance == null) {
		synchronized (EventBus.class) {
			if (defaultInstance == null) {
				defaultInstance = new EventBus();
			}
		}
		return defaultInstance;
	}
}
/*
	很明显这是一个单例模式，采用了双重检查模式（DCL）：double click lock。
	接下来查看 EventBus 的构造方法做了什么：
*/
public EventBus() {
	this(DEFAULT_BUILDER);
}
/*
	这里DEFAULT_BUILDER是默认的EventBusBuilder，用来构造EventBus：
*/
private static final EventBusBuilder DEFAULT_BUILDER = new EventBusBuilder();

/*
	this 调用了 EventBus 的另一个构造方法，如下所示：
*/
EventBus(EventBusBuilder builder) {
	subscriptionsByEventType = new HashMap<>();
	typesBySubscriber = new HashMap<>();
	stickyEvents = new ConcurrentHashMap<>();
	mainThreadPoster = new HandlerPoster(this, Looper.getMainLooper(), 10);
	bacjgroundPoster = new BackgroundPoster(this);
	indexCount = builder.subscriberInfoIndexes != null ? builder.subscriberInfoIndexes.size() : 0;
	subscriberMethodFinder = new subscriberMethodFinder(builder.subscriberInfoIndexes, builder.strictMethodVerification, builder.ignoreGenerateIndex);
	logSubscriberExceptions = builder.sendSubscriberExceptions;
	logSubscriberMessages = builder.logSubScriberMessages;
	sendSubscriberExceptionEvent = builder.sendSubscriberExceptionEvent;
	sendNoSubscriberEvent = builder.throwSubscriberException;
	eventInheritance = builder.eventInheritance;
	executorService = builder.executorService;
}


