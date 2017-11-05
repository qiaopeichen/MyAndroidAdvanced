public class OkHttpEngine {
	private static volatile OkHttpEngine singleton;
	private OkHttpClient mOkHttpClient;
	private Handler mHandler;
	
	public static OkHttpEngine getInstance(Context context) {
		if (mInstance == null) {
			synchronized (OkHttpEngine.class) {
				if (mInstance == null) {
					mInstance = new OkHttpClient(context);
				}
			}
		}
		return mInstance;
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
								if () {
									justRun();
								}
							}
						}
					}
				})
			}
		}
	}
}