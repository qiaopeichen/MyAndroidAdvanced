// 定义业务接口
public interface IUserBiz {
	public void login(String username, String password, OnLoginListener loginListener);
}