/*
	首先来看一下 otto 源码各个类的作用：
	Produce、Subscribe：发布者和订阅者注解类。
	Bus：事件总线类，用来注册和取消注册，维护发布-订阅模型，并处理事件调度分发。
	HandlerFinder、AnnotatedHandlerFinder：用来查找发布者和订阅者。
	EventProducer、EventHandler：分别封装发布者和订阅者的数据结构。
*/

// 1.otto 构造方法
// 在使用 otto 时，首先要创建Bus类。Bus类的构造方法如下所示：
public Bus() {
	this(DEFAULT_IDENTIFIER);
}

// 这个 DEFAULT_IDENTIFIER 是一个字符串"default"，this 调用了 Bus 类的另一个构造方法：
public Bus(String identifier) {
	this(ThreadEnforcer.MAIN, identifier);
}

// ThreadEnforcer.MAIN 意味着默认在主线程中调度事件。再往里看 this 又调用了什么，如下所示：
public Bus(ThreadEnforcer enforcer, String identifier) {
	this(enforcer, identifier, HandlerFinder.ANNOTATED);
}

// 第一个参数我们提到了，就是事件调度的简称，identifier 为 Bus 的名称，默认是"default"。而第三个参数则是HandlerFinder，用于在 register/unregister 的时候寻找所有的 subscriber 和 producer。再往里查看this又调用了什么，如下所示：
Bus(ThreadEnforcer enforcer, String identifier, HandlerFinder handlerFinder) {
	this.enforcer = enforcer;
	this.identifier = identifier;
	this.handlerFinder = handlerFinder;
}
// 这是最终调用的 Bus 的构造方法，在这里要首先记住 handlerFinder 指的就是传进来的 HandlerFinder.ANNOTATED，后面在注册时会用到 handlerFinder 这个属性。

// 2 注册
// 生成 Bus 类后，我们要调用它的 register 方法来进行注册。register 方法如下所示：
public void register(Object object) {
	if (object == null) {
		throw new NullPointerException("Object to register must not be null.");
	}
	enforcer.enforce(this);
	Map<Class<?>, EventProducer> foundProducers = handlerFinder.findAllProducers(object); // 1
	...
}
// 上面代码注释 1 处调用了 handlerFinder 的 findAllProducers 方法。此前讲到构造方法时，我们知道该 handlerFinder 指的是 HandlerFinder.ANNOTATED。ANNOTATED 的代码如下所示：
HandlerFinder ANNOTATED = new HandlerFinder() {
	@Override
	public Map<Class<?>, EventProducer> findAllProducers(Object listner) {
		return AnnotatedHandlerFinder.findAllProducers(listener);
	}
	@Override
	public Map<Class<?>, Set<EventHandler>> findAllSubscribers(Object listener) {
		return AnnotatedHandlerFinder.findAllSubscribers(listener);
	}
}

// 从上面代码的 findAllProducers 方法和 findAllSubscribers 方法的返回值可以推断出一个注册类只能有一个发布者，但却可以有多个订阅者。findAllProducers 方法最终调用的是 AnnotatedHandlerFinder 的 findAllProducers 方法：
static Map<Class<?>, EventProducer> findAllProducers(Object listener) {
	final Class<?> listenerClass = listener.getClass();
	Map<Class<?>, EventProducer> handlersInMethod = new HashMap<Class<?>, EventProducer>();
	Map<Class<?>, Method> methods = PRODUCERS_CACHE.get(listenerClass); // 1
	if (null == methods) {
		methods = new HashMap<Class<?>, Method>();
		loadAnnotatedProducerMethods(listenerClass, methods); // 2
	}
	if (!methods.isEmpty()) {
		for (Map.Entry<Class<?>, Method) e : methods.entrySet()) { // 3
			EventProducer producer = new EventProducer(listener, e.getValue());
			handlersInMethod.put(e.getKey(), producer);
		}
	}
	return handlersInMethod;
}

