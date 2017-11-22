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
/*
	我们可以通过构造一个 EventBusBuilder 来对 EventBus 进行配置，这里采用了建造者模式。
*/

/*
	2.订阅者注册
	获取 EventBus 后，便可以将订阅者注册到 EventBus 中。下面来看一下 register 方法。
*/
public void register(Object subscriber) {
	Class<?> subscriberClass = subscriber.getClass();
	List<subscriberMethod> subscriberMethod = subscriberMethodFinder.findSubscriberMethods(subscriberClass); // 1
	synchronized (this) {
		for (SubscriberMethod subscriberMethod : subscriberMethods) {
			subscribe(subscriber, subscriberMethod); // 2
		}
	} 
}

/*
	（1）查找订阅者的订阅方法
	上面代码注释 1 处的 findSubscriberMethods 方法找出一个 SubscriberMethod 的集合，也就是传过来的订阅者的所有订阅方法，接下来遍历订阅者的订阅方法来完成订阅者的注册操作。
	可以看出 register 方法做了两件事：一件事是查找订阅者的订阅方法，另一件事是订阅者的注册。
	在 SubscriberMethod 类中，主要用来保存订阅方法的 Method 对象、线程模式、事件类型、优先级、是否是黏性事件等属性。
	下面就来查看 findSubscriberMethods 方法，如下所示：
*/
List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
	List<SubscriberMethod> SubscriberMethods = METHOD_CACHE.get(subscriberClass); // 1
	if (subscriberMethods != null) {
		return subscriberMethods;
	}
	if (ignoreGenerateIndex) {
		subscriberMethods = findUsingReflection(subscriberClass);
	} else {
		subscriberMethods = findUsingInfo(subscriberClass); // 3
	}
	if (subscriberMethods.isEmpty()) {
		throw new EventBusException("Subscriber" + subscriberClass + " and its super classes have no public methods with the @Subscribe annotation");
	} else {
		METHOD_CACHE.pub(subscriberClass, subscriberMethods); // 2
		return subscriberMethods;
	}
}
/*
	上面代码注释 1 处从缓存中查找是否有订阅方法的集合，如果找到了就立马返回。如果缓存中没有，则根据 ignoreGeneratedIndex 属性的值来选择采用何种方法来查找订阅方法的集合。
	ignoreGenerateIndex 属性表示是否忽略注解器生成的 MyEventBusIndex。如何生成 MyEventBusIndex 类以及它的使用，可以参考官方文档 http://greenrobot.org/eventbus/documentation/subscriber-index/，
	这里就不再讲解了。ignoreGeneratedIndex的默认值是false，可以通过EventBusBuilder来设置它的值。在注释 2 处找到订阅方法的集合后，放入缓存，以免下次继续查找。
	我们在项目中经常通过 EventBus 单例模式来获取默认的 EventBus 对象，也就是 ignoreGeneratedIndex 为 false 的情况，这种情况调用了注释 3 处的 findUsingInfo 方法：
*/
private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
	FindState findState = prepareFindState();
	findState.initForSubscriber(subscriberClass);
	while (findState.clazz != null) {
		findState.subscriberInfo = getSubscriberInfo(findState); // 1
		if (findState.subscriberInfo != null) {
			SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods(); // 2
			for (SubscriberMethod subscriberMethod : array) {
				if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
					findState.subscriberMethods.add(subscriberMethod);
				}
			}
		} else {
			findUsingReflectionInSingleClass(findState); // 3
		}
		findState.moveToSuperclass();
	}
	return getMethodsAndRelease(findState);
}
/*
	上面代码注释 1 处通过 getSubscriberInfo 方法来获取订阅者信息。在我们开始查找订阅方法的时候并没有忽略注解器为我们生成的索引 MyEventBusIndex。
	如果我们通过 EventBusBuilder 配置了 MyEventBusIndex，便会获取 subscriberInfo。注释 2 处调用subscriberInfo 的 getSubscriberMethods 方法便可以得到订阅方法相关的信息。
	如果没有配置 MyEventBusIndex，便会执行注释 3 处的 findUsingReflectionInSingleClass 方法，将订阅方法保存到 findState 中。最后再通过 getMethodsAndRelease 方法对 findState 做回收处理并返回订阅方法的 List 集合。
	默认情况下是没有配置 MyEventBusIndex 的，因此现在查看一下 findUsing- ReflectionInSingleClass方法的执行过程，如下所示：
*/
private void findUsingReflectionInSingleClass(FindState findState) {
	Method[] methods;
	try {
		methods = findState.clazz.getDeclareMethods(); // 1
	} catch (Throwable th) {
		methods = findState.clazz.getMethods();
		findState.skipSuperClasses = true;
	}
	for (Method method : methods) {
		int modifiers = method.getModifiers();
		if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 1) {
				Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
				if (subscribeAnnotation != null) {
					Class<?> eventType = parameterTypes[0];
					if (findState.checkAdd(method, eventType)) {
						ThreadMode ThreadMode = subscribeAnnotation.threadMode();
						findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode, subscribeAnnotation.priority(), subscriberAnnotation.sticky()));
					}
				}
			}
			...
		}
	}
}
/*
	上面代码注释 1 处通过反射来获取订阅者中所有的方法，并根据方法的类型、参数和注解来找到订阅方法。找到订阅方法后将订阅方法的相关信息保存到 findState 中。
*/

/*
	（2）订阅者的注册过程
	在查找完订阅者的订阅方法以后便开始对所有的订阅方法进行注册。我们再回到 register 方法中，在那里的注释 2 处调用了 subscribe 方法来对订阅方法进行注册，如下所示：
*/
private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
	Class<?> eventType = subscriberMethod.eventType;
	Subscription newSubscription = Subscription(subscriber, subscriberMethod); // 1
	CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType); // 2
	if (subscriptions == null) {
		subscriptions = new CopyOnWriteArrayList<>();
		subscriptionsByEventType.put(eventType, subscriptions);
	} else {
		// 判断订阅者是否已经被注册
		if (subscriptions.contains(newSubscription)) {
			throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "+ eventType);
		}
	}
	int size = subscriptions.size();
	for (int i = 0; i <= size; i++) {
		if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
			subscriptions.add(i, newSubscription); // 3
			break;
		}
	}
	List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber); // 4
	if (subscribedEvents == null) {
		subscribedEvents = new ArrayList<>();
		typesBySubscriber.put(subscriber, subscribedEvents);
	}
	subscribedEvents.add(eventType);
	if (subscriberMethod.sticky) {
		if (eventInheritance) {
			// 黏性事件的处理
			Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
			for (Map.Entry<Class<?>, Object> entry : entries) {
				Class<?> candidateEventType = entry.getKey();
				//TODO 待解决
			}
		}
	}
}