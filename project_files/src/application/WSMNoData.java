package application;

import java.net.Socket;

public abstract class WSMNoData extends WSMessage {

	public WSMNoData(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
	}
	
	public WSMNoData(WSMCode opcode) {
		this.opcode = opcode;
		dataLength = 0;
		msgBytes = genBasicMsgBytes(opcode.rawCode, dataLength);
	}

	@Override
	public void parse2Attributes() {
	}

	@Override
	public String toString() {
		return opcode + "|" +  dataLength;
	}

}