/* 
	PRODUCERS_CACHE 是一个 ConcurrentHashMap，它的key为 bus.register() 时传入的class，而 value 是一个 map，这个 map 的 key 是事件的 class，value 是生产事件的方法。
	上面代码注释 1 处首先在 PRODUCERS_CACHE 根据传入的对象的类型查找是否有缓存的事件方法。如果没有，就调用注释 2 处的代码利用反射去寻找所有使用了 @Produce 注解的方法，并且将结果缓存到 PRODUCE_CACHE 中。
	接着在注释 3 处遍历这些事件方法，并为每个事件方法创建 EventProducer 类，将这些 EventProducer 类作为 value 存入 handlersInMethod 并返回。接下来我们返回查看 register方法：
*/
public void register(Object object) {
	if (object == null) {
		throw new NullPointerException("Object to register must not be null.");
	}
	enforcer.enforce(this);
	Map<Class<?>, EventProducer> foundProducers = handlerFinder.findAllProducers(object);
	for (Class<?> type : foundProducers.keySet()) {
		final EventProducer producer = foundProducers.get(type);
		EventProducer previousProducer = producersByType.putIfAbsent(type, producer); // 1
		if (previousProducer != null) {
			throw new IllegalArgumentException("Producer method for type " + type + " found on type " + producer.target.getClass() + ", but already registered by type " + previousProducer.target.getClass() + ".");
		}
		Set<EventHandler> handlers = handlersByType.get(type);
		if (handlers != null && !handlers.isEmpty()) {
			for (EventHandler handler : handlers) {
				dispatchProducerResultToHandler(handler, producer); // 2
			}
		}
	}
	...
}
/*
	调用完 findAllProducers 方法后，会在上面代码注释 1 处检查是否有该类型的发布者已经存在。如果存在，则抛出异常；如果不存在，则调用注释 2 处的 dispatchProducerResultToHandler 方法来触发和发布者对应的订阅者以处理事件。
	接下来 register 方法的后一部分代码就补贴上来了。这根此前的流程大致一样，就是调用 findAllSubscribers 方法来查找所有使用了 @Subscribe 注解的方法。跟此前不同的是，一个注册类可以有多个订阅者。
	随后判断是否有该类型的订阅者存在，也就是判断注册类是否已经注册，如果存在，则抛出异常；如果不存在，则查找是否有和这些订阅者对应的发布者，如果有的话，就会触发对应的订阅者处理事件。
*/

// 3.发送事件
// 我们会调用 Bus 的 post 方法来发送事件，它的代码如下所示：
public void post(Object event) {
	if (event == null) {
		throw new NullPointerException("Event to post must not be null.");
	}
	enforcer.enforce(this);
	Set<Class<?> dispatchTypes = flattenHierarchy(event.getClass()); // 1
	boolean dispatched = false;
	for (Class<?> eventType : dispatchTypes) {
		Set<EventHandler> wrappers = getHandlersForEventType(eventType);
		if (wrappers != null && !wrappers.isEmpty()) {
			dispatched = true;
			for (EventHandler wrapper : wrappers) {
				enqueueEvent(event, wrapper); // 2
			}
		}
	}
	if (!dispatched && !(event instanceof DeadEvent)) {
		post(new DeadEvent(this, event));
	}
	dispatchQueuedEvents(); // 3
}

/*
	上面代码注释 1 处的 flattenHierarchy 方法首先会从缓存中查找传进来的 event（消息事件类）的所有父类，如果没有则找到 event 的所有父类并存储入缓存中。
	接下来遍历这些父类，找到它们的所有订阅者，并在注释 2 处将这些订阅者压入线程的事件队列中。在注释 3 处调用 dispatchQueuedEvents 方法，依次取出事件队列中的订阅者来处理相应 event 的事件。
*/

// 4 取消注册
// 取消注册时，我们会调用 Bus 的 unregister 方法。unregister 方法如下所示：
public void unregister(Object object) {
	if (object == null) {
		throw new NullPointerException("Object to unregister must not be null.");
	}
	enforcer.enforce(this);
	Map<Class<?>, EventProducer> producersInListener = handlerFinder.findAllProducers(object); // 1
	for (Map.Entry<Class<?>, EventProducer> entry : producersInListener.entrySet()) {
		final Class<?> key = entry.getKey();
		EventProducer producer = getProducerForEventType(key);
		EventProducer value = entry.getValue();
		if (value == null || !value.equals(producer)) {
			throw new IllegalArgumentException("Missing event producer for an annotated method. Is " + object.getClass() + " registered?");
		}
		producersByType.remove(key).invalidate(); // 2
	}
	...
} 
/*
	取消注册分为两部分：一部分是订阅者取消注册，另一部分是发布者取消注册。
	这两部分的代码类似，因此，上面的代码只列出了发布者取消注册的代码。在上面代码注释 1 处得到所有使用 @Produce 注解的方法，并遍历这些方法；
	调用注释 2 处的代码从缓存中清除所有和传进来的注册类相关的发布者，以完成发布者的取消注册操作。
*/