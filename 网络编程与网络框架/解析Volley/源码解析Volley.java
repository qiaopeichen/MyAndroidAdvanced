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











