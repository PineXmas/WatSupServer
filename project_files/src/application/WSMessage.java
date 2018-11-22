package application;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class WSMessage {
	
	/***********************************************
	 * [MEMBERS]
	 ***********************************************/
	
	WSMCode opcode;
	int dataLength;
	byte[] data;
	Socket sender;
	
	/***********************************************
	 * [STATIC METHODS]
	 ***********************************************/
	
	/** 
	 * Determine opcode from the given byte-array
	 * @param arrBytes the bytes read so far from the input stream, could be empty
	 * @return the opcode or -1 if not enough byte to determine
	 */
	static public int getOpcode(byte[] arrBytes) {
		if (arrBytes.length < 4) {
			return -1;
		}
		
		return ByteBuffer.allocate(4).put(arrBytes, 0, 4).getInt(0);
	}
	
	/** 
	 * Determine data-length from the given byte-array
	 * @param arrBytes the bytes read so far from the input stream, could be empty
	 * @return the data-length or -1 if not enough byte to determine
	 */
	static public int getDataLength(byte[] arrBytes) {
		if (arrBytes.length < 8) {
			return -1;
		}
		
		return ByteBuffer.allocate(4).put(arrBytes, 4, 4).getInt(0);
	}
	
	/** 
	 * Determine remaining-byes-to-read from the given byte-array, return -1 if not enough byte to determine
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
		return parse2Msgs(arrReadBytes, arrIncompleteMsg, listNewIncompleteMsg, null);
	}
	
	/**
	 * Parse the given byte-array into messages (and return leftover bytes if available)
	 * @param arrReadBytes array of bytes read from the input stream
	 * @param arrIncompleteMsg array of bytes from an incomplete message
	 * @param listNewIncompleteMsg list of bytes left after this operation (an incomplete message)
	 * @param sender the socket where the message is received (optional, could be NULL)
	 * @return list of 0 or more complete messages
	 */
	static public ArrayList<WSMessage> parse2Msgs(byte[] arrReadBytes, byte[] arrIncompleteMsg, ArrayList<Byte> listNewIncompleteMsg, Socket sender){
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
			WSMDummy msgDummy = new WSMDummy(currOpcode, currDataLength, ErrandBoy.convertList2Array(listNewIncompleteMsg), sender);
			listComplete.add(msgDummy);
			listNewIncompleteMsg.clear();
		}
		
		return listComplete;
	}
	
	/**
	 * Parse the given bytes into a label, which could be used for user name or room name
	 * @param arrBytes the bytes containing the label 
	 * @return
	 */
	static public String parse2String(byte[] arrBytes) {
		int length = -1;
		for (int i = 0; i < arrBytes.length; i++) {
			if (arrBytes[i] == 0) {
				length = i;
				break;
			}
		}
		
		if (length == -1) {
			return new String(arrBytes);
		}
		
		return new String(arrBytes, 0, length);
	}
	
	/***********************************************
	 * [METHODS]
	 ***********************************************/
	
	public WSMessage() {
	}
	
	public WSMessage(int opcode, int dataLength, byte[] data) {
		this(opcode, dataLength, data, null);
	}
	
	/**
	 * This constructor mostly is used in case the message is received from a socket-input-stream
	 * @param opcode first 4 byte of the message
	 * @param dataLength next 4 byte of the message
	 * @param data remaining bytes of the message
	 * @param sender the socket where the message is received (optional, could be NULL)
	 */
	public WSMessage(int opcode, int dataLength, byte[] data, Socket sender) {
		this.opcode = WSMCode.GetCode(opcode);
		this.dataLength = dataLength;
		this.data = data;
		this.sender = sender;
		
		parse2Attributes();
	}
	
	/**
	 * Concatenate opcode +  data-length + data => message ready to send
	 * @return the message or NULL if error occurs
	 */
	public byte[] formatToSend() {
		// TODO (NOTE) if length of chars cause problem, consider changing to 2-byte chars
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(ByteBuffer.allocate(4).putInt(opcode.rawCode).array());
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
	public abstract void parse2Attributes(); 
	
	/**
	 * Present the message in human-readable text
	 */
	public abstract String toString();
	
	// DEBUG: test functions in this WSMessage class
	public static void main() {
		
	}
}
