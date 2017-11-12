//1.OKHttp的请求网络流程
@Override
public Call newCall(Request request) {
	return RealCall(this, request);
}

//RealCall方法：
void enqueue(Callback responseCallback, boolean forWebSocket) {
	synchronized (this) {
		if (executed) throw new IllegalStateException("Already Executed");
		executed = true;
	}
	client.dispatcher().enqueue(new AsyncCall(responseCallback, forWebSocket));
}

//分析dispatcher：
/** 最大并发请求数 */
private int maxRequests = 64;
/** 每个主机的最大请求数 */
private int maxRequestsPerHost = 5;
/** 消费者线程池 */
private ExecutorService executorService;
/** 将要运行的异步请求队列 */
private final Deque<AsyncCall> readyAsyncCalls = new ArrayDeque<>();
/** 正在运行的异步请求队列 */
private final Deque<AsyncCall> runningAsyncCalls = new ArrayDeque<>();
/** 正在运行的同步请求队列 */
private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();

//Dispatcher的构造方法
public Dispatcher(ExecutorService executorService) {
	this.executorService = executorService;
}
public Dispatcher() {
}
public synchronized ExecutorService executorService() {
	if (executorService == null) {
		executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), Util.threadFactory(OkHttp Dispatcher", false));
	}
	return executorService;
}


//dispatcher的enqueue方法：
synchronized void enqueue(AsyncCall call) {
	if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
		runningAsyncCalls.add(call);
		executorService().execute(call);
	} else {
		readyAsyncCalls.add(call);
	}
}

//AsyncCall内部实现的execute方法：
protected void execute() {
	boolean signalledCallback = false;
	try {
		...
	} catch (IOException e) {
		...
	} finally {
		client.dispatcher().finished(this);
	}
}

//finished方法：
synchronized void finished(AsyncCall call) {
	if (!runningAsyncCall.remove(call)) throw new AssertionError("AsyncCall wasn't running !");
	promoteCalls();
}

//promoteCalls方法：
private void promoteCalls() {
	if (runningAsyncCalls.size() >= maxRequests) return;
	if (readyAsyncCalls.isEmpty()) return;
	for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext();) {
		AsyncCall call = i.next();
		
		if (runningCallsForHost(call) < maxRequestsPerHost) {
			i.remove();
			runningAsyncCalls.add(call);
			executorService().execute(call);
		}
		if (runningAsyncCall.size() >= maxRequests) return;
	}
}

//AsyncCall的execute方法：
@Override void execute() {
	boolean signalledCallback = false;
	try {
		Response response = getResponseWithInterceptorChain(forWebSocket); //请求网络返回了response
		if (canceled) {
			sginalledCallback = true;
			responseCallback.onFailure(RealCall.this, new IOException("Canceled"))'
		} else {
			signalledCallback = true;
			responseCallback.onResponse(RealCall.this, response);
		}
	} catch (IOException e) {
		if (signalledCallback) {
			loggor.log(Level.INFO, "Callback failure for" + toLoggableString(), e);
		} else {
			responseCallback.onFailure(RealCall.this, e);
		}
	}finally {
		client.dispatcher().finished(this);
	}
}


//Interceptor拦截器
getResponseWithInterceptorChain方法：
private Response getResponseWithInterceptorChain(boolean forWebSocket) throws IOException {
	Interceptor.Chain chain = new ApplicationInterceptorChain(0, originalRequest, forWebSocket);
	return chain.proceed(originalRequest);
}

//ApplicationInterceptorChain的proceed方法：
publc Response proceed(Request request) throws IOException {
	if(index < client.interceptors().size()) {
		Interceptor.Chain chain = new ApplicationInterceptorChain(index + 1, request, forWebSocket);
		// 从拦截器列表中取出拦截器
		Interceptor interceptor = client.interceptors().get(index);
		Response interceptedResponse = interceptor.intercept(chain); // 1
		if (interceptedResponse == null) {
			throw new NullPointerException("application interceptor" + interceptor + " returned null");
		}
		return interceptedResponse;
	}
	return getResponse(request, forWebSocket);
}

