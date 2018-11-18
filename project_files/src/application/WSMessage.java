package application;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class WSMessage {
	//PREDEFINED OPCODES
	static final int OPCODE_UNKNOWN = -1;
	static final int OPCODE_DUMMY = 0;
	static final int OPCODE_ERROR = 1001;
	static final int OPCODE_KEEPALIVE = 1002;
	static final int OPCODE_LOGIN = 1003;
	static final int OPCODE_LOGOUT = 1004;
	static final int OPCODE_LOGIN_SUCCESS = 1005;
	static final int OPCODE_LIST_ROOMS = 1006;
	static final int OPCODE_LIST_ROOMS_RESP = 1007;
	static final int OPCODE_LIST_USERS_RESP = 1008;
	static final int OPCODE_JOIN_ROOM = 1009;
	static final int OPCODE_LEAVE_ROOM = 1010;
	static final int OPCODE_SEND_ROOM_MSG = 1011;
	static final int OPCODE_TELL_ROOM_MSG = 1012;
	
	//MEMBERS
	int opcode;
	int dataLength;
	byte[] data;
	
	//METHODS
	public WSMessage(int opcode, int dataLength, byte[] data) {
		this.opcode = opcode;
		this.dataLength = dataLength;
		this.data = data;
		
		parseToAttributes();
	}
	
	/**
	 * Concatenate opcode +  data-length + data => message ready to send
	 * @return the message or NULL if error occurs
	 */
	public byte[] formatToSend() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(ByteBuffer.allocate(4).putInt(opcode).array());
			outputStream.write(ByteBuffer.allocate(4).putInt(dataLength).array());
			outputStream.write(data);
			return outputStream.toByteArray();
		} catch (Exception e) {
			ErrandBoy.printlnError(e, "Error when formating message to send");
			return null;
		}
	}
	
	/**
	 * Parse the data-bytes into different attributes, depending on particular message types.
	 */
	public abstract void parseToAttributes();
	
	// DEBUG: test outputStream.write(int)
	// TODO force char to be 1 byte
	static byte[] dummyData = "this is dummy text!".getBytes();
	
	public static void main() {
		WSMDummy msgDummy = new WSMDummy(2100000000, dummyData.length, dummyData);
		byte[] ready2Send = msgDummy.formatToSend();
		System.out.println(new String(ready2Send));
		
		System.out.println("size of char = " + Character.BYTES);
		System.out.println("size of  int = " + Integer.BYTES);
		System.out.println("size of long = " + Long.BYTES);
	}
}
