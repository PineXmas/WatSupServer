package application;

import java.util.ArrayList;

/**
 * Store information regarding to a chat room in WatSup. The information includes:
 * - list of users
 * - room name
 * - list of messages
 * @author pinex
 *
 */
public class ChatRoom {
	public ArrayList<WSClientHandler> listUsers = new ArrayList<>();
	public ArrayList<ChatMessage> listMsgs = new ArrayList<>();
	public String roomName;
	
	public ChatRoom(String roomName) {
		this.roomName = roomName;
	}
	
	public void addUser(WSClientHandler newUser) {
		listUsers.add(newUser);
	}
	
	public void addMessage(ChatMessage newMsg) {
		listMsgs.add(newMsg);
	}
	
	/**
	 * Generate LIST-USERS-RESP message ready to send to a client.
	 * @return
	 */
	public WSMListUsersResp genListUsersMsg() {
//		String[] arrUserNames = new String[listUsers.size()];
//		for (int i = 0; i < listUsers.size(); i++) {
//			arrUserNames[i] = listUsers.get(i).userName;
//		}
//		
//		return new WSMListUsersResp(roomName, arrUserNames);
		
		return null;
	}
	
	/**
	 * Get all messages in this room.
	 * @return
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[" + roomName + "]\n");
		for (int i = 0; i < listMsgs.size(); i++) {
			sb.append(listMsgs.get(i).toString() + "\n");
		}
		
		return sb.toString();
	}
}
