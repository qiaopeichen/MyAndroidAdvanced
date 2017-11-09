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
	// 
}











