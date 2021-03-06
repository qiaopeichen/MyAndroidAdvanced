/*
	这里讲讲Apache的HttpClient和Java的HttpURLConnection，它们都是我们平常请求网络会用到的。
	无论我们自己封装的网络请求类还是第三方的网络请求框架，都离不开这两个类库。
*/

/*
	AndroidSDK中包含了HttpClient。
	Android6.0版本直接删除了HttpClient类库。
	如果仍想使用它，解决方法就是在相应module下的build.gradle中加入如下代码：
*/
android {
	useLibrary 'org.apache.http.legacy'
}

// HttpClient的GET请求

// 首先用DefaultHttpClient类来实例化一个HttpClient，并配置好默认的请求参数，代码如下：
// 创建HttpClient
private HttpClient createHttpClient() {
	HttpParams mDefaultHttpParams = new BasicHttpParams();
	// 设置连接超时
	HttpConnectionParams.setConnectionTimeout(mDefaultHttpParams, 15000);
	// 设置请求超时
	HttpConnectionParams.setSoTimeout(mDefaultHttpParams, 15000);
	HttpConnectionParams.setTcpNoDelay(mDefaultHttpParams, true);
	HttpProtocolParams.setVersion(mDefaultHttpParams, HttpVersion.HTTP_1_1);
	HttpProtocolParams.setContentCharset(mDefaultHttpParams, HTTP.UTF_8);
	// 持续握手
	HttpProtocolParams.setUseExceptContinue(mDefaultHttpParams, true);
	HttpClient mHttpClient = new DefaultHttpClient(mDefaultHttpParams);
	return mHttpClient;
}

//接下来创建HttpGet和HttpClient，请求网络并得到HttpResponse，并对HttpResponse进行处理：
private void useHttpClientGet(String url) {
	HttpGet mHttpGet = new HttpGet(url);
	mHttpGet.addHeader("Connection", "Keep-Alive");
	try {
		HttpClient mHttpClient = createHttpClient();
		HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
		HttpEntity mHttpEntity = mHttpResponse.getEntity();
		int code = mHttpResponse.getStatusLine().getStatusCode();
		if (null != mHttpEntity) {
			InputStream mInputStream = mHttpEntity.getContent();
			String response = converStreamToString(mInputStream); // 1
			Log.d(TAG, "请求状态码：" + code + "\n请求结果：\n" + response);
			mInputStream.close();
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
}

// 上面代码注释1处的converStreamToString方法将请求结果转换成String类型
private String converStreamToString(InputStream is) throws IOException {
	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	StringBuffer sb = new StringBuffer();
	String line = null;
	while ((line = reader.readLine()) != null) {
		sb.append(line + "\n");
	}
	String response = sb.toString();
	return response;
}

// 最后我们开启线程访问百度。
new Thread(new Runnable() {
	@Override
	public void run() {
		useHttpClientGet("http://www.baidu.com");
	}
}).start();






