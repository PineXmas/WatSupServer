package application;

import java.net.Socket;

public class ChatClient {
	public String userName;
	
	/**
	 * The socket this client/user is bound to
	 */
	public Socket socket;
	
	public String toString() {
		return userName + "@" + WSClientHandler.getClientName(socket);
	}
}
