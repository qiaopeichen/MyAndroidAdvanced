// 1.从RequestQueue入手
// 使用Volley之前首先要创建RequestQueue，就从这里开始入手，如下所示：
RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
// 查看newRequestQueue做了什么：
public static RequestQueue newRequestQueue(Context context) {
	return newRequestQueue(Context, (HttpStack)null); // null也可以强制转换类型
}
public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
	return newRequestQueue(context, stack, -1);
}
// 这里连续调用了两个重载函数，最终调用如下代码：
public static RequestQueue newRequestQueue(Context context, HttpStack stack, int maxDiskCacheBytes) {
	File cacheDir = new File(context.getCacheDir(), "volley");
	String userAgent = "volley/0";
	try {
		String network = context.getPackageName();
		PackageInfo queue = context.getPackageManager().getPackageInfo(network, 0);
		userAgent = network + "/" +queue.versionCode;
	} catch (NameNotFoundException var7) {
		;
	}
	if (stack == null) {
		if (VERSION.SDK_INT >= 9) { // 1
			stack = new HurlStack();
		} else {
			stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
		}
	}
	BasicNetwork network1 = new BasicNetwork((HttpStack)stack);
	RequestQueue queue1;
	if (maxDiskCacheBytes <= -1) {
		queue1 = new RequestQueue(new DiskBasedCache(cacheDir), network1);
	} else {
		queue1 = new RequestQueue(new DiskBasedCache(cacheDir, maxDiskCacheBytes), network1);
	}
	queue1.start();
	return queue1;
}
// 注释1处说明，如果Android版本>=2.3，则调用基于HttpURLConnection的HurlStack，否则调用基于HttpClient的HttpClientStack。
// 接下来创建RequestQueue，并调用它的Start()方法：
public void start() {
	this.stop();
	this.mCacheDispatcher = new CacheDispatcher(this.mCacheQueue, this.mNetworkQueue, this.mCache, this.mDelivery);
	this.mCacheDispatcher.start();
	
	for (int i = 0; i < this.mDispatchers.length; ++i) {
		NetworkDispatcher networkDispatcher = new NetworkDispatcher (this.mNetworkQueue, this.mNetwork, this.mCache, this.mDelivery);
		this.mDispatchers[i] = networkDispatcher;
		networkDispatcher.start();
	}
}
// CacheDispatcher是缓存调度线程，并调用了start()方法。在循环中嗲用了NetworkDispatcher的start方法。
// NetworkDispatcher是网络调度线程，默认情况下mDispatchers.length为4，默认开启了4个网络调度线程，加上一个缓存调度线程，就有5个线程在后台运行并等待请求的到来。
// 按照Volley的使用流程，接下来会创建各种Request，并调用RequestQueue的add()方法:
public <T> Request<T> add(Request<T> request) {
	request.setRequestQueue(this);
	Set var2 = this.mCurrentRequests;
	synchronized(this.mCurrentRequests) {
		this.mCurrentRequests.add(request);
	}
	request.setSequence(this.getSequenceNumber());
	request.addMarker("add-to-queue");
	// 如果不能缓存，则将请求添加到网络请求队列中
	if (!request.shouldCache()) { // 1
		this.mNetworkQueue.add(request);
		return request;
	} else {
		Map var8 = this.mWaitingRequests;
		synchronized(this.mWaitingRequests) {
			String cacheKey = request.getCacheKey();
			
			// 如果此前有相同的请求且还没有返回结果的，就将此请求加入mWaitingRequests队列
			if (this.mWaitingRequests.containsKey(cacheKey)) {
				Object stagedRequests = (Queue)this.mWaitingRequests.get(cacheKey);
				if(stagedRequests == null) {
					stagedRequests = new LinkedList();
				}
				((Queue)stagedRequests).add(request);
				this.mWaitingRequests.put(cacheKey, stagedRequests);
				if(VolleyLog.DEBUG) {
					VolleyLog.v("Request for cachekey=%s in flight, putting on hold.", new Object[]{cachekey});
				}
			} else {
				// 没有的话就将请求加入缓存队列mCacheQueue
				this.mWaitingRequests.put(cachekey, (Object)null);
				this.mCacheQueue.add(request);
			}
			return request;
		}
	}
}
// RequestQueue的add方法并没有请求网络或者对缓存进行操作。当将请求添加到网络请求队列或缓存队列时，在后台的网络调度线程和缓存调度线程轮询各自的请求队列，若发现有请求任务则开始执行。

// CacheDispatcher缓存调度线程
// CacheDispatcher的run方法代码如下所示：
public void run() {
	if(DEBUG) {
		VolleyLog.v("start new dispatcher", new Object[0]);
	}
	// 线程优先级设置为最高级别
	Process.setThreadPriority(10);
	this.mCache.initialize();
	while(true) { 
		while(true) { 
			while(true) { 
				while(true) {  
					try {
						// 获取缓存队列中的一个请求
						final Request e = (Request)this.mCacheQueue.take();
						e.addMarker("cache-queue-take");
						// 如果请求取消了，则将请求停止
						if (e.isCanceled()) {
							e.finish("cache-discard-canceled");
						} else {
							// 查看是否有缓存的响应
							Entry entry = this.mCache.get(e.getCacheKey());
							// 如果缓存的响应为空，则将请求加入网络队列
							if (entry == null) {
								e.addMarker("cache-miss");
								this.mNetworkQueue.put(e);
								// 判断缓存的响应是否过期
							} else if(!entry.isExpired()) {
								e.addMarker("cache-hit");
								// 对数据进行解析并回调给主线程
								Response response = e.parseNetworkResponse(newNetworkResponse(entry.data, entry.responseHeaders));
								...
							}
						}
					}
				}
			}
		}
	}
}

