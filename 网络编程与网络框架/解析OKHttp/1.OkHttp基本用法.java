//1.使用前的准备工作
配置gradle
compile 'com.squareup.okhttp3:okhttp:3.2.0'
compile 'com.squareup.okio:okio:1.7.0'

//2.异步get请求
Request.Builder requestBuilder =new Request.Builder().url("http://blog.csdn.net/itachi85");
requestBuilder.method("GET", null);
Request request = requestBuilder.build();
OkHttpClient mOkHttpClient = new OkHttpClient();
Call mcall = mOkHttpClient.newCall(request);
mcall.enqueue(new Callback() {
	@Override
	public void onFailure(Call call, IOException e) {
	}
	@Override
	public void onResponse(Call call, Response response) throws IOException {
		String str = response.body().string();
		Log.d(TAG, str);
	}
});

//3.异步POST请求
RequestBody fromBody = new FromBody.Builder().add("ip", "59.108.54.37").build();
Request request = new Request.Builder()
	.url("http://ip.taobao.com/service/getIpInfo.php")
	.post(formBody)
	.build();
OkHttpClient mOkHttpClient = new OkHttpClient();
Call call = mOkHttpClient.newCall(request);
call.enqueue(new Callback() {
	@Override
	public void onFailure(Call call, IOException e) {
	}
	@Override
	public void onResponse(Call call, Response response) throws IOException {
		String str = response.body().string();
		Log.d(TAG, str);
	}
});

//4.异步上传文件
//定义上传类型
public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
//在SD卡根目录创建一个txt文件，里面的内容为“OKHttp”。
String filepath = "";
if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
} else {
	return;
}
File file = new File(filepath, "wangshu.txt");
Request request = new Request.Builder()
	.url("https:api.github.com/markdown/raw")
	.post(ResquestBody.create(MEDIA_TYPE_MARKDOWN,file))
	.build();
mOKHttpClient.newCall(request).enqueue(new Callback() {
	@Override
	public void onFailure(Call call, IOException e) {
	}
	@Override
	public void onResponse(Call call, Response response) throws IOException {
		Log.d(TAG, response.body().string());
	}
});

//5.异步下载文件
String url = "http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg";
Request request = new Request.Builder().url(url).build();
mOKHttpClient.newCall(request).enqueue(new Callback() {
	@Override
	public void onFailure(Call call, IOException e) {}
	@Override
	public void onResponse(Call call, Response response) {
		InputStream inputStream = response.body().byteStream();
		FileOutputStream fileOutputStream = null;
		String filepath = "";
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
			} else {
				filepath = getFilesDir().getAbsolutePath();
			}
			File file = new File(filepath, "wangshu.jpg");
			if(null != file) {
				fileOutputStream = new FileOutputStream(file);
				byte[] buffer = new byte[2048];
				int len = 0;
				while ((len = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, len);
				}
				fileOutputStream.flush();
			}
		} catch (IOException e) {
			Log.d(TAG, "IOException");
			e.printStackTrace();
		}
	}
});

//6.异步上传Multipart文件
private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
private void sendMultipart() {
	mOkHttpClient = new OkHttpClient();
	RequestBody requestBody = new MultipartBody.Builder()
		.setType(MultipartBody.FORM)
		.addFormDataPart("title", "wangshu")//1
		.addFormDataPart("image", "wangshu.jpg", RequestBody.create(MEDIA_TYPE_PNG, new File("/sdcard/wangshu.jpg")))//2
		.build();
	Request request = new Request.Builder()
		.header("Authorization", "Client-ID " + "...");
		.url("https://api.imgur.com/3/image")
		.post(requestBody)
		.build();
	mOKHttpClient.newCall(request).enqueue(new Callback() {
		@Override
		public void onFailure(Call call, IOException e) {
		}
		@Override
		public void onResponse(Call cal, Response response) throws IOException {
			Log.d(TAG, response.body().string());
		}
	});
}

//7.设置超时时间和缓存
File sdcache = getExternalCacheDir();
int cacheSize = 10 * 1024 * 1024;
OKHttpClient.Builder builder = new OkHttpClient.Builder()
	.connectTimeout(15, TimeUnit.SECONDS)
	.writeTimeout(20, TimeUnit.SECONDS)
	.readTimeout(20, TimeUnit.SECONDS)
	.cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
