/*
	在2013年Google I/O大会上推出了一个新的网络通信框架Volley。
	Volley既可以访问网络取得数据，也可以加载图片，并且在性能方面进行了大幅度的调整。
	它的设计目标就是适合进行数据量不大但通信频繁的网络操作。
	而对于大数据量的网络操作，比如说下载文件等，Volley的表现却非常糟糕。
*/

// Volley基本用法
// 在使用Volley前请下载Volley库且放在libs目录下并add到工程中。其下载地址为http://central.maven.org/maven2/com/mcxiaoke/volley/library/。

/*
	Volley请求网络都是基于请求队列的，开发者只要把请求放在请求队列中就可以了。
	一般情况下完全可以只有一个请求队列（对应Application）。
	如果网络请求非常多或有其他情况，则可以使一个Avtivity对应一个网络请求队列。
	首先创建队列如下所示：
*/
RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());

// StringRequest返回的数据是String类型的。我们查看一下StringRequest的源码：
// 这里有两个构造方法，如果采用第二个则默认是GET请求。
public class StringRequest extends Request<String> {
	private final Listener<String> mListener;
	public StringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		this.mListener = listener;
	}
	
	public StringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
		this(0, url, listenr, errorListener);
	}
	...
}

// 我们试着用GET方法来请求百度，代码如下所示：
RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
StringRequest mStringRequest = new StringRequest(Request.Method.GET, "http://www.baidu.com", 
							new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									Log.d(TAG, response);
								}		
							}, new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									Log.e(TAG, error.getMessage(), error);
								}
							});
// 将请求添加到请求队列中
mQueue.add(mStringRequest);
// 请求结果不用说是百度界面的HTML文件。

// JsonRequest的用法
/*
	为了返回的数据是JSON格式的数据，这里仍旧访问淘宝IP库，解析JSON数据，这里采用的是Gson库。
	针对这个接口返回的JSON数据格式，我们可以采用http://www.bejson.com/json2javapojo/ 这个网站来将JSON字符串转换成Java实体类。
	生成的实体类经过了简单修改，如下所示：
*/
public class IpModel {
	private IpData data;
	public void setData(IpData data) {
		this.data = data;
	}
	public IpData getData() {
		return this.data;
	}
	...
}

// IpModel中包含了IpData，IpData类如下所示：
public class IpData {
	private String country;
	public void setCountry(Country country) {
		this.country = country;
	}
	public Country getCountry() {
		return country;
	}
	...
}

// JsonRequest和StringRequest的使用方法类似，如下所示：
JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://ip.taobao.com/service/getIpInfo.php?ip=59.108.54.37", 
									new Response.Listener<JSONObject>() {
										@Override
										public void onResponse(JSONObject response) {
											IpModel ipModel = new Gson().fromJson(response.toString(), IpModel.class);
											if (null != ipModel && null != ipModel.getData()){
												String city = ipModel.getData().getCity();
												Log.d(TAG, city);
											}
										}
									}, new Response.ErrorListener() {
										@Override
										public void onErrorResponse(VolleyError error) {
											Log.e(TAG, error.getMessage(), error);
										}
									});
mQueue.add(mJsonObjectRequest);
// 最终Log打印的结果是“北京”，很显然通过淘宝IP库查询59.108.54.37这个IP地址所在的地理位置为北京。

// 使用ImageRequest加载图片
RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
ImageRequest imageRequest = new ImageRequest("http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg", 
						new Response.Listener<Bitmap>() {
							@Override
							public void onResponse(Bitmap response) {
								iv_image.setImageBitmap(response);
							}
						}, 0, 0, Bitmap.Config.RGB_565, 
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								iv_image.setImageResource(R.drawable.icon_default);
							}
						});
						mQueue.add(imageRequest);

// 查看ImageRequest的源码发现，它可以设置你想要的图片的最大宽度和高度。在加载图片时，如果图片超过期望的最大宽度和高度，则会进行压缩：
public ImageRequest(String url, Listener<Bitmap> listener, int maxWidth, int maxHeight, ScaleType scaleType, Config decodeConfig, ErrorListener errorListener) {
	super(0, url, errorListener);
	this.setRetryPolicy(new DefaultRetryPolicy(1000, 2, 2.0F));
	this.mListener = listener;
	this.mDecodeConfig = decodeConfig;
	this.mMaxWidth = maxWidth;
	this.mMaxHeight = maxHeight;
	this.mScaleType = scaleType;
}

// 使用ImageLoader加载图片
RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
ImageLoader imageLoader = new ImageLoader(mQueue, new BitmapCache());
ImageLoader.ImageLisener listener = ImageLoader.getImageListener(iv_image, R.drawable, ico_default, R.drawable. ico_default);
imageLoader.get("http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg", listener);

// ImageLoader也提供了设置最大宽度和高度的方法，如下所示：
public ImageLoader.ImageContainer get(String requestUrl, ImageLoader.ImageListener imageListener, int maxWidth, int maxHeight) {
	return this.get(requestUrl, imageListener, maxWidth, maxHeight, ScaleType.CENTER_INSIDE);
}

// 使用NetworkImageView加载图片
// NetworkImageView是一个自定义控件，继承自ImageView, 其封装了请求网络加载图片的功能，先在布局中引用：
<com.android.volley.toolbox.NetworkImageView
	android:id="@+id/iv_image"
	android:layout_width="200dp"
	android:layout_height="200dp"
	android:layout_centerHorizontal="true"
	android:layout_below="@id/iv_image"
	android:layout_marginTop="20dp">
</com.android.volley.toolbox.NetworkImageView>

// 接着在代码中调用，其和ImageLoader用法类似，如下所示：
iv_image = (ImageView) this.findViewById(R.id.iv_image);
RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
ImageLoader imageLoader = new ImageLoader(mQueue, new BitmapCache());
nv_image.setDefaultImageResId(R.drawable.ico_default);
nv_image.setErrorImageResId(R.drawable.ico_default);
nv_image.setImageUrl("http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg", imageLoader);














