public class MainActivity extends ActionBarActivity implements OnWeatherListener, View.OnClickListener {
	private WeatherModel weatherModel;
	private EditText cityNOInput;
	private TextView city;
	...

	@Override
	protected void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		setContentView(R.layout.activity_main);
		weatherModel = new WeatherModelImpl();
		initView();
	}

	// 初始化View
	private void initView() {
		cityNOInput = findView(R.id.et_city_no);
		city = findView(R.id.tv_city);
		...
		findView(R.id.btn_go).setOnClickListener(this);
	}

	// 显示结果
	public void displayResult(Weather weather) {
		WeatherInfo weatherInfo = weather.getWeatherInfo();
		city.setText(Weather.getCity());
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btn_go:
				weatherModel.getWeather(cityNOInput.getText().toString().trim(), this);
				break;
		}
	}

	@Override
	public void onSuccess(Weather weather) {
		displayResult(weather);
	}

	@Override
	public void onError() {
		Toast.makeText(this, "获取天气失败", Toast.LENGTH_SHORT).show();
	}

	private T findView(int id) {
		return (T) findView(id);
	}
}