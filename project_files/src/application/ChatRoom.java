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
	
	/**
	 * Try to add the new user if not joined yet
	 * @param newUser
	 * @return TRUE if the user is added to room (new user), otherwise return FALSE
	 */
	public boolean addUser(WSClientHandler newUser) {
		if (searchUser(newUser.userName) >= 0) {
			return false;
		}
		
		listUsers.add(newUser);
		return true;
	}
	
	public void removeUser(int index) {
		listUsers.remove(index);
	}
	
	public void addMessage(ChatMessage newMsg) {
		listMsgs.add(newMsg);
	}

	public int searchUser(String userName) {
		return WSServer.searchUser(userName, listUsers);
	}
	
	/**
	 * Generate LIST-USERS-RESP message ready to send to a client.
	 * @return
	 */
	public WSMListUsersResp genListUsersRespMsg() {
		String[] arrUserNames = new String[listUsers.size()];
		for (int i = 0; i < listUsers.size(); i++) {
			arrUserNames[i] = listUsers.get(i).userName;
		}
		
		return new WSMListUsersResp(roomName, arrUserNames);
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