//getResponse方法：
Response getResponse(Request request, boolean forWebSocket) throws IOException {
	...
	engine = new HttpEnginee(client, request, false, false, forWebSocket, null, null, null);
	int followUpCount = 0;
	while (true) {
		if (canceled) {
			engine.releaseStreamAllocation();
			throw new IOException("Canceled");
		}
		boolean releaseConnection = true;
		try {
			engine.sendRequest();
			engine.readResponse();
			releaseConnection = false();
		} catch (RequestException e) {
			throw e.getCause();
		} catch (RouteException e) {
			...
		}
	}
}

//缓存策略：
//HttpEnginee的sendRequest方法：
public void sendRequest() throws RequestException,RouteException, IOException {
	if (cacheStrategy != null) return; //Already sent.
	if (httpStream != null) throw new IllegalStateException();
	Request request = networkRequest(userRequest);
	// 获取client中的Cache，同时Cache在初始化时会读取缓存目录中曾经请求过的所有信息
	InternalCache responseCache = Internal.instance.internalCache(client);
	Response cacheCandidate = responseCache != null ? responseCache.get(request) : null; // 1
	long now = System.currentTimeMillis();
	cacheStrategy = new CacheStrategy.Factory(now, request, cacheCandidate);
	get();
	// 网络请求
	networkRequest = cacheStrategy.networkRequest;
	// 缓存的相应
	cacheResponse = cacheStrategy.cacheResponse;
	if (responseCache != null) {
		// 记录当前请求是网络发起还是缓存发起
		responseCache.trackResponse(cacheStrategy);
	}
	if (cacheCandidate != null && cacheResponse == null) {
		closeQuietly(cacheCandidate.body());
	}
	// 不进行网络请求并且缓存不存在或者过期，则返回504错误
	if (networkRequest == null && cacheResponse == null) {
		userResponse = new Response.Builder()
			.request(userRequest)
			.priorResponse(stripBody(priorResponse))
			.protocol(Protocol.HTTP_1_1)
			.code(504)
			.message("Unsatisfiable Request (only-if-cached)")
			.body(EMPTY_BODY)
			.build();
		return;
	}
	// 不进行网络请求而且缓存可以使用，则直接返回缓存
	if (networkRequest == null) {
		userResponse = cacheResponse.newBuilder()
			.request(userRequest)
			.priorResponse(stripBody(priorResponse))
			.cacheResponse(stripBody(cacheResponse))
			.build();
		userResponse = unzip(userResponse);
		return;
	}
	// 需要访问网络时
	boolean success = false;
	try {
		httpStream = connect();
		httpStream.setHttpEngine(this);
		...
	}
}

//HttpEngine的readResponse方法：
public void readSponse() throws IOException {
	...
	else {
		// 读取网络响应
		networkRequest = readNetworkResponse();
	}
	receiveHeaders(networkResponse.headers());
	if (cacheResponse != null) {
		// 检查缓存是否可用。如果可用，就用当前缓存的Response，关闭网络连接，释放连接
		if (validate(cacheResponse, networkResponse)) { // 1
			userResponse = cacheResponse.newBuilder()
				.request(userRequest)
				.priorResponse(stripBody(cacheResponse))
				.headers(combine(cacheRespose.headers(), networkResponse.headers()))
				.cacheResponse(stripBody(cacheResponse))
				.networkResponse(stripBody(cacheResponse))
				.networkResponse(stripBody(networkResponse))
				.build();
			networkResponse.body().close();
			releaseStreamAllocation();
			InternalCache responseCache = Internal.instance.internalCache(client);
			responseCache.trackConditionalCacheHit();
			responseCache.update(cacheResponse, stripBody(userResponse));
			userResponse = unzip(userResponse);
			return;
		} else {
			closeQuietly(cacheResponse.body());
		}
	}
	userResponse = networkResponse.newBuilder()
		.request(userRequest)
		.priorResponse(stripBody(priorResponse))
		.cacheResponse(stripBody(cacheResponse))
		.networkResponse(stripBody(networkResponse))
		.build();
	
	if (hasBody(userRespnse)) {
		maybeCache();
		userResponse = unzip(cacheWritingResponse(storeRequest, userResponse));
	}
}

