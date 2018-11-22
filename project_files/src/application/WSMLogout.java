package application;

import java.net.Socket;

public class WSMLogout extends WSMNoData {

	public WSMLogout(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
	}
	
	public WSMLogout() {
		super(WSMCode.OPCODE_LOGOUT);
	}

}
