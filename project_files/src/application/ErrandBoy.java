package application;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * This class provide utilities for WatSup system. Use this to:
 * + quickly test functions
 * + provide a public place to store data
 * + execute common functions
 * 
 * This class should not be instantiated. 
 * @author PineXmas
 *
 */
public abstract class ErrandBoy {
	
	public static void Test() {
		System.out.println("hello");
	}
	
	/**
	 * (SYNC) Print message to console
	 * @param msg message to print
	 */
	synchronized public static void print(String msg) {
		System.out.print(msg);
	}
	
	/**
	 * (SYNC) Print message to console & end with newline
	 * @param msg message to print
	 */
	synchronized public static void println(String msg) {
		System.out.println(msg);
	}
	
	/**
	 * (SYNC) Print error to console together with a headline
	 * @param e the exception where error happens
	 * @param errHeadline the headline
	 */
	synchronized public static void printlnError(Exception e, String errHeadline) {
		System.out.println(errHeadline);
		
		//get error stack trace into string & print to console
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteStream);
		e.printStackTrace(printStream);
		System.out.println(byteStream.toString());
	}
}
