package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class WSClientHandler {
	Socket clientSocket;
	Thread listener;
	
	public WSClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
		
		//set up listener thread
		listener = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					//set up input stream reader
					InputStream inputStream = clientSocket.getInputStream();
					byte[] buff = new byte[1024];
					
					//keep reading from client
					int readBytes;
					while (true) {
						ErrandBoy.println("Waiting for input from client " + getName());
						readBytes = inputStream.read(buff);
						if (readBytes <= 0) {
							break;
						}
						
						String content = new String(buff, 0, readBytes);
						ErrandBoy.println("Client " + getName() + " sent (" + readBytes + "): " + content);
					}
					
					//reach here means the input stream has reach eof
					if (readBytes < 0) {
						ErrandBoy.println("Client " + getName() + " input is EOF, reading is stopped");
					}
				} catch (Exception e) {
					ErrandBoy.printlnError(e, "Error while reading from client's input stream");
				} finally {
					// TODO finalize client socket after error occurs
				}
				
			}
		});
	}
	
	//keep reading input from client's socket
	public void listen() {
		if (clientSocket == null) {
			ErrandBoy.println("Client socket is null, cannot listen from");
			return;
		}
		
		listener.start();
	}
	
	//stop reading input & close socket
	public void close() {
		
		try {
			clientSocket.close();
		} catch (Exception e) {
			ErrandBoy.printlnError(e, "Error when closing client socket " + getName());
		}
	}
	
	//return client's name in form of: address & port
	public String getName() {
		return clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort();
	}
}
