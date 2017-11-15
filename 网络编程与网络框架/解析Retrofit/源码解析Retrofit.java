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

/*
	Call的创建过程
	下面我们创建Retrofit实例并调用如下代码来生成接口的动态代理对象：
*/
IpService ipService = retrofit.create(IpService.class);
// 接下来看Retrofit的create方法做了什么，代码如下：
public <T> T create(final Class<T> service) {
	Utils.validateServiceInterface(service);
	if (validateEagerly) {
		eagerlyValidateMethods(service);
	}
	return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{ service },
								new InvocationHandler() {
									private final Platform platform = Platform.get();
									@Override
									public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
										if (method.getDeclaringClass() == Object.class) {
											return method.invoke(this, args);
										}
										if (platform.isDefaultMethod(method)) {
											return platform.invokeDefaultMethod(method, service, proxy, args);
										}
										ServiceMethod serviceMethod = loadServiceMethod(method); // 1
										OkHttpCall okHttpCall = new OkHttpCall<>(serviceMethod, args);
										return serviceMethod.callAdapter.adapt(okHttpCall);
									}
								});
}
/*
	可以看到create返回了一个Proxy.newProxyInstance的动态代理对象。
	当我们调用IPService的getIpMsg方法时，最终会调用InvocationHandler的invoke方法。
	它有三个参数：第一个是代理对象，第二个是调用的方法，第三个是方法的参数。
	在上面代码注释1处loadServiceMethod(method)中的method就是我们定义的getIpMsg方法。
	下面看看loadServiceMethod方法里做了什么：
*/
private final Map<Method, ServiceMethod> serviceMethodCache = new LinkedHashMap<>();
ServiceMethod loadServiceMethod(Method method) {
	ServiceMethod result;
	synchronized (serviceMethodCache) {
		result = serviceMethodCache.get(method);
		if (result == null) {
			result = new ServiceMethod.Builder(this, method).build();
			serviceMethodCache.put(method, result);
		}
	}
	return result;
}
/*
	这里首先会从serviceMethodCache查询传入的方法是否有缓存。如果有，就用缓存的ServiceMethod；
	如果没有，就创建一个，并加入serviceMethodCache缓存起来。
	下面看ServiceMethod是如何构建的：
*/
public ServiceMethod build() {
	callAdapter = createCallAdapter(); // 1
	responseType = callAdapter.responseType(); // 2
	if (responseType == Response.class || responseType == okhttp3.Response.class) {
		throw methodError("'" + Utils.getRawType(responseType).getName() + "' is not a valid response body type. Did you mean ResponseBody?");
	}
	responseConverter = createResponseConverter(); // 3
	for (Annotation annotation : methodAnnotations) {
		parseMethodAnnotation(annotation); // 4
	}
	...
	int parameterCount = parameterAnnotationsArray.length;
	parameterHandlers = new ParameterHandler<?>[parameterCount];
	for (int p = 0; p < parameterCount; p++) {
		Type parameterType = parameterTypes[p];
		if (Utils.hasUnresolvableType(parameterType)) {
			throw parameterError(p, "Parameter type mnust not include a type variable or wildcard: %s", parameterType);
		}
		Annotation[] parameterAnnotations = parameterAnnotationsArray[p]; // 5
		if (parameterAnnotations == null) {
			throw parameterError(p, "No Retrofit annotation found.");
		}
		parameterHandlers[p] = parseParameter(p, parameterType, parameterAnnotations);
	}
	...
	return new ServiceMethod<>(this);
}
/*
	在上面代码注释1处调用了createCallAdapter方法，它最终会得到我们在构建Retrofit调用build方法时adapterFactories添加的对象的get方法。
	Retrofit的build方法部分代码如下所示：
*/
List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories);
adapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));
/*
	adapterFactories列表默认会添加defaultCallAdapterFactory。defaultCallAdapterFactory指的是ExecutorCallAdapterFactory。
	ExecutorCallAdapterFactory的get方法如下所示：
*/
public CallAdapter<Call<?>> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
	if (getRawType(returnType) != Call.class) {
		return null;
	}
	final Type responseType = Utils.getCallResponseType(returnType);
	return new CallAdapter<Call<?>>() {
		@Override
		public Type responseType() {
			return responseType;
		}
		@Override
		public <R> Call<R> adapt(Call<R> call) {
			return new ExecutorCallbackCall<>(callbackExecutor, call);
		}
	};
}
/*
	get方法会得到CallAdapter对象，CallAdapter的responseType方法会返回数据的真实类型，比如传入的是Call<IpModel>, responseType方法就会返回IpModel。
	adapt方法会创建ExecutorCallbackCall,它会将call的回调转发至UI线程。
	接着回到ServiceMethod的build方法。
	在那里注释2处调用CallAdapter的responseType得到的是返回数据的真实类型。注释3处调用createResponseConverter方法来遍历converterFactories列表中存储的Converter.Factory，
	并返回一个合适的Converter用来转换对象。
	此前我们在构建Retrofit时调用了addConverterFactory(GsonConverterFactory.create())，这段代码将GsonConverterFactory(Converter.Factory的子类)添加到converterFactories列表中，
	表示返回的数据支持转换为JSON对象。
	注释4处遍历parseMethodAnnotation方法来对请求方式（比如GET、POST）和请求地址进行解析。注释5处对方法中的参数注解进行解析（比如@Query、@Part）。最后创建ServiceMethod类并返回。
*/

