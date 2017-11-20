/*
	为了简化并且更加高质量地在 Activity、Fragment、Thread 和 Service 等之间的通信，同时解决组件之间高耦合的同时能继续高效地通信，事件总线设计出现了。
	提到事件总线我们会想到 EventBus 和 otto，所以本章就来讲解它们的使用方法以及原理。
*/

/*
	EventBus 是一款针对 Android 优化的发布-订阅事件总线。它简化了应用程序内各组件间、组件与后台进程间的通信。其优点是开销小，代码更优雅，以及将发送者和接收者解耦。
	如果 Activity 和Activity 进行交互还好说，但如果 Fragment 和 Fragment 进行交互则着实令人头疼。
	这时我们会使用广播来处理，但是使用广播略嫌麻烦并且效率也不高。如果传递的数据是实体类，需要序列化，那么传递的成本会有点高。
*/

/*
	在讲到EventBus的基本用法之前，我们需要了解 EventBus 的三要素以及它的 4 种 ThreadMode。
	Event：事件。可以是任意类型的对象。
	Subscriber：事件订阅者。在 EventBus 3.0 之前消息处理的方法只能限定于 onEvent、onEventMainThread、onEventBackgroundThread 和 onEventAsync，
		它们分别代表4种线程模型。而在 EventBus 3.0 之后，事件处理的方法可以随便取名，但是需要添加一个注解@Subscribe，并且要指定线程模型（默认为POSTING）。
		4种线程模型下面会讲到。
	Publisher：事件发布者。可以在任意线程任意位置发送事件，直接调用 EventBus 的 post(Object) 方法。可以自己实例化 EventBus 对象，但一般使用 EventBus.getDefault()就可以。
		根据 post 函数参数的类型，会自动调用订阅相应类型事件的函数。

	EventBus 的 4 种 ThreadMode（线程模型）如下：
	
	POSTING（默认）：如果使用事件处理函数指定了线程模型为POSTING，那么该事件是在哪个线程发布出来的，事件处理函数就会在哪个线程中运行，也就是说发布事件和接受事件在同一个线程中。
		在线程模型为POSTING的事件处理函数中尽量避免执行耗时操作，因为它会阻塞事件的传递，甚至有可能会引起ANR。
	MAIN：事件的处理会在UI线程中执行。事件处理的时间不能太长，长了会导致ANR。
	BACKGROUND：如果事件是在UI线程中发布出来的，那么该事件处理函数就会在新的线程中运行；如果事件本来就是在子线程中发布出来的，那么该事件处理函数直接在发布事件的线程中执行。
		在此事件处理函数中禁止进行UI更新操作。
	ASYNC：无论事件在哪个线程中发布，该事件处理函数都会在新建的子线程中执行；同样，此事件处理函数中禁止进行UI更新操作。
*/

/*
	EventBus基本用法
	EventBus使用起来分为以下5个步骤：
*/

// 1 自定义一个事件类
public class MessageEvent {
	...
}

// 2 在需要订阅事件的地方注册事件
EventBus.getDefault().register(this);

// 3 发送事件
EventBus.getDefault().post(MessageEvent);

// 4 处理事件
@Subscribe (threadMode = ThreadMode.MAIN) 
public void XXX(MessageEvent MessageEvent) {
	...
}
// 前面说过，消息处理的方法可以随便取名，但是需要添加一个注解@Subcrvibe，并且要指定线程模型（默认为POSTING）。

// 5 取消事件订阅
EventBus.getDefault().unregister(this);


/*
	EventBus 应用举例
	前面讲到了 EventBus 的基本用法，但是这过于简单，这里举一个例子来应用 EventBus。
*/

/* 
	1 添加依赖库
	首先配置gradle，如下所示：
*/
compile 'org.greenrobot:eventbus:3.0.0'

/*
	2 定义消息事件类
*/
public class MessageEvent {
	private String message;
	public MessageEvent(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}

/*
	3 注册和取消订阅事件
	在MainActivity中注册和取消订阅事件，如下所示：
*/
public class MainActivity extends AppcompatActivity {

