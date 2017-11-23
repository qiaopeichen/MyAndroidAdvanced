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
				if (eventType.isAssignableFrom(candidate)) {
					Object stickyEvents = entry.getValue();
					checkPostStickyEventToSubscription(new Subscription, stickyEvent);
				}
			}
		} else {
			Object stickyEvent = stickyEvents.get(eventType);
			checkPostStickyEventToSubscription(new Subscription, stickyEvent);
		}
	}
}
/*
	首先，上面代码注释 1 处会根据 subscriber（订阅者）和 subscriberMethod（订阅方法）创建一个 Subscription（订阅对象）。
	注释 2 处根据 eventType（事件类型）获取 Subscriptions（订阅对象集合）。如果 Subscriptions 为 null 则重新创建，并将 Subscriptions 根据eventType保存在 subscriptionsByEventType（Map集合）。
	注释 3 处按照订阅方法的优先级插入到订阅对象集合中，完成订阅方法的注册。
	注释 4 处通过 subscriber 获取 subscribedEvents（事件类型集合）。如果 subscribedEvents 为 null 则重新创建，并将 eventType 添加到 subscribedEvents 中，并根据 subscriber 将 subscribedEvents 存储在 typesBySubscriber（Map集合）。
	如果是黏性事件，则从 stickyEvents 事件保存队列中取出该事件类型的事件发送给当前订阅者。
	总结一下，subscribe 方法主要就是做了两件事：一件事是将 Subscriptions 根据 eventType 封装到 subscriptionsByEventType 中，将 subscribedEvents 根据 subscriber 封装到 typesBySubscriber 中；
	第二件事就是对黏性事件的处理。
*/

/*
	3.事件的发送
	在获取 EventBus 对象以后，可以通过 post 方法来进行对事件的提交。post 方法的源码如下：
*/
public void post(Objcet event) {
	// PostingThreadState 保存着事件队列和线程状态信息
	PostingThreadState postingState = currentPostingThreadState.get();
	// 获取事件队列，并将当前事件插入事件队列
	List<Object> eventQueue = postingState.eventQueue;
	eventQueue.add(event);
	if (!postingState.isPosting) {
		postingState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
		postingState.isPosting = true;
		if (postingState.canceled) {
			throw new EventBusException("Internal error.Abort state was not reset");
		}
		try {
			// 处理队列中的所有事件
			while (!eventQueue.isEmpty()) {
				postSingleEvent(eventQueue.remove(0), postingState);
			} 
		} finally {
			postingState.isPosting = false;
			postingState.isMainThread = false;
		}
	}
}

/*
	首先从 PostingThreadState 对象中取出事件队列，然后再将当前的事件插入事件队列。最后将队列中的事件依次交由 postSingleEvent 方法进行处理，并移除该事件。
	之后查看 postSingleEvent 方法里做了什么：
*/
private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
	Class<?> eventClass = event.getClass();
	boolean subscriptionFound = false;
	// eventInheritance 表示是否向上查找事件的父类，默认为true
	if (eventInheritance) {
		List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
		int countTypes = eventTypes.size();
		for (int h = 0; h < countTypes.size(); h++) {
			Class<?> clazz = eventTypes.get(h);
			subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
		}
	} else {
		subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
	}
	// 找不到该事件时的异常处理
	if (!subscriptionFound) {
		if (logNoSubscriberMessage) {
			Log.d(TAG, "No subscribers registered for event " + eventClass);
		}
		if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class && eventClass != SubscriberExceptionEvent.class) {
			post(new NoSubscriberEvent(this, event));
		}
	}
}

