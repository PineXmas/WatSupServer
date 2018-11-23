package application;

import java.net.Socket;

public class WSMSendPrivateMsg extends WSMOneLabelChatMsg {
	public WSMSendPrivateMsg(int opcode, int dataLength, byte[] msgBytes, Socket sender) {
		super(opcode, dataLength, msgBytes, sender);
	}
	
	public WSMSendPrivateMsg(String name, String chatContent) {
		super(WSMCode.OPCODE_SEND_PRIVATE_MSG, name, chatContent);
	}
}