	private TextView tv_message;
	private Button bt_message;
	private Button bt_subscription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tv_message = (TextView) this.findViewById(R.id.tv_message);
		tv_message.setText("MainActivity");

		bt_subscription = (Button) this.findViewById(R.id.bt_subscription);
		bt_subscription.setText("注册事件");

		bt_message = (Button) this.findViewById(R.id.bt_message);
		bt_message.setText("跳转到SecondActivity");
		
		bt_message.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SecondActivity.class));
			}
		});

		bt_subscription.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 注册事件
				EventBus.getDefault().register(MainActivity.this);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 取消注册事件
		EventBus.getDefault().unregister(this);
	}
}
// 在 MainActivity 中定义了两个 Button：一个用来注册事件，另一个用来跳转到 SecondActivity。

/*
	4 事件订阅者处理事件
	在 MainActivity 中自定义方法来处理事件，在这里ThreadMode 设置为 MAIN，事件的处理会在UI线程中执行，用 TextView 来展示收到的事件消息：
*/ 
@Subscribe (threadMode = ThreadMode.MAIN)
public void onMoonEvent(MessageEvent messageEvent) {
	tv_message.setText(messageEvent.getMessage());
}

/*
	5 事件发布者发布事件
	创建了SecondActivity来发布消息，代码如下所示：
*/ 
public class SecondActivity extends AppcompatActivity {

	private Button bt_message;
	private TextView tv_message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tv_message = (TextView) this.findViewById(R.id.tv_message);
		tv_message.setText("SecondActivity");

		bt_message = (Button) this.findViewById(R.id.bt_message);
		bt_message.setText("发送事件");
		bt_message.setOnClickListener(new View.setOnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new MessageEvent("欢迎关注刘望舒的博客"));
				finish();
			}
		});
	}
}
/*
	在SecondActivity中，我们定义“发送事件”按钮来发送事件并将 SecondActivity finish掉。
	运行程序，点击MainActivity中的“注册事件”按钮来注册事件，然后点击“跳转到SECONDACTIVITY”按钮，这时跳转到 SecondActivity，接下来点击“发送事件”按钮，
	这时 SecondActivity 被 finish 掉，因此界面展示的是 MainActivity，可以看到 MainActivity 的 TextView 显示“欢迎关注刘望舒的博客”，
	MainActivity 成功地收到了 SecondActivity 发送的事件。
*/

/*
	6 ProGuard 混淆规则 
	最后不要忘了在 ProGuard 中加入混淆规则：
*/ 
-keepattributes *Annotation*
-keepclassmembers class ** {
	@org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
	<init> (java.lang.Throwable);
}

/*
	EventBus 的黏性事件
	除了上面讲的普通事件外，EventBus 还支持发送黏性事件，就是在发送事件之后再订阅该事件也能收到该事件，这跟黏性广播类似。
	为了验证黏性事件，我们修改以前的代码如下所示：
*/

/*
	1.订阅者处理黏性事件
	在 MainActivity 中新写一个方法用来处理黏性事件：
*/
Subscribe(threadMode = ThreadMode.POSTING, sticky = true) 
public void ononMoonStickyEvent(MessageEvent messageEvent) {
	tv_message.setText(messageEvent.getMessage());
}

/*
	2.发送黏性事件
	在 SecondActivity 中定义一个 Button 来发送黏性事件：
*/
bt_subscription.setOnClickListener(new View.OnClickListener() {
	@Override
	public void onClick(View v) {
		EventBus.getDefault().postSticky(new MessageEvent("黏性事件"));
		finish();
	}
})
/*
	现在运行代码看看效果。首先，我们在 MainActivity 中并没有点击“注册事件”按钮，
	而是直接跳到 SecondActivity 中点击发送“黏性事件”按钮。这时界面回到 MainActivity，
	我们看到 TextView 仍旧显示着 MainActivity 的字段，这是因为我们现在还没有订阅事件。
	接下来我们点击“注册事件”按钮，TextView内容发生改变，显示“黏性事件”，说明黏性事件被成功接收到了。 
*/