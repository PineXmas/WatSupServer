package application;

import java.net.Socket;

public class WSMDummy extends WSMessage {
	public String dataAsText;
	
	public WSMDummy(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
	}

	@Override
	public void parseToAttributes() {
		dataAsText = new String(data, 8, dataLength);
	}

	@Override
	public String toString() {
		return opcode + "|" + dataLength + "|" + dataAsText;
	}

}
