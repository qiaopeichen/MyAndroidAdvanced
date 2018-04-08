public class WeatherModelImpl implements WeatherModel {
	@Override
	public void getWeather(String cityNumber, final OnWeatherListener listener) {
		/*数据层操作*/
		VolleyRequest.newInstance().newGsonRequest(http://www.weather.com.cn/data/sk + cityNumber + .html,
			Weather.class, new Response.Listener<weather>() {
				@Override
				public void onResponse(Weather weather) {
					if (weather != null) {
						listener.onSuccess(weather);
					} else {
						listener.onError();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						listener.onError();
					}
				});
	}
}