//注释1出的validate方法是如何判断缓存是否可用的
private static boolean validate(Response cached, Response network) {
	//如果服务器返回304，则缓存有效
	if (network.code() == HTTP_NOT_MODIFIED) {
		return true;
	}
	//通过缓存和网络请求相应中的Last-Modified来计算是否是最新数据。如果是，则缓存有效
	Date lastModified = cached.headers().getDate("Last-Modified");
	if (lastModified != null) {
		Date networkLastModified = network.headers().getDate("Last-Modified");
		if (networkLastModified != null &&networkLastModified.getTime() < lastModified.getTime()) {
			return true;
		}
	}
	return false;
}

//失败重连
// 最后我们再回到RealCall的getResponse方法，如下所示：
Response getResponse(Request request, boolean forWebSocket) throws IOException {
	...
	boolean releaseConnection = true;
	try {
		engine.sendRequest();
		engine.readResponse();
		releaseConnection = false;
	} catch (RequestException e) {
		throw e.getCause();
	} catch (RouteException e) {
		HttpEngine retryEngine = engine.recover(e.getLastConnectException(), null); // 1
		if (retryEngine != null) {
			releaseConnection = false;
			engine = retryEngine;
			continue;
		}
		throw e.getLastConnectException();
	} catch (IOException e) {
		HttpEngine retryEngine = engine.recover(e, null); // 2
		if (retryEngine != null) {
			releaseConnection = false;
			engine = retryEngine;
			continue;
		}
		throw e;
	} finally {
		if (releaseConnection) {
			StreamAllocation streamAllocation = engine.close();
			streamAllocation.release();
		}
	}
	...
	engine = new HttpEngine(client, request, false, false, forWebSocket, streamAllocation, null, response);
}

// 在上面代码注释1和注释2处，当发生IOException或者RouteException时都会执行HttpEngine的recover方法，它的代码如下所示：
public HttpEngine recover(IOException e, Sink requestBodyOut) {
	if (!streamAllocation.recover(e, requestBodyOut)) {
		return null;
	}
	if (!client.retryOnConnectionFailure()) {
		return null;
	}
	StreamAllocation streamAllocation = close();
	return new HttpEngine(client, userRequest, bufferRequestBody, callerWritesRequestBody, forWebSocket, streamAllocation, (RetryableSink)requestBodyOut, priorResponse);
}
// 通过最后一行可以看到，其就是重新创建了HttpEngine并返回，用来完成重连。到这里OkHttp请求网络的流程基本上讲完了。

// OkHttp的复用连接池

/*
	TCP有3次握手与4次挥手。
	为了解决TCP握手与挥手的效率问题，HTTP有一种叫做keepalive connections的机制；
	而OkHttp支持5个并发socket连接，默认keepAlive时间为5分钟。接下来我们学习OkHttp是怎么复用连接的。
*/

/*
	主要变量与构造方法：
	连接池的类位于okhttp3.ConnectionPool，它的主要变量如下所示：
*/
private static final Executor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttpConnectionPool", true));
// 空闲的socket最大连接数
private final int maxIdleConnections;
// socket的keepAlive时间
private final long keepAliveDurationNs;
private final Deque<RealConnection> connections = new ArrayDeque<>();
final RouteDatabase routeDatabase = new RouteDatabase();
boolean cleanupRunning;
/*
	主要的变量有必要说明一下：
	executor线程池，类似于CachedThreadPool, 需要注意的是这种线程池的工作队列采用了没有容量的SynchronousQueue。
	Deque，双向队列，双端队列同时具有队列和栈性质，经常在缓存中被使用，里面维护了RealConnection也就是socket物理连接的包装。
	RouteDatabase，它用来记录连接失败的路线名单，当连接失败的时候就会把失败的线路加进去。
*/

