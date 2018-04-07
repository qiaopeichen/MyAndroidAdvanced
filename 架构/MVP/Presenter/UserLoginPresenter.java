public class UserLoginPresenter {
	private IUserBiz userBiz;
	private IUserLoginView userLoginView;
	private Handler mHandler = new Handler();
	// Presenter必须要能拿到View和Model的实现类
	public UserLoginPresenter(IUserLoginView userLoginView) {
		this.userLoginView = userLoginView;
		this.userBiz = new userBiz;
	}

	public void login() {
		userLoginView.showLoading();
		userBiz.login(userLoginView.getUserName(), userLoginView.getPassword(), new OnLoginListener() {
			@Override
			public void loginSuccess(final User user) {
				// 需要在UI线程执行
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						userLoginView.toMainActivity(user);
						userLoginView.hideLoading();
					}
				});
			}
			@Override
			publilc void loginFailed() {
				// 需要在UI线程中执行
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						userLoginView.showFailedError();
						userLoginView.hideLoading();
					}
				})
			}
		})
	}

	public void clear() {
		userLoginView.clearUserName();
		userLoginView.clearPassword();
	}
}