package application;

import java.net.Socket;

public class WSMKeepAlive extends WSMNoData {

	public WSMKeepAlive(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
	}
	
	public WSMKeepAlive() {
		super(WSMCode.OPCODE_KEEPALIVE);
	}

}