//	ConnectionPool的构造方法如下所示：
public ConnectionPool() {
	this(5, 5, TimeUnit.MINUTES);
}
public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
	this.maxIdleConnections = maxIdleConnections;
	this.keepAliveDurationNs = thisUnit.toNanos(keepliveDuration);
	if (keepAliveDuration <= 0) {
		throw new IllegalArgumentException("keepAliveDuration <= 0:" + keepAliveDuration);
	}
}

// ConnectionPool是在OkHttpClient实例化时创建的：
public OkHttpClient() {
	this (new Builder());
}

public Builder() {
	dispatcher = new Dispatcher();
	...
	connectionPool = new ConnectionPool();
	...
}

/* 
	缓存操作
	ConnectionPool提供对Deque<RealConnection>进行操作的方法分别为put、get、connectionBecameIdle和evictAll这几个操作。
	分别对应放入连接、获取连接、移除连接和移除所有连接操作。
	这里我们举例说明put和get操作。
*/
void put(RealConnection connection) {
	assert (Thread.holdsLock(this));
	if (!clernupRunning) {
		cleanupRunning = true;
		executor.execute(cleanupRunnable);
	}
	connections.add(connection);
}
// 在添加到Deque之前首先要清理空闲的线程，这个后面会讲到，再来看get操作：
RealConnection get(Address address, StreamAllocation streamAllocation) {
	assert (Thread.holdsLock(this));
	for (RealConnection connection : connections) {
		if (connection.allocations.size() < connection.allocationLimit && address.equals(connection.route().address) && !connection.noNewStreams) {
			streamAllocation.acquire(connection);
			return connection;
		}
	}
	return null;
}
// 遍历connections缓存列表。当某个连接计数的次数小于限制的大小，并且request的地址和缓存列表中此连接的地址完全匹配时，则直接复用缓存列表中的connection作为request的连接。

