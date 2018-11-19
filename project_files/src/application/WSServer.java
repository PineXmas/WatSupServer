package application;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class WSServer {
	
	/***********************************************
	 * DEFAULT SETTINGS
	 ***********************************************/
	
	static public final int _DEFAULT_PORT = 8312;
	static public final int _MAX_ROOMS = 5;
	static public final int _MAX_USERS = 10;
	
	/***********************************************
	 * MEMBERS
	 ***********************************************/
	
	//chat data
	Object listRooms;
	Object listUsers;
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
	
	//client sockets
	ArrayList<WSClientHandler> listClientSockets = new ArrayList<>();
	
	/***********************************************
	 * CONSTRUCTORS
	 ***********************************************/
	
	public WSServer() {
		this(_DEFAULT_PORT, _MAX_ROOMS, _MAX_USERS);
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
						ErrandBoy.println("Connected to client at " + clientSocket.getInetAddress().getHostName() + ", port " + clientSocket.getPort());
						
						//assign client to a dealer for further handling
						WSClientHandler dealer = new WSClientHandler(clientSocket, receivingMsgQueue);
						listClientSockets.add(dealer);
						dealer.listen();
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
					while ( !((msg = receivingMsgQueue.take()) instanceof WSMStopSerer)) {
						//TODO process messages here. Print to console for now
						ErrandBoy.println("Client " + WSClientHandler.getClientName(msg.sender) + " sent: " + msg.toString());
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
	public void Start() {
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
	public void Stop() {
		
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
			handler.close();
		}
		listClientSockets.clear();
		
		//stop the receiver-thread
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					receivingMsgQueue.put(new WSMStopSerer(-1, -1, null));
				} catch (Exception e) {
					ErrandBoy.printlnError(e, "Error when trying to enqueue WSMStopServer to stop receiver-thread");
				}
				
			}
		}).start();
		
	}
}
