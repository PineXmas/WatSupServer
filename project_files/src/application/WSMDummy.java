package application;

import java.net.Socket;

public class WSMDummy extends WSMessage {
	public String dataAsText;
	
	/**
	 * This constructor mostly is used in case the message is received from a socket-input-stream
	 * @param opcode first 4 byte of the message
	 * @param dataLength next 4 byte of the message
	 * @param data remaining bytes of the message
	 * @param sender the socket where the message is received (optional, could be NULL)
	 */
	public WSMDummy(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
	}

	@Override
	public void parse2Attributes() {
		dataAsText = new String(data, 8, dataLength);
	}

	@Override
	public String toString() {
		return opcode + "|" + dataLength + "|" + dataAsText;
	}

}
