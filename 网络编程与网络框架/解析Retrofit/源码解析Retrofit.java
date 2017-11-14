/*
	Retrofit的创建过程：
	当我们使用Retrofit请求网络时，首先要写请求接口：
*/
public interface IpService {
	@GET("getIpInfo.php?ip=59.108.54.37")
	Call<IpModel> getIpMsg();
}
/*
	接着我们通过调用如下代码来创建Retrofit：
*/
Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(url)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
/*
	Retrofit是通过建造者模式构建出来的。接下来查看Builder方法做了什么:
*/
public Builder() {
	this(Platform.get());
}
// 很简短，查看Platform的get方法，如下所示：
private static final Platform PLATFORM = findPlatform();
static Platform get() {
	return PLATFORM;
}
private static Platform findPlatform() {
	try {
		Class.forName("android.os.Build");
		if (Build.VERSION.SDK_INT != 0) {
			return new Android();
		}
	} catch (ClassNotFoundException ignored) {}
	try {
		Class.forName("java.util.Optional");
		return new Java8();
	} catch (ClassNotFoundException ignored) {}
	try {
		Class.forName("org.robovm.apple.foundation.NSObject");
		return new IOS();
	} catch (ClassNotFoundException ignored){}
	return new Platform();
}
/*
	Platform的get方法最终调用的是findPlatform方法，根据不同的运行平台来提供不同的线程池。接下来查看build方法，代码如下所示：
*/
public Retrofit build() {
	if (baseUrl == null) { // 1
		throw new IllegalStateException("Base URL required.");
	}
	okhttp3.Call.Factory callFactory = this.callFactory; // 2
	if (callFactory == null) {
		callFactory = new OkHttpClient(); // 3
	}
	Executor callbackExecutor = this.callbackExecutor;
	if (callbackExecutor == null) {
		callbackExecutor = platform.defaultCallbackExecutor(); // 4
	}
	List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories); // 5
	adapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));
	List<Converter.Factory> converterFactories = new ArrayList<>(this.converterFactories); // 6
	return new Retrofit(callFactory, baseUrl, converterFactories, adapterFactories, callbackExecutor, validateEagerly);
}
/*
	从上面代码注释1处可以看出baseUrl是必须制定的。
	在注释2处，callFactory默认为this.callFactory。
	this.callFactory就是我们在构建Retrofit时调用callFactory方法锁传进来的，如下所示：
*/
public Builder callFactory(okhttp3.Call.Factory factory) {
	this.callFactory = checkNotNull(factory, "factory == null");
	return this;
}
/*
	因此，如果需要对OkHttpClient进行设置，则可以构建OkHttpClient对象，然后调用callFactory方法将设置好的OkHttpClient传进去。
	在注释3处，如果没有设置callFactory，则直接创建OkHttpClient。注释4处的callbackExecutor用来将回调传递到UI线程。
	注释5处的adapterFactories主要用于存储对Call进行转化的对象，后面也会提及。
	此前在例子中调用的addConverterFactory(GsonConverterFactory.create())，就是设置返回的数据支持转换为Gson对象。
	其最终会返回配置好的Retrofit类。
*/










