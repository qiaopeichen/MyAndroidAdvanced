/* 
	POST请求和GET请求类似，就是需要配置要传递的参数。
	这里访问淘宝IP库，它的接口说明在地址http:ip.taobao.com/instructions.php中
	
	接口说明
	
	1. 请求接口（GET）：
		/service/getIpInfo.php?ip=[ip地址字串]
	2. 响应信息：
		（json格式的）国家 、省（自治区或直辖市）、市（县）、运营商
	3. 返回数据格式：
		{"code":0,"data":{"ip":"210.75.225.254","country":"\u4e2d\u56fd","area":"\u534e\u5317",
		"region":"\u5317\u4eac\u5e02","city":"\u5317\u4eac\u5e02","county":"","isp":"\u7535\u4fe1",
		"country_id":"86","area_id":"100000","region_id":"110000","city_id":"110000",
		"county_id":"-1","isp_id":"100017"}}
		其中code的值的含义为，0：成功，1：失败。

	这个接口不仅支持GET请求，同时也支持POST请求，代码如下所示：
*/
private void useHttpClientPost(String url) {
	HttpPost mHttpPost = new HttpPost(url);
	mHttpPost.addHeader("Connection", "Keep-Alive");
	try {
		HttpClient mHttpClient = createHttpClient();
		List<NameValuePair> postParams = new ArrayList<>(); // NameValuePair：键值对
		// 要传递的参数
		postParams.add(new BasicNameValuePair("ip", "59.108.54.37"));
		mHttpPost.setEntity(new UrlEncodedFormEntity(postParams));
		HttpResponse mHttpResponse = mHttpClient.execute(mHttpPost);
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

// 注释1处的converStreamToString方法将请求结果转换成String类型
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

// 开启线程访问淘宝库
new Thread(new Runnable {
	@Override
	public void run() {
		useHttpClientPost("http://ip.taobao.com/service/getIpInfo.php");
	}
}).start();