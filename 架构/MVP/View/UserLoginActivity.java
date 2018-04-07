public class UserLoginActivity extends ActionBarActivity implements IUserLoginView {
	private EditText mEtUsername, mEtPassword;
	private Button mBtnLogin, mBtnClear;
	private ProgressBar mPbLoading;
	private UserLoginPresenter mUserLoginPresenter = new UserLoginPresenter(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_login);
		initView();
	}

	private void initView() {
		mEtUsername = findViewById(R.id.id_et_username);
		mEtPassword = findViewById(R.id.id_et_password);
		mBtnClear = findViewById(R.id.btn_clear);
		mBtnLogin = findViewById(R.id.btn_login);
		mPbLoading = findViewById(R.id.pb_loading);
		mBtnLogin.setOnClickListener(new View.setOnClickListener() {
			@Override
			public void onClick(View v) {
				mUserLoginPresenter.login();
			}
		});
		mBtnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mUserLoginPresenter.clear();
			}
		})
	}

	@Override
	public String getUserName() {
		return mEtUsername.getText().toString();
	}

	@Override
	public String getPassword() {
		return mEtPassword.getText().toString();
	}

	@Override
	public void clearUserName() {
		mEtUsername.setText("");
	}

	@Override
	public String clearPassword() {
		mEtPassword.setText("");
	}

	@Override
	public void showLoading() {
		mPbLoading.setVisibility(View.VISIBILE);
	}

	@Override
	public void toMainActivity() {
		Toast.makeText(this, "login success, to MainActivity", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showFailedError() {
		Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show();
	}
}