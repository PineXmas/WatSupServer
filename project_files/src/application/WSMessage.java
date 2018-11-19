package application;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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
	
	//PREDEFINED ERROR CODES
	
	//MEMBERS
	int opcode;
	int dataLength;
	byte[] data;
	Socket sender;
	
	//STATIC METHODS
	
	/** Determine opcode from the given byte-array
	 * 
	 * @param arrBytes the bytes read so far from the input stream, could be empty
	 * @return the opcode or -1 if not enough byte to determine
	 */
	static public int getOpcode(byte[] arrBytes) {
		if (arrBytes.length < 4) {
			return -1;
		}
		
		return ByteBuffer.allocate(4).put(arrBytes, 0, 4).getInt(0);
	}
	
	/** Determine data-length from the given byte-array
	 * 
	 * @param arrBytes the bytes read so far from the input stream, could be empty
	 * @return the data-length or -1 if not enough byte to determine
	 */
	static public int getDataLength(byte[] arrBytes) {
		if (arrBytes.length < 8) {
			return -1;
		}
		
		return ByteBuffer.allocate(4).put(arrBytes, 4, 4).getInt(0);
	}
	
	/** Determine remaining-byes-to-read from the given byte-array, return -1 if not enough byte to determine
	 * 
	 * @param arrBytes the bytes read so far from the input stream, could be empty
	 * @return the remaining-byes-to-read or -1 if not enough byte to determine
	 */
	static public int getRemainingBytes2Read(byte[] arrBytes) {
		if (arrBytes.length < 8) {
			return -1;
		}
		
		int need2Read = getDataLength(arrBytes);
		int readSoFar = arrBytes.length - 8;
		
		return need2Read - readSoFar;
	}	
	
	/**
	 * Parse the given byte-array into messages (and return leftover bytes if available)
	 * @param arrReadBytes array of bytes read from the input stream
	 * @param arrIncompleteMsg array of bytes from an incomplete message
	 * @param listNewIncompleteMsg list of bytes left after this operation (an incomplete message)
	 * @return list of 0 or more complete messages
	 */
	static public ArrayList<WSMessage> parse2Msgs(byte[] arrReadBytes, byte[] arrIncompleteMsg, ArrayList<Byte> listNewIncompleteMsg){
		ArrayList<WSMessage> listComplete = new ArrayList<>();
		int currPos = 0;
		int currOpcode = -1;
		int currDataLength = -1;
		int remainingBytes = -1;
		
		//init the new-incomplete to be the curr-incomplete
		for (int i = 0; i < arrIncompleteMsg.length; i++) {
			listNewIncompleteMsg.add(arrIncompleteMsg[i]);
		}
		
		//determine opcode, data-length, remaining-bytes-to-read
		currOpcode = getOpcode(arrIncompleteMsg);
		currDataLength = getDataLength(arrIncompleteMsg);
		remainingBytes = getRemainingBytes2Read(arrIncompleteMsg);
		
		
		//read bytes & construct messages
		while (currPos < arrReadBytes.length) {
			
			//check for complete message: add to complete-list & reset the incomplete message
			if (currOpcode != -1 && currDataLength != -1 && remainingBytes == 0) {
				//TODO create specific message based on its opcode here. Return dummy message for now
				/*
				 * the function will look like this: create-specific-message(opcode, data-length, data)
				 */
				WSMDummy msgDummy = new WSMDummy(currOpcode, currDataLength, ErrandBoy.convertList2Array(listNewIncompleteMsg), null);
				listComplete.add(msgDummy);
				
				currOpcode = -1;
				currDataLength = -1;
				remainingBytes = -1;
				listNewIncompleteMsg.clear();
			}
			
			//read next byte
			Byte currByte = arrReadBytes[currPos];
			listNewIncompleteMsg.add(currByte);
			currPos++;
			
			//read opcode if not yet
			if (currOpcode == -1) {
				currOpcode = getOpcode(ErrandBoy.convertList2Array(listNewIncompleteMsg));
				continue;
			}
			
			//read data-length if not yet
			if (currDataLength == -1) {
				currDataLength = getDataLength(ErrandBoy.convertList2Array(listNewIncompleteMsg));
				
				//compute remaining-bytes-to-read
				remainingBytes = getRemainingBytes2Read(ErrandBoy.convertList2Array(listNewIncompleteMsg));
				
				continue;
			}
			
			//decrease remaining-bytes-to-read
			--remainingBytes;
		}
		
		//try to finalize the incomplete message before exit
		if (currOpcode != -1 && currDataLength != -1 && remainingBytes == 0) {
			//TODO create specific message based on its opcode here. Return dummy message for now
			/*
			 * the function will look like this: create-specific-message(opcode, data-length, data)
			 */
			WSMDummy msgDummy = new WSMDummy(currOpcode, currDataLength, ErrandBoy.convertList2Array(listNewIncompleteMsg), null);
			listComplete.add(msgDummy);
			listNewIncompleteMsg.clear();
		}
		
		return listComplete;
	}
	
	//METHODS
	
	public WSMessage(int opcode, int dataLength, byte[] data) {
		this(opcode, dataLength, data, null);
	}
	
	/**
	 * Constructor
	 * @param opcode
	 * @param dataLength
	 * @param data
	 * @param sender the socket sending this message
	 */
	public WSMessage(int opcode, int dataLength, byte[] data, Socket sender) {
		this.opcode = opcode;
		this.dataLength = dataLength;
		this.data = data;
		this.sender = sender;
		
		parseToAttributes();
	}
	
	/**
	 * Concatenate opcode +  data-length + data => message ready to send
	 * @return the message or NULL if error occurs
	 */
	public byte[] formatToSend() {
		// TODO (NOTE) if length of chars cause problem, consider changing to 2-byte chars
		
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
	
	/**
	 * Present the message in human-readable text
	 */
	public abstract String toString();
	
	// DEBUG: test functions in this WSMessage class
	public static void main() {
		ArrayList<Byte> listEmpty = new ArrayList<>();
		byte[] arrEmpty = ErrandBoy.convertList2Array(listEmpty);
		System.out.println(arrEmpty.length);
	}
}
