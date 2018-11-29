package application;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public class WSServer {
	
	/***********************************************
	 * MEMBERS
	 ***********************************************/
	
	//chat data
	ArrayList<ChatRoom> listRooms = new ArrayList<>();
	ArrayList<WSClientHandler> listClientSockets = new ArrayList<>();
	int portNumber;
	int maxRooms;
	int maxUsers;
	
	//server socket
	ServerSocket listeningSocket;
	Boolean isServerRunning;
	Object lock_isServerRunning = new Object();
	LinkedBlockingQueue<WSMessage> receivingMsgQueue;
	Thread msgReceiver;
	Thread connectionListener;
	
	
	/***********************************************
	 * CONSTRUCTORS
	 ***********************************************/
	
	public WSServer() {
		this(WSSettings._DEFAULT_PORT, WSSettings._MAX_ROOMS, WSSettings._MAX_USERS);
	}
	
	public WSServer(int portNumber, int maxRooms, int maxUsers) {
		
		//initialization
		this.portNumber = portNumber;
		this.maxRooms = maxRooms ;
		this.maxUsers = maxUsers ;
		isServerRunning = false;
		receivingMsgQueue = new LinkedBlockingQueue<>();
		
		/*
		 * set up threads 
		 */
		
		//listening thread
		connectionListener = new Thread(new Runnable() {
			@Override
			public void run() {
				if (listeningSocket == null) {
					ErrandBoy.println("Socket is null, could not listen");
					return;
				}
				
				try {

					// TODO right now the only way to stop this thread is to interrupt it from outside. Consider keep/improve this
					
					//mark the server in being running
					synchronized (isServerRunning) {
						isServerRunning = true;	
					}
					
					//keep listening for new connection
					ErrandBoy.println("Start server socket at port " + listeningSocket.getLocalPort());
					while (true) {
						ErrandBoy.println("Waiting for new connection...");
						Socket clientSocket = listeningSocket.accept();
						ErrandBoy.println("Connected to client at " + WSClientHandler.getClientName(clientSocket) + clientSocket.getPort());
						
						//assign client to a dealer for further handling
						WSClientHandler dealer = new WSClientHandler(clientSocket, receivingMsgQueue);
						listClientSockets.add(dealer);
						dealer.start();
					}
				} catch (Exception e) {
					ErrandBoy.printlnError(e, "Error when waiting for new connection");
				} finally {
					//try stopping the listening socket
					
					try {
						if (!listeningSocket.isClosed()) {
							listeningSocket.close();
						}
					} catch (Exception e) {
						ErrandBoy.printlnError(e, "Error when closing listening socket");
					} finally {
						listeningSocket = null;
						synchronized (isServerRunning) {
							isServerRunning = false;	
						}
						ErrandBoy.println("Server socket has been closed successfully");
					}
				}
				
			}
		});
		
		//message receiver thread
		msgReceiver = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (receivingMsgQueue == null) {
					ErrandBoy.println("Received-message queue is not init. Receiver quits.");
					return;
				}
				
				try {
					ErrandBoy.println("Waiting to process messages in the queue...");
					WSMessage msg;
					int found;
					ChatRoom aRoom;
					
					while ( !((msg = receivingMsgQueue.take()) instanceof WSMStopSerer)) {
						try {
							ErrandBoy.println("Client " + msg.clientHandler.getName() + " sent: " + msg.toString());

							//do nothing if the message (somehow) does not have any bound client-handler
							if (msg.clientHandler == null) {
								continue;
							}
							
							//process LOG_IN message 
							if (msg.opcode == WSMCode.OPCODE_LOGIN) {
								/*
								 * - check for name duplication
								 * - possible messages to send: NAME_EXISTS, TOO_MANY_USERS, LOGIN_SUCCESS
								 * - if success --> assign the user name inside the message to the sender socket
								 * - notify all users about the new user
								 */
								WSMLogin msgLogin = (WSMLogin) msg;
								
								//case 0: do nothing if a logged-in user send another LOG_IN message
								if (msgLogin.clientHandler.userName != null) {
									ErrandBoy.println("An already logged-in user sends another LOG_IN message. Ignore the message.");
									continue;
								}
								
								//case 1: TOO_MANY_USERS
								if (listClientSockets.size() >= WSSettings._MAX_USERS) {
									msgLogin.clientHandler.enqueueMessage(new WSMError(WSMCode.ERR_TOO_MANY_USERS));
									continue;
								}
								
								//case 2: NAME_EXISTS
								found = searchUser(msgLogin.userName, listClientSockets);
								if (found >= 0) {
									msgLogin.clientHandler.enqueueMessage(new WSMError(WSMCode.ERR_NAME_EXISTS));
									continue;
								}
								
								//case 3: LOGIN_SUCCESS
								msgLogin.clientHandler.userName = msgLogin.userName;
								msgLogin.clientHandler.enqueueMessage(new WSMLoginSuccess());
								
								//notify all users about the new user
								sendAllUsers(genListUsersAllMsg());
								
								continue;
							}
							
							//check logged-in condition before processing other message types
							if (!checkLoggedIn(msg)) {
								ErrandBoy.println("A never-logged-in user sends message different from LOG_IN. Ignore the message.");
								continue;
							}
							
							//process other messages
							switch (msg.opcode) {
							
							case OPCODE_LOGOUT:
								/*
								 * - remove the user out of WatSup
								 * - remove the user out of its joined rooms
								 * 		* for each such room, notify all users in the room know
								 * - send LIST_USERS_ALL to all users in WatSup since list of all users has changed		
								 */
								WSMLogout msgLogout = (WSMLogout) msg;
								ArrayList<ChatRoom> listJoinedRooms = removeUser(msgLogout.clientHandler.userName);
								for (ChatRoom chatRoom : listJoinedRooms) {
									WSMListUsersResp msgNewUserList = chatRoom.genListUsersRespMsg();
									for (WSClientHandler clientHandler : chatRoom.listUsers) {
										clientHandler.enqueueMessage(msgNewUserList);
									}
								}
								sendAllUsers(genListUsersAllMsg());
								
								break;
								
							case OPCODE_LIST_ROOMS:
								/*
								 * - send back the list of room names
								 * - for each room, send back the list of users in each room
								 */
								WSMListRooms msgListRooms = (WSMListRooms)msg;
								msgListRooms.clientHandler.enqueueMessage(genListRoomsRespMsg());
								for (ChatRoom chatRoom : listRooms) {
									msgListRooms.clientHandler.enqueueMessage(chatRoom.genListUsersRespMsg());
								}
								
								break;
								
							case OPCODE_JOIN_ROOM:
								/*
								 * check for max rooms reached --> send back TOO_MANY_ROOMS
								 * 
								 * search for the room name
								 * - if exist: add the client to the room
								 * - if not: create the room, add the client to the room
								 * 
								 * notify all users about the new room
								 */
								WSMJoinRoom msgJoinRoom = (WSMJoinRoom)msg;
								found = searchRoom(msgJoinRoom.roomName);
								if (found >= 0) {
									 listRooms.get(found).addUser(msgJoinRoom.clientHandler);
								} else {
									if (listRooms.size() >= WSSettings._MAX_ROOMS) {
										msgJoinRoom.clientHandler.enqueueMessage(new WSMError(WSMCode.ERR_TOO_MANY_ROOMS));
										break;
									}
									addRoom(msgJoinRoom.roomName, msgJoinRoom.clientHandler);
								}
								sendAllUsers(genListRoomsRespMsg());
								
								break;
								
							case OPCODE_LEAVE_ROOM:
								/*
								 * search for the room name
								 * - if not found --> ignore
								 * - if found --> try to remove the user out of the room
								 * 		* notify all users in the room about the leaving 
								 */
								WSMLeaveRoom msgLeaveRoom = (WSMLeaveRoom)msg;
								found = searchRoom(msgLeaveRoom.roomName);
								if (found == -1) {
									ErrandBoy.println("Client " + msgLeaveRoom.clientHandler.getName() + " wanna leave from un-existing room. Ignore the message.");
									break;
								}
								aRoom = listRooms.get(found);
								found = aRoom.searchUser(msgLeaveRoom.clientHandler.userName);
								if (found == -1) {
									ErrandBoy.println("Client " + msgLeaveRoom.clientHandler.getName() + " hasn't joined the room yet. Ignore the message LEAVE_ROOM.");
									break;
								}
								aRoom.removeUser(found);
								sendMsg2Users(aRoom.genListUsersRespMsg(), aRoom.listUsers);
								
								break;
								
							case OPCODE_SEND_ROOM_MSG:
								/*
								 * search for the room
								 * - if not found --> ignore
								 * - if found --> search for the user in the room --> found --> send the chat content to all users in the room via TELL_ROOM_MSG (ignore otherwise)
								 * - (optional) update the message to the room's message history
								 */
								WSMSendRoomMsg msgSendRoomMsg = (WSMSendRoomMsg) msg;
								found = searchRoom(msgSendRoomMsg.name);
								if (found == -1) {
									ErrandBoy.println("Client " + msgSendRoomMsg.clientHandler.getName() + " wanna send message to un-existing room. Ignore the message.");
									break;
								}
								aRoom = listRooms.get(found);
								found = aRoom.searchUser(msgSendRoomMsg.clientHandler.userName);
								if (found == -1) {
									ErrandBoy.println("Client " + msgSendRoomMsg.clientHandler.getName() + " wanna send message to an un-joined room. Ignore the message.");
									break;
								}
								WSMTellRoomMsg msgTellRoomMsg = new WSMTellRoomMsg(msgSendRoomMsg.clientHandler.userName, msgSendRoomMsg.name, msgSendRoomMsg.chatContent);
								sendMsg2Users(msgTellRoomMsg, aRoom.listUsers);
								aRoom.listMsgs.add(new ChatMessage(msgSendRoomMsg.chatContent, msgSendRoomMsg.clientHandler));
								
								break;
								
							case OPCODE_SEND_PRIVATE_MSG:
								/*
								 * search for the receiving user
								 * - if not found --> ignore
								 * - if found --> forward the message
								 */
								WSMSendPrivateMsg msgSendPrivateMsg = (WSMSendPrivateMsg) msg;
								found = searchUser(msgSendPrivateMsg.name, listClientSockets);
								if (found == -1) {
									ErrandBoy.println("Client " + msgSendPrivateMsg.clientHandler.getName() + " wanna send message to un-existing user. Ignore the message.");
									break;
								}
								listClientSockets.get(found).enqueueMessage(new WSMTellPrivateMsg(msgSendPrivateMsg.clientHandler.userName, msgSendPrivateMsg.chatContent));
								
								break;
								
								
							default:
								/*
								 * ignore all other message types
								 */
								ErrandBoy.println("Client " + msg.clientHandler.getName() + " send un-expected message " + msg.opcode + ". Ignore the message.");
								break;
							}
						} catch (Exception e) {
							ErrandBoy.printlnError(e, "Error while processing message: " + msg.opcode + " from " + msg.clientHandler.getName());
						}
					}
					
					ErrandBoy.println("Receiver-thread has stopped");
				} catch (Exception e) {
					ErrandBoy.printlnError(e, "Error when processing received messages");
				}
			}
		});
	}
	
	/***********************************************
	 * METHODS
	 ***********************************************/
	
	/**
	 * Start listening socket at local-host & wait for connection from users
	 */
	public void start() {
		try {
			//do nothing if the server is already running
			synchronized (isServerRunning) {
				if (isServerRunning) {
					ErrandBoy.println("Server is already running");
					return;
				}
			}
			
			//create the socket & start the listening thread
			listeningSocket = new ServerSocket(portNumber);
			connectionListener.start();
			
			//start message-receiver-thread
			msgReceiver.start();
		} catch (Exception e) {
			ErrandBoy.printlnError(e, "Error when starting server");
		}
	}
	
	/**
	 * Stop all activities running in the server
	 */
	public void stop() {
		
		//stop listening socket
		if (connectionListener.isAlive()) {
			try {
				listeningSocket.close();
			} catch (Exception e) {
				ErrandBoy.printlnError(e, "Error when interrupting listening thread");
			}
		}
		
		//stop all client handlers & empty the list
		for (WSClientHandler handler : listClientSockets) {
			handler.stop();
		}
		listClientSockets.clear();
		
		//stop the receiver-thread
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					receivingMsgQueue.put(new WSMStopSerer());
				} catch (Exception e) {
					ErrandBoy.printlnError(e, "Error when trying to enqueue WSMStopServer to stop receiver-thread");
				}
				
			}
		}).start();
		
	}

	/**
	 * Search for the user in the given client-list.
	 */
	public static int searchUser(String userName, ArrayList<WSClientHandler> listClientSockets) {
		for (int i = 0; i < listClientSockets.size(); i++) {
			if (listClientSockets.get(i).userName == null) {
				continue;
			}
			
			if (userName.equals(listClientSockets.get(i).userName)) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * Search for the given room.
	 */
	public int searchRoom(String roomName) {
		for (int i = 0; i < listRooms.size(); i++) {
			if (roomName.equals(listRooms.get(i).roomName)) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Return list of all room names.
	 */
	public String[] getRoomNames() {
		String[] arrNames = new String[listRooms.size()];
		for (int i = 0; i < listRooms.size(); i++) {
			arrNames[i] = listRooms.get(i).roomName;
		}
		
		return arrNames;
	}
	
	/**
	 * Create a new room, add in the user, and add the room to the room-list
	 */
	public void addRoom(String roomName, WSClientHandler theFirstUser) {
		ChatRoom newRoom = new ChatRoom(roomName);
		newRoom.addUser(theFirstUser);
		listRooms.add(newRoom);
	}
	
	/**
	 * Check if the user has logged-in or not, via the given received message.
	 */
	public boolean checkLoggedIn(WSMessage msg) {
		return msg.clientHandler.userName != null ? true : false;
	}
	
	/**
	 * Generate LIST_USERS_ALL message. <br>
	 * NOTE that only logged-in users are included
	 */
	public WSMListUsersAll genListUsersAllMsg() {
		ArrayList<String> listNames = new ArrayList<>();
		for (WSClientHandler client : listClientSockets) {
			if (client.userName != null) {
				listNames.add(client.userName);
			}
		}
		
		String[] arr = new String[listNames.size()];
		arr = listNames.toArray(arr);
		return new WSMListUsersAll(arr);
	}
	
	/**
	 * Generate LIST_ROOMS_RESP message
	 */
	public WSMListRoomsResp genListRoomsRespMsg() {
		return new WSMListRoomsResp(getRoomNames());
	}
	
	/**
	 * Send the given message to the given list of users
	 */
	public void sendMsg2Users(WSMessage msg, ArrayList<WSClientHandler> listClients) {
		for (WSClientHandler client : listClients) {
			if (client.userName == null) {
				continue;
			}
			
			client.enqueueMessage(msg);
		}
	}
	
	/**
	 * Send the given message to all users.
	 */
	public void sendAllUsers(WSMessage msg) {
		sendMsg2Users(msg, listClientSockets);
	}
	
	/**
	 * Remove user out of WatSup, including removing the user out of its joined rooms and return the room lists.
	 */
	public ArrayList<ChatRoom> removeUser(String userName) {
		ArrayList<ChatRoom> listJoinedRooms = new ArrayList<>();
		
		//user not found
		int found = searchUser(userName, listClientSockets);
		if (found == -1) {
			return listJoinedRooms;
		}
		
		//stop the user's client-handler
		listClientSockets.get(found).stop();
		
		//remove user out of client-list
		listClientSockets.remove(found);
		
		//go to all rooms to search for the user & remove
		for (int i = 0; i < listRooms.size(); i++) {
			ChatRoom room = listRooms.get(i);
			found = room.searchUser(userName);
			
			//remove the user if found in this room. Also add the room to the joined-room list
			if (found >= 0) {
				listJoinedRooms.add(room);
				room.removeUser(found);
			}
		}
		
		return listJoinedRooms;
	}
}