/*
	接下来看Retrofit的create方法：
	在调用了loadServiceMethod方法后会创建OkHttpCall，
	OkHttpCall的构造方法只是进行了赋值操作。紧接着调用serviceMethod.callAdapter.adapt(okHttpCall)。
	callAdapter的adapt方法会创建ExecutroCallbackCall，并传入OkHttpCall。ExecutorCallbackCall的部分代码如下所示：
*/
ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
	this.callbackExecutor = callbackExecutor;
	this.delegate = delegate;
}
@Override
public void enqueue(final Callback<T> callback) {
	if (callback == null) throw new NullPointerException("callback == null");
	delegate.enqueue(new Callback<T>() { // 1
		@Override
		public void onResponse(Call<T> call, final Response<T> response) {
			callbackExecutor.execute(new Runnable() {
				@Override
				public void run() {
					if (delegate.isCanceled()) {
						callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
					} else {
						callback.onResponse(ExecutorCallbackCall.this, response);
					}
				}
			});
		}
		@Override
		public void onFailure(Call<T> call, final Throwable t) {
			callbackExecutor.execute(new Runnable() {
				@Override public void run () {
					callback.onFailure(ExecutorCallbackCall.this, t);
				}
			});
		}
	});
}
/*
	可以看出ExecutorCallbackCall是对Call的封装，它主要添加了通过callbackExecutor将请求回调到UI线程。
	当我们得到Call对象后会调用它的enqueue方法，其实调用的是ExecutorCallbackCall的enqueue方法。
	从而上面代码注释1处可以看出ExecutorCallbackCall的enqueue方法最终调用的是delegate的enqueue方法。
	delegate是传入的OkHttpCall。
*/

/*
	OkHttpCall的enqueue方法：
*/
public void enqueue(final Callback<T> callback) {
	if (callback == null)
		throw new NullPointerException("callback == null");
	okhttp3.Call call;
	...
	call.enqueue(new okhttp3.Callback() { // 1
		@Override
		public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) throws IOException {
			Response<T> response;
			try {
				response = parseResponse(rawResponse); // 2
			} catch (Throwable e) {
				callFailure(e);
				return;
			}
			callSuccess(response);
		}
		...
	})
}
/*
	上面代码注释1处调用了okhttp3.Call类型的call的enqueue方法。注释2处调用了parseRespnse方法：
*/
Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
	ResponseBody rawBody = rawResponse.body();
	...
	int code = rawResponse.code();
	if (code < 200 || code >= 300) {
		try {
			ResponseBody bufferedBody = Utils.buffer(rawBody);
			return Response.error(bufferedBody, rawResponse);
		} finally {
			rawBody.close();
		}
	}
	if (code == 204 || code == 205) {
		return Response.success(null, rawResponse);
	}
	ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
	try {
		T body = serviceMethod.toResponse(catchingBody); // 2
		return Response.success(body, rawResponse);
	} catch (RuntimeException e) {
		catchingBody.throwIfCaught();
		throw e;
	}
}
/*
	根据返回的不同状态码code值来做不同的操作。如果顺利，则会调用上面代码注释2处的代码。
	接下来看toResponse方法里做了什么：
*/
T toResponse(ResponseBody body) throws IOException {
	return responseConverter.convert(body);
}
/*
	这个responseConverter就是此前讲过在ServiceMethod的build方法调用createResponseConverter方法返回的Converter。
	在此前的例子中我们传入的是GsonConverterFactory，因此可以查看GsonConverterFactory的代码，如下所示：
*/
public final class GsonConverterFactory extends Converter.Factory {
	...
	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
		return new GsonResponseBodyConverter<>(gson, adapter);
	}
	...
}
/*
	在GsonConverterFactory中有一个方法responseBodyConverter，它最终会创建GsonResponseBodyConverter:
*/
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
	private final Gson gson;
	private final TypeAdapter<T> adapter;
	GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
		this.gson = gson;
		this.adapter = adapter;
	}
	@Override
	public T convert(ResponseBody value) throws IOException {
		JsonReader jsonReader = gson.newJsonReader(value.charStream());
		try {
			return adapter.read(jsonReader);
		} finally {
			value.close();
		}
	}
}
/*
	在GsonResponseBodyConverter的convert方法里会将回调的数据转换为JSON格式。
	因此，我们也知道了此前responseConverter.convert是为了转换为特定的数据格式。
	Call的enqueue方法主要做的就是用OkHttp来请求网络，将返回的Response进行数据转换并回调给UI线程。
*/

























