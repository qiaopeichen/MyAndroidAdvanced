/*
	otto 是 Square 公司发布的一个发布-订阅模式框架，它基于 Google Guava 项目中的 eventbus 模块开发，针对 Android 平台做了优化和加强。
	虽然 Square 已经停止了对 otto 的更新并推荐使用 RxJava 和 RxAndroid 来替代它，但是 otto 的设计理念和源码仍旧值得我们学习。
*/

/*
	使用otto	
*/

// 1 添加依赖库，首先配置 gradle，如下所示
compile 'com.squareup:otto:1.3.8'

// 2 定义消息类
// 与 EventBus一样，我们接着定义消息类，它是一个bean文件，如下所示：
public class BusData {
	public String message;
	public BusData (String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}

// 3 单例封装Bus
// otto 的 Bus 类相当于 EventBus 中的 EventBus 类，它封装了 otto 的主要功能。但它不是一个单例，其每次都要用 new 创建出来，这样显然不是很方便。因此我们用单例模式将它封装起来，如下所示：
public class OttoBus extends Bus {
	private volatile static OttoBus bus;
	private OttoBus() {
	}
	public static OttoBus getInstance() {
		if (bus == null) {
			synchronized (OttoBus.class) {
				if (bus == null) {
					bus = new OttoBus();
				}
			}
		}
		return bus;
	}
}

// 4 注册和取消注册订阅事件
// otto 同样需要注册和取消注册订阅事件，通过 OttoBus 得到 Bus 对象，调用 Bus 的 register 和 unregister 方法来注册和取消注册，同时我们定义一个 Button，点击这个 Button 跳转到 SecondActivity，SecondActivity 用来发送事件：
public class MainActivity extends AppCompatActivity {
	private Button bt_jump;
	private TextView tv_message;
	private Bus bus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate();
		setContentView(R.layout.activity_main);
		tv_message = (TextView) this.findViewById(R.id.tv_message);
		bt_jump = (Button) this.findViewById(R.id.bt_jump);
		bt_jump.setText("跳转到SecondActivity");
		bt_jump.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SecondActivity.class));
			}
		});
		bus = OttoBus.getInstance();
		bus.register(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bus.unregister(this);
	}
}

// 5 事件订阅者处理事件
// 它和 EventBus 一样用 @Subscribe 来订阅事件，在 MainActivity 中添加如下代码：
@Subscribe
public void setContent(BusData data) {
	tv_message.setText(data.getMessage());
}
// 同样地用 textView 来现实接收到的消息。

// 6 使用 post 发送事件
// 创建 SecondActivity，并设置一个 Button，点击发送事件，并 finish 掉自身，如下所示：
public class SecondActivity extends AppCompatActivity {
	private Button bt_jump;
	private OttoBus bus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bt_jump = (Button) this.findViewById(R.id.bt_jump);
		bt_jump.setText("发送事件");
		bt_jump.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OttoBus.getInstance().post(new BusData("事件更新"));
				finish();
			}
		});
	}
}
// 这样我们运行程序，点击 MainActiviy 的“跳转到 SECONDACTIVITY”按钮直接跳转到 SecondActivity，再点击“发送事件”按钮，SecondActivity被finish掉，回到MainActivity，MainActivity中textView的文字变为“事件更新”。

// 7 使用 @Produce 来发布事件
// Produce 注解用于生产发布事件。需要注意的是，它生产事件前需要进行注册，并且在生产完事件后需要取消注册。如果使用这种方法，则在跳转到发布者所在的类中时会立即产生事件并触发订阅者，修改 SecondActivity，代码如下所示：
public class SecondActivity extends AppCompatActivity {
	private Button bt_jump;
	private OttoBus bus;

	@Override
	protected void onCreate(Bundle) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bt_jump = (Button) this.findViewById(R.id.bt_jump);
		bt_jump.setText("发送事件");
		bt_jump.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		bus = OttoBus.getInstance();
		bus.register(this);
	}
	
	@Produce
	public BusData setInitialContent() {
		return new BusData("事件更新");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bus.unregister(this);
	}
}
// 在 MainActivity 跳转到SecondActivity 时，MainActivity 会马上收到邮件。
