// 结果回调接口
public interface OnLoginListener {
	void loginSuccess(User user);
	void loginFailed();
}