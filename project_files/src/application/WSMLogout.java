package application;

import java.net.Socket;

public class WSMLogout extends WSMessage {

	public WSMLogout(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parse2Attributes() {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