mOKHttpClient = builder.build();

//8.取消请求
private ScheduleExecutorService executor = Executors.newScheduledThreadPool(1);
private void cancel() {
	final Request request = new Request.Builder()
		.url("http://www.baidu.com")
		.cacheControl(CacheControl.FORCE_NETWORK)//1
		.build();
	Call call = null;
	call = mOKHttpClient.newCall(request);
	final Call finalCall = call;
	//100ms后取消call
	executor.schedule(new Runnable() {
		@Override
		public void run() {
			finalCall.cancel();
		}
	}, 100, TimeUnit.MILLISECONDS);
	call.enqueue(new Callback() {
		@Override
		public void onFailure(Call call, IOException e) {
		}
		@Override
		public void onResponse(Call call, Response response) throws IOException {
			if (null != response.cacheResponse()) {
				String str = response.cacheResponse().toString();
				Log.d(TAG, "cache---" + str);
			} else {
				String str = response.networkResponse().toString();
				Log.d(TAG, "network---" + str);
			}
		}
	});
}

//9.关于封装
//避免重复代码调用；将请求结果回调改为UI线程
//封装Okhttp推荐使用OkHttpFinal
//首先写一个抽象类用于请求回调
public abstract class ResultCallback {
	public abstract void onError(Request request, Exception e);
	public abstract void onResponse(String str) throws IOException;
}

//接下来封装OKHttp
public class OkHttpEngine {
	private static volatile OkHttpEngine singleton;
	private OkHttpClient mOkHttpClient;
	private Handler mHandler;
	
	public static OkHttpEngine getInstance(Context context) {
		if (singleton == null) {
			synchronized (OkHttpEngine.class) {
				if (singleton == null) {
					singleton = new OkHttpClient(context);
				}
			}
		}
		return singleton;
	}
	
	private OkHttpEngine(Context context) {
		File sdcache = context.getExternalCacheDir();
		int cacheSize = 10 * 1024 * 1024;
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
			.connectTimeout(15, TimeUnit.SECONDS)
			.writeTimeout(20, TimeUnit.SECONDS)
			.readTimeout(20, TimeUnit.SECONDS)
			.cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
		mOkHttpClient = builder.build();
		mHandler = new Handler();
	}
	/**
	 * 异步GET请求
	 * @param url
	 * @param callback
	 */
	public void getAsynHttp(String url, ResultCallback callback) {
		final Request request = new Request.Builder()
			.url(url)
			.build();
		Call call = mOkHttpClient.newCall(request);
		dealResult(call, callback();
	}
	private void dealResult(Call call, final ResultCallback callback) {
		call.enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				sendFailedCallback(call.request(), e, callback);
			}
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				sendSuccessCallback(response, callback);
			}
			private void sendSuccessCallback(final String str, final ResultCallback callback) {
				call.enqueue(new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						sendFailedCallback(call.request(), e, callback);
					}
					@Override 
					public void onResponse(Call call, Response response) throws IOException {
						sendSuccessCallback(response, callback);
					}
					private void sendSuccessCallback(final String str, final ResultCallback callback) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (callback != null) {
									try {
										callback.onResponse(str);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
					private void sendFailedCallback(final Request request, final Exception e, final ResultCallback callback) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (callback != null) {
									callback.onError(request, e);
								}
							}
						}
					}
				});
			}
		}
	}
}

//原理就是写一个双重检查模式的单例，在开始创建的时候配置好OkHttpClient，并创建Handler，
//在请求网络的时候用Handler将请求的结果回调给UI线程。当想要请求网络时就调用OkHttpEngine的getAsyncHttp方法。
OkHttpEngine.getInstance(MainActivity.this).getAsynHttp("http://www.baidu.com", new ResultCallback() {
	@Override
	public void onError(Request request, Exception e) {
		
	}
	@Override
	public void onResponse(String string) throws IOException{
		String str = response.body().string();
		Log.d(TAG, str);
		Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
	}
});














