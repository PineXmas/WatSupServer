package application;

import java.net.ServerSocket;
import java.net.Socket;

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
	
	public Object listRooms;
	public Object listUsers;
	public int portNumber;
	public int maxRooms;
	public int maxUsers;
	public Object sendingMsgPool;
	public Object receivingMsgPool;
	public Object msgSender;
	public Object msgReceiver;
	public Thread connectionListener;
	
	//socket
	public ServerSocket listeningSocket;
	public Boolean isServerRunning;
	Object lock_isServerRunning = new Object();
	
	/***********************************************
	 * CONSTRUCTORS
	 ***********************************************/
	
	public WSServer() {
		this(_DEFAULT_PORT, _MAX_ROOMS, _MAX_USERS);
	}
	
	public WSServer(int portNumber, int maxRooms, int maxUsers) {
		
		//settings
		this.portNumber = portNumber;
		this.maxRooms = maxRooms ;
		this.maxUsers = maxUsers ;
		isServerRunning = false;
		
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
						ErrandBoy.println("Connected to client at " + clientSocket.getInetAddress().toString() + ", port " + clientSocket.getPort());
						
						// TODO assign the socket to a client-dealer
						
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
		
		//... thread
	}
	
	/***********************************************
	 * METHODS
	 ***********************************************/
	
	//start listening socket at local-host & wait for connection from users
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
		} catch (Exception e) {
			ErrandBoy.printlnError(e, "Error when starting server");
		}
	}
	
	//stop all activities running in the server
	public void Stop() {
		
		//stop listening socket
		if (connectionListener.isAlive()) {
			try {
				listeningSocket.close();
			} catch (Exception e) {
				ErrandBoy.printlnError(e, "Error when interrupting listening thread");
			}
		}
	}
}