// NetworkDispatcher网络调度线程
// NetworkDispatcher的run方法代码如下所示：
public void run() {
	Process.setThreadPriority(10);
	while(true) {
		long startTimeMs;
		Request request;
		while(true) {
			startTimeMs = SystemClock.elapsedRealtime();
			try {
				// 从队列中取出请求
				request = (Request)this.mQueue.take();
				break;
			} catch (InterruptedException var6) {
				if(this.mQuit) {
					return;
				}
			}
		}
		try {
			request.addMarker("network-queue-take");
			if(request.isCanceled()) {
				request.finish("network-discard-cancelled")
			} else {
				this.addTrafficStatsTag(request);
				// 请求网络
				NetworkResponse e = this.mNetwork.performRequest(request);
				request.addMarker("network-http-complete");
				if(e.notModified && request.hasHadResponseDelivered()) {
					request.finish("not-modified");
				} else {
					Response volleyError1 = request.parseNetworkResponse(e);
					request.addMarker("network-parse-complete");
					if(request.shouldCache() && volleyError1.cacheEntry != null) {
						// 将响应存入缓存
						this.mCache.put(request.getCacheKey(), volleyError1.cacheEntry);
						request.addMarker("network-cache-written");
					}
					request.markDelivered();
					this.mDelivery.postResponse(request, volleyError1);
				}
			}
			...
		}
	}
}

/* 
	网络调度线程也是从队列中取出请求并且判断该请求是否被取消了。
	如果该请求没被取消，就去请求网络得到响应并回调给主线程。
	请求网络时调用this.mNetwork.performRequest(request),这个mNetwork是一个接口，实现它的类是BasicNetwork。
	接下来查看BasicNetwork的perfromRequest()方法：
*/
public NetworkResponse performRequest(Request<?> request) throws VolleyError {
	long requestStart = SystemClock.elapsedRealtime();
	while (true) {
		HttpResponse httpResponse = null;
		Object responseContents = null;
		Map responseHeaders = Collections.emptyMap();
		try {
			HashMap e = new HashMap();
			this.addCacheHeaders(e, request.getCacheEntry());
			httpResponse = this.mHttpStack.performRequest(request, e); // 1
			StatusLine statusCode1 = httpResponse.getStatusLine();
			int networkResponse1 = statusCode1.getStatusCode();
			responseHeaders = convertHeaders(httpResponse.getAllHeaders());
			if (netwrkResponse1 == 304) {
				Entry requestLifetime2 = request.getCacheEntry();
				if (requestLifetime2 == null) {
					return new NetworkResponse(304, (byte[])null, responseHeaders, true, SystemClock.elapsedRealtime() - requestStart);
				}
				requestLifetime2.responseHeaders.putAll(responseHeaders);
				return new NetworkResponse(304, requestLifetime2.data, requestLifetime2.responseHeaders, true, SystemClock.elapsedRealtime() - requestStart);
			}
			...
		}
	}
}
/*
	上面代码注释1处调用HttpStack的performRequest方法请求网络，接下来根据不同的状态码来返回不同的NetworkResponse。另外HttpStack也是一个接口，实现它的两个类是HurlStack和HttpClientStack。
	我们在回到NetworkDispather，请求网络后，会将响应结果存在缓存中，并调用this.mDelivery.postResponse(request, volleyError1)来回调给主线程。
	查看Delivery的postResponse方法，如下所示：
*/
public void postResponse(Request<?> request, Request<?> response, Runnable runnable) {
	request.markDelivered();
	request.addMarker("post-response");
	this.mResponsePoster.execute(new ExecutorDelivery.ResponseDeliveryRunnable(request, response, runnable));
}

// 查看ResponseDeliveryRunnable里面做了什么：
private class ResponseDeliveryRunnable implements Runnable {
	private final Request mRequest;
	private final Response mResponse;
	private final Runnable mRunnable;
	public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable) {
		this.mRequest = request;
		this.mResponse = response;
		this.mRunnable = runnable;
	}
	public void run() {
		if (this.mRequest.isCanceled()) {
			this.mRequest.finish("canceled-at-delivery");
		} else {
			if (this.mResponse.isSuccess()) {
				this.mRequest.deliveryResponse(this.mResponse.result); // 1
			} else {
				this.mRequest.deliveryError(this.mResponse.error);
			}
			...
		}
	}
}
// 上面代码注释1处调用了Request的deliverResponse方法，假设这里我们使用的是StringRequest，它继承自Request，因此查看StringRequest的源码：
public class StringRequest extends Request<String> {
	private final Listener<String> mListener;
	public StringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
		super (method, url, errorListener);
		this.mListener = listener;
	}
	public StringRequest(String url, Listenr<String> listener, ErrorListener errorListener) {
		this(0, url, listener, errorListener);
	}
	protected void deliverResponse(String response) {
		this.mListener.onResponse(response);
	}
	...
}

// 在deliverResponse方法中调用了this.mListener.onResponse(response)， 最终将response回调给了Response.Listener的onResponse()方法。
// 我们用StringRequest请求网络的写法是这样的：
RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
StringRequest mStringRequest = new StringRequest(Request.Method.GET, "http://www.baidu.com",
							new Response.Listener<String>() {
								@Override
								public void onResponse(String response) { // 1
									Log.i("wangshu", response);
								}, 
							new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									Log.e("wangshu", error.getMessage(), error);
								}
							});
// 将请求添加到请求队列中
mQueue.add(mStringRequest);
// 上面代码注释1处将请求网络得到的response通过Response.Listener的onResponse方法回调回来，这样整个Volley的大致流程就走通了。









