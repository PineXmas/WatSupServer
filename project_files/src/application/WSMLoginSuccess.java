package application;

import java.net.Socket;

public class WSMLoginSuccess extends WSMNoData {

	public WSMLoginSuccess(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
	}
	
	public WSMLoginSuccess() {
		super(WSMCode.OPCODE_LOGIN_SUCCESS);
	}

}
