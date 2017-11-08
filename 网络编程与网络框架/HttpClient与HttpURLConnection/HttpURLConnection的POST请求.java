/* 
	在Android2.2版本及其以前，HttpURLConnection一直存在着一些令人厌烦的bug。
	比如说对一个刻度的InputStream调用close()方法时，就有可能会导致连接池失效。
	我们通常的解决办法就是直接禁用连接池的功能，代码如下所示：
*/
private void disableConnectionReuseIfNecessary() {
	// 这是2.2版本之前的一个bug
	if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
		System.setProperty("http.keepAlive", "false");
		
	}
}

/* 
	所以在Android2.2之前用HttpClient是较好的选择；而在Android2.3版本及其以后，HttpURLConnection则是最佳的选择，
	它的API简单，体积较小，因而非常适用于Android项目。
	另外在Android6.0版本中，HttpClient库被移除了，如果不引用HttpClient，HttpURLConnection则是以后我们唯一的选择。
*/

// HttpURLConnection的POST请求

/* 
	首先创建一个UrlConnectionManager类，
	然后在里面提供getHttpURLConnection()方法用于配置默认的参数并返回HttpURLConnection，
	代码如下所示：
*/
public static HttpURLConnection getHttpURLConnection(String url) {
	HttpURLConnection mHttpURLConnection = null;
	try {
		URL mUrl = new URL(url);
		mHttpURLConnection = (HttpURLConnection)mUrl.openConnection();
		// 设置连接超时时间
		mHttpURLConnection.setConnectionTimeout(15000);
		// 设置读取超时时间
		mHttpURLConnection.setReadTimeout(15000);
		// 设置请求参数
		mHttpURLConnection.setRequestMethod("POST");
		// 添加Header
		mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
		// 接收输入流
		mHttpURLConnection.setDoInput(true);
		// 传递参数时需要开启
		mHttpURLConnection.setDoOutput(true);
	} catch (IOException e) {
		e.printStackTrace();
	}
	return mHttpURLConnection;
}

// 因为我们要发送POST请求，所以在UrlConnManager类中再写一个postParams方法，组织一下请求参数并将请求参数写入输出流，代码如下所示：
public static void postParams(OutputStream output, List<NameValuePair>paramsList) throws IOException {
	StringBuilder mStringBuilder = new StringBuilder();
	for (NameValuePair pair:paramsList) {
		if (!TextUtils.isEmpty(mStringBuilder)) {
			mStringBuilder.append("&");
		}
		mStringBuilder.append(URLEncoder.encode(pair.getName(), "UTF-8"));
		mStringBuilder.append("=");
		mStringBuilder.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	}
	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
	writer.write(mStringBuilder.toString());
	writer.flush();
	writer.close();
}

/* 
	接下来我们添加请求参数，
	调用postParams()方法将请求的参数组织好并传给HttpURLConnection的输出流，请求连接并处理返回的结果。
	这里仍旧访问淘宝IP库，代码如下所示：
*/
private void useHttpUrlConnectionPost(String url) {
	InputStream mInputStream = null;
	HttpURLConnection mHttpURLConnection = UrlConnManager.getHttpURLConnection(url);
	try {
		List<NameValuePair> postParams = new ArrayList<>();
		postParams.add(new BasicNameValuePair("ip", "59.108.54.37"));
		UrlConnManager.postParams(mHttpURLConnection.getOutputStream(), postParams);
		mHttpURLConnection.connect();
		mInputStream = mHttpURLConnection.getInputStream();
		int code = mHttpURLConnection.getResponseCode();
		String response = converStreamToString(mInputStream);
		Log.d(TAG, "请求状态码:" + code + "\n请求结果:\n" + response);
		mInputStream.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

private String converStreamToString(mInputStream is) throws IOException {
	BufferedReader reader = new BufferedReader(InputStreamReader(is));
	StringBuffer sb = new StringBuffer();
	String line = null;
	while ((line = reader.readLine()) != null) {
		sb.append(line + "\n");
	}
	String response = sb.toString();
	return response;
}
// 最后开启线程请求淘宝IP库，如下所示：
new Thread(new Runnable() {
	@Override
	public void run() {
		useHttpUrlConnectionPost("http://ip.taobao.com/service/getIpInfo.php");
	}
}).start();














