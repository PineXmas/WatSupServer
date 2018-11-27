package application;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class WSClientHandler {
	Socket clientSocket;
	Thread listener;
	final BlockingQueue<WSMessage> receivingMsgQueue;
	
	public WSClientHandler(Socket clientSocket, BlockingQueue<WSMessage> receivingMsgQueue) {
		this.clientSocket = clientSocket;
		this.receivingMsgQueue = receivingMsgQueue;
		
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
					byte[] arrIncompleteMsg = new byte[0];
					ArrayList<WSMessage> listMsgs;
					ArrayList<Byte> listNewIncompleteMsg = new ArrayList<>();
					while (true) {
						ErrandBoy.println("Waiting for input from client " + getClientName(clientSocket));
						readBytes = inputStream.read(buff);
						if (readBytes <= 0) {
							break;
						}
						
						//parse read bytes into messages
						byte[] actualReadBuff = ByteBuffer.allocate(readBytes).put(buff, 0, readBytes).array();
						listMsgs = WSMessage.parse2Msgs(actualReadBuff, arrIncompleteMsg, listNewIncompleteMsg);
						ErrandBoy.println("  Read " + listMsgs.size() + " complete msgs from client " + getClientName(clientSocket));
						
						//update incomplete message
						arrIncompleteMsg = ErrandBoy.convertList2Array(listNewIncompleteMsg);
						if (arrIncompleteMsg.length > 0) {
							ErrandBoy.println("  Incomplete msg from client " + getClientName(clientSocket) + ": " + new String(arrIncompleteMsg));
						}
						
						//enqueue messages
						for (WSMessage msg : listMsgs) {
							msg.clientHandler = clientSocket;
							receivingMsgQueue.put(msg);
						}
						
					}
					
					//reach here means the input stream has reach eof
					if (readBytes < 0) {
						ErrandBoy.println("Client " + getClientName(clientSocket) + " input is EOF, reading is stopped");
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
			ErrandBoy.printlnError(e, "Error when closing client socket " + getClientName(clientSocket));
		}
	}
	
	/**
	 * Return client's name in form of: address & port
	 * @param clientSocket
	 * @return
	 */
	public static String getClientName(Socket clientSocket) {
		if (clientSocket == null) {
			return "<null-socket>";
		}
		return clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort();
	}
}
