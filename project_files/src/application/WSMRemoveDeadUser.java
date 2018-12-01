package application;

/**
 * This class serves as a signal to stop the receiver-thread in the server side
 * @author pinex
 *
 */
public class WSMRemoveDeadUser extends WSMessage {
	String userName = null;
	WSClientHandler userHandler = null;
	
	public boolean hasUserName() {
		return userName == null ? false : true;
	}
	
	public WSMRemoveDeadUser(String userName) {
		this.userName = userName;
	}
	
	public WSMRemoveDeadUser(WSClientHandler userHandler) {
		this.userHandler = userHandler;
	}

	@Override
	public void parse2Attributes() {
	}

	@Override
	public String toString() {
		return "WSMRemoveDeadUser: " + userName;
	}

}