/*
	eventInheritance 表示是否向上查找事件的父类，它的默认值为true，可以通过在 EventBusBuilder 中进行配置。当 eventInheritance 为true 时，则通过 lookupAllEventTypes 找到所有的父类事件并存在 List 中，
	然后通过 postSingleEventForEventType 方法对事件逐一处理。postSingleEventForEventType 方法的源码如下所示：
*/
private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> && eventClass) {
	CopyOnWriteArrayList<Subscription> subscriptions;
	synchronized (this) {
		subscriptions = subscriptionsByEventType.get(eventClass); // 1
	}
	if (subscriptions != null && !subscriptions.isEmpty()) {
		for (Subscription subscription : subscriptions) { // 2
			postingState.event = event;
			postingState.subscription = subscription;
			boolean aborted = false;
		}
		try {
			postToSubscription(subscription, event, postingState.isMainThread);
			aborted = postingState.canceled;
		} finally {
			postingState.event = null;
			postingState.subscription = null;
			postingState.canceled = false;
		}
		if (aborted) {
			break;
		}
		return true;
	}
	return false;
}

/*
	上面代码注释 1 处同步取出该事件对应的 Subscriptions（订阅对象集合）。注释 2 处遍历 Subscriptions，将事件 event 和对应的 Subscription（订阅对象）传递给 postingState 并调用 postToSubscription 方法对事件进行处理。
	接下来查看 postToSubscription 方法：
*/
private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
	switch (subscription.subscriberMethod.threadMode) {
		case POSTING:
			invokeSubscriber(subscription, event);
			break;
		case MAIN:
			if (isMainThread) {
				invokeSubscriber(subscription, event);
			} else {
				mainThreadPoster.enqueue(subscription, event);
			}
			break;
		case BACKGROUND:
			if (isMainThread) {
				backgroundPoster.enqueue(subscription, event);
			} else {
				invokeSubscriber(subscription, event);
			}
			break;
		case ASYNC:
			asyncPoster.enqueue(subscription, event);
			break;
		default:
			throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
	}
}
/*
	取出订阅方法的 threadMode（线程模式），之后根据 threadMode 来分别处理。如果 threadMode 是 MAIN，若提交事件的线程是主线程，则通过反射直接运行订阅的方法；
	若其不是主线程，则需要 mainThreadPoster 将我们的订阅事件添加到主线程队列中。 mainThreadPoster 是 HandlePoster 类型的，继承自 Handler，通过 Handler 将订阅方法切换到主线程执行。
*/

/*
	4.订阅者取消注册
	取消注册则需要调用 unregister 方法，如下所示：
*/
public synchronized void unregister(Object subscriber) {
	List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber); // 1
	if (subscribedTypes != null) {
		for (Class<?> eventType : subscribedTypes) {
			unsubscribeByEventType(subscriber, eventType); // 2
		}
		typesBySubscriber.remove(subscriber); // 3
	} else {
		Log.w(TAG, "Subscriber to unregister was not registered before:" + subscriber.getClass());
	}
}
/*
	我们在订阅者注册的过程中讲到过 typesBySubscriber，它是一个 map 集合。上面代码注释 1 处通过 subscriber 找到 subscribedTypes（事件类型集合）。
	注释 3 处将 subscriber 对应的 eventType 从 typesBySubscriber 中移除。注释 2 处遍历 subscribedTypes，并调用 unsubscribeByEventType 方法：
*/
private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
	List<Subscription> subscriptions = subscriptionsByEventType.get(eventType); // 1
	if (subscriptions != null) {
		int size = subscriptions.size();
		for (int i = 0; i < size; i++) {
			Subscription subscription = subscriptions.get(i);
			if (subscription.subscriber == subscriber) {
				subscription.active = false;
				subscriptions.remove(i);
				i--;
				size--;
			}
		}
	}
}
/*
	上面代码注释 1 处通过 eventType 来得到对应的 Subscriptions（订阅对象集合），并在 for 循环中判断如果 Subscriptions（订阅对象）的 subscriber（订阅者）属性等于传进来的 subscriber，
	则从 Subscriptions 中移除该 Subscription。EventBus的源码就讲到这里了。
*/