/*
	自动回收连接
	OkHttp是根据StreamAllocation引用计数是否为0来实现自动回收连接的。我们在put操作前首先要调用executor.execute(cleanupRunnable)来清理闲置的线程。
	查看cleanuRunnable到底做了什么：
*/
private final Runnable cleanupRunnbale = new Runnable() {
	@Override
	public void run() {
		while (true) {
			long waitNanos = cleanup(System.nanoTime());
			if (waitNanos == -1) return;
			if (waitNanos > 0) {
				long waitMillis = waitNanos / 1000000L;
				waitNanos -= (waitMillis * 1000000L);
				synchronized (ConnectionPool.this) {
					try {
						ConnectionPool.this.wait(waitMillis, (int) waitNanos);
					}
				}
			}
		}
	}
};
/*
	线程不断地调用cleanup方法来进行清理，并返回下次需要清理的间隔时间，然后调用wait方法进行等待以释放锁与时间片。
	当等待时间到了后，再次进行清理，并返回下次要清理的间隔时间。如此循环下去。接下来看看cleanup方法，如下所示：
*/
long cleanup(long now) {
	int inUseConnectionCount = 0;
	int idleConnectionCount = 0;
	RealConnection longestIdleConnection = null;
	long longestIdleDurationNs = Long.MIN_VALUE;
	synchronized (this) {
		for (Iterator<RealConnection> i = connectinos.iterator(); i.hasNext; ) {
			RealConnection connection = i.next();
			if (pruneAndGetAllocationCount(connection, now) > 0) {// 1
				inUseConnectionCount++;
				continue;
			}
			idleConnectionCount++;
			long idleDurationNs = now - connection.idleAtNanos;
			if (idleDurationNs > longestIdleDurationNs) {
				longestIdleDurationNs = idleDurationNs;
				longestIdleConnection = connection;
			}
		}
		if (longestIldeDurationNs >= this.keepAliveDurationNs || idleConnectionCount > this.maxIdleConnections) { // 2
			connections.remove(longestIdleConnection);
		} else if (idleConnectionCount > 0) {
			return keepAliveDurationNs - longestIdleDurationNs;
		} else if (inUseConnectionCount > 0) {
			return keepAliveDurationNs;
		} else {
			cleanupRunning = false;
			return -1; // 3
		}
	}
	closeQuietly(longestIdleConection.socket());
	return 0;
}
/*
	cleanup方法所做的事情简单总结就是，根据连接中的引用计数来计算空闲连接数和活跃连接数，然后标记出空闲的连接。
	在上面代码注释2处，如果空闲连接keepAlive时间超过5分钟，或者空闲连接数超过5个，则从Deque中移除此连接。
	接下来根据空闲连接或者活跃连接来返回下次需要清理的时间数：
	如果空闲连接大于0，则返回此连接即将到期的时间；
	如果都是活跃连接并且大于0，则返回默认的keepAlive时间5分钟。
	在注释3处，如果没有任何连接，则跳出循环并返回-1.
	在注释1处，通过pruneAndGetAllocationCount方法来判断连接是否闲置。如果pruneAndGetAllocationCount方法的返回值大于0则是活跃连接，否则就是空闲连接。
	接下来查看pruneAndGetAllocationCount方法，如下所示：
*/
private int pruneAndGetAllocationCount(RealConnection connection, long now) {
	List<Reference<StreamAllocation>> references = connection.allocations;
	for (int i = 0; i < references.size(); ) {
		Reference<StreamAllocation> reference = references.get(i);
		if (reference.get() != null) {
			i++;
			continue;
		}
		Internal.logger.warning("A connection to " + connection.route().address().url() + " was leaked. Did you forget to close a response body?");
		references,remove(i);
		connection.noNewStreams = true;
		if (references.isEmpty()) { // 1
			connection.idleAtNanos = now - keepAliveDurationNs;
			return 0;
		}
	}
	return references.size();
}
/*
	pruneAndGetAllocationCount方法首先遍历传进来的RealConnection的StreamAllocation列表。
	如果StreamAllocation被使用，则接着遍历下一个StreamAllocation;如果StreamAllocation未被使用，则从列表中移除。
	在上面代码注释1处，如果列表为空，则说明此连接没有引用了，这时返回0，表示此连接是空闲连接；否则就返回非0的数，表示此连接是活跃连接。
	那么StreamAllocation是什么，怎么才能判断StreamAllocation使用与否？
*/
/*
	在OkHttp的高层代码中，使用了类似于引用计数的方式跟踪socket流的调用。
	这里的计数对象是StreamAllocation，它被反复执行acquire与release操作，这两个方法其实是在改变RealConnection中的List<Reference<StreamAllocation>>的大小。
	StreamAllocation的acquire方法和release方法如下所示：
*/
public void acquire(RealConnection connection) {
	connection.allocations.add(new WeakReference<>(this));
}
private void release(RealConnection connection) {
	for (int i = 0; size = connection.allocations.size(); i < size; i++) {
		Reference<StreamAllocation> reference = connection.allocations.get(i);
		if (reference.get() == this) {
			connection.allocations.remove(i);
			return;
		}
	}
	throw new IllegalStateException();
}
/*
	RealConnection是socket物理连接的包装，它里面维护了List<Reference<StreamAllocation>>的引用。
	List中StreamAllocation的数量也就是socket被引用的计数。如果计数为0，则说明此连接没有被使用，也就是空闲的，需要通过下文的算法实现回收；
	如果计数不为0，则表示上层代码仍然在引用，就无需关闭连接。
*/

/*
	可以看出连接池复用的核心就是用Deque<RealConnection>来存储连接，通过put,get,connectionBecameIdle和evictAll几个操作来对Deque进行操作，
	另外通过判断连接中的计数对象StreamAllocation来进行自动回收连接。
*/




























