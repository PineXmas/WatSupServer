package application;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Used as base class for 3 messages: SEND_PRIVATE_MSG, TELL_PRIVATE_MSG & SEND_ROOM_MSG, since they all require only 1 label for sender/receiver's name
 * @author pinex
 *
 */
public class WSMOneLabelChatMsg extends WSMessage {

	public String roomName;
	
	public WSMOneLabelChatMsg(int opcode, int dataLength, byte[] msgBytes, Socket sender) {
		super(opcode, dataLength, msgBytes, sender);
	}
	
	public WSMOneLabelChatMsg(String roomName) {
		
		//opcode
		opcode = WSMCode.OPCODE_JOIN_ROOM;
		
		//data length
		dataLength = WSSettings._LABEL_SIZE;
		
		//user name
		int nameLength = Math.min(roomName.length(), WSSettings._LABEL_SIZE);
		this.roomName = roomName.substring(0, nameLength);
		
		//message-byte-array
		msgBytes = ByteBuffer.allocate(8 + WSSettings._LABEL_SIZE)
				.putInt(opcode.rawCode)
				.putInt(dataLength)
				.put(roomName.getBytes(), 0, nameLength).array();
	}

	@Override
	public void parse2Attributes() {
		roomName = parse2String(msgBytes, 8, msgBytes.length);
	}

	@Override
	public String toString() {
		
		return opcode + "|" +  dataLength + "|" + roomName;
	}

	@Override
	public String toStringOfBytes() {
		return 	super.toStringOfBytes() + "|" +
				displayBytes(msgBytes, 8, msgBytes.length-8)
				;
	}

}
