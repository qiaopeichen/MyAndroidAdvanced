/*
	Retrofit是Square公司开发的一款针对Android网络请求的框架，Retrofit底层是基于OkHttp实现的。
	与其他网络框架不同的是，它更多使用运行时注解的方式提供功能。
*/

// 首先配置build.gradle，如下所示：
dependencies {
	...
	compile 'com.squareup.retrofit2:retrofit:2.1.0'
	compile 'com.squareup.retrofit2:converter-gson:2.1.0'
}
/*
	最后一行是为了增加支持返回值为Gson类型数据所添加的依赖包。如果想增加其他类型数据的支持，可以添加如下依赖包。
	Scalars(primitives,boxed,and String): com.sqarep.retrofit2:converter-scalars;
	Jackson: com.squareup.retrofit2:converter-jackson;
	Moshi: com.squareup.retrofit2:converter-moshi;
	Protobuf: com.squareup.retrofit2:converter-protobuf;
	wire: com.squareup.retrofit2:converter-wire;
	Simple XML: com.squareup.retrofit2:converter-simplexml。

	当然，别让了在manifest中加入访问网络的权限
*/
/*
	Retrofit与其他请求框架不同的是，它使用了注解。Retrofit的注解分为三大类，分别是HTTP请求方法注解，标记类注解和参数类注解。
	
	其中，HTTP请求方法注解有8种：GET,POST,PUT,DELETE,HEAD,PATCH,OPTIONS,HTTP。
	前7种对应HTTP的请求方法，HTTP则可以替换以上7种，也可以扩展请求方法。
	
	标记类注解有3种：FormUrlEncoded,Multipart,Streaming。
	前2种后面会讲到，Streaming代表响应的数据以流的形式返回，如果不使用它，则默认会把全部数据加载到内存，所以下载大文件时需要加上这个注解。
	
	参数类注解有Header,headers,Body,Path,Field,FieldMap,Part,PartMap,Query,QueryMap等，下面会介绍几种参数类注解的用法：
*/

// 请求访问网络
public interface IpService {
	@GET("getIpInfo.php?ip=59.108.54.37")
	Call<IpModel> getIpMsg();
}
/*
	Retrofit提供的请求方式注解有@GET和@POST等，分别代表GET请求和POST请求，我们在这里用的是GET请求，访问的地址是"getIpInfo.php?ip=59.108.54.37"。
	另外定义了getIpMsg方法，这个方法返回Call<IpModel>类型的参数。接下来创建Retrofit，并创建接口文件，代码如下：
*/
String url = "http://ip.taobao.com/service/";
Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(url)
				// 增加返回值为Json的支持
				.addConverterFactory(GsonConverterFactory.create())
				.build();
IpService ipService = retrofit.create(IpService.class);
Call<IpModel>call = ipService.getIpMsg();
/*
	Retrofit是通过建造者模式构建出来的。请求URL是拼接而成的，它是由baseUrl传入的URL加上请求网络接口的@GET("getIpInfo.php?ip=59.108.54.37")中的URL拼接而成的。
	接下来用Retrofit动态代理获取到之前定义的接口，并调用该接口定义的getIpMsg方法得到Call对象。
	接下来用Call请求网络并处理回调，代码如下所示：
*/
call.enqueue(new Callback<IpModel>()) {
	@Override
	public void onResponse(Call<IpModel> call, Response<IpModel> response) {
		String country = response.body().getData().getCountry();
		Toast.makeText(getApplicationContext(), country, Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onFailure(Call<IpModel> call, Throwable t) {
	}
}
/*
	这里是异步请求网络，回调的Callback是运行在UI线程的。得到返回的Response后将返回数据的country字段用Toast显示出来。
	如果想同步请求网络，请使用call.execute();如果想中断网络请求，则可以使用call.cancel();
*/

/*
	动态配置URL地址：@Path
	请求网络接口代码如下所示：
*/
public interface IpServiceForPath {
	@GET("{path}/getIpInfo.php?ip=59.108.54.37")
	Call<IpModel> getIpMsg(@Path("path") String path);
}
// 在GET注解中包含了{path}，它对应着@Path注解中的"path"，而用来替换{path}的正是需要传入的"String path"的值。请求网络的代码如下所示：
String url = "http://ip.taobao.com";
Retrofit retrofit = new Retrofit.Builder.Builder()
				.baseUrl(url)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
IpServiceForPath ipService = retrofit.create(IpServiceForPath.class);
Call<IpModel> call = ipService.getIpMsg("service"); // 1
call.enqueue(new Callback<IpModel>() {
	@Override
	public void onResponse(Call<IpModel> call, Response<IpModel> response) {
		String country = response.body().getData().getCountry();
		Toast.makeText(getApplicationContext(), country, Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onFailure(Call<IpModel> call, Throwable t) {
	}
})
// 上面代码注释1处，传入"service"来替换@GET注解中的{path}的值。

/*
	动态指定查询条件：@Query
	之前的例子就是为了查询ip的地址位置，每次查询更换不同的ip就可以了，可以用@Query来动态地指定ip的值。
	请求网络接口的代码如下所示：
*/
public interface IpServiceForQuery {
	@GET("getIpInfo.php")
	Call<IpModel> getIpMsg(@Query("ip")String ip);
}
// 请求网络时，只需要传入想要查询的ip值就可以了














