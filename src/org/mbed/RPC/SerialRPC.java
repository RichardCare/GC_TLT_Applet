package org.mbed.RPC;
import java.io.*;
import java.util.*;
import javax.comm.*;

/**
 *  This class is used to create an object which can communicate using RPC to an mbed Connected over Serial.
 *  This class requires the Java Communications API be installed on your computer.
 * 
 * @author Michael Walker
 * @license
 * Copyright (c) 2010 ARM Ltd
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software and associated documentation files (the "Software"), to deal
 *in the Software without restriction, including without limitation the rights
 *to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *copies of the Software, and to permit persons to whom the Software is
 *furnished to do so, subject to the following conditions:
 * <br>
 *The above copyright notice and this permission notice shall be included in
 *all copies or substantial portions of the Software.
 * <br>
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *THE SOFTWARE.
 *
 */
public class SerialRPC extends mbed implements  SerialPortEventListener{
	protected CommPortIdentifier mbedPortID;
	protected SerialPort mbedSerialPort;
	protected CommPort mbedPort;
	protected DataInputStream inputStream;
	protected PrintStream outputStream;
	protected BufferedReader reader;
	protected PrintWriter to_mbed = null;
	
	/**
	 * This creates an mbed object for an mbed connected over Serial.
	 * <br>
	 * Using this class requires the Sun Communications API to be installed
	 * 
	 * @param PortName The Serial Port mbed is connected to eg "COM5" on Windows.
	 * @param Baud The baud rate
	 */
	public SerialRPC(String PortName, int Baud) {
		//open serial port
		try{
			mbedPortID = CommPortIdentifier.getPortIdentifier(PortName);
			
			mbedPort = mbedPortID.open("mbed", 100000);
			mbedSerialPort = (SerialPort) mbedPort;
			mbedSerialPort.setSerialPortParams(Baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	
		
			inputStream = new DataInputStream(mbedPort.getInputStream());
			outputStream = new PrintStream(mbedPort.getOutputStream(), true);

			reader= new BufferedReader(new InputStreamReader(inputStream));
			to_mbed = new PrintWriter(outputStream, true);

			mbedSerialPort.addEventListener(this);
			//Important to set this regardless of whether interrupts are in use to keep the buffer clear
			mbedSerialPort.notifyOnDataAvailable(true);
			
		}catch(TooManyListenersException e) {
			//Error adding event listener
			System.out.println("Too many Event Listeners");
		}catch(NoSuchPortException e){
			System.out.println("No Such Port");
		}catch(PortInUseException e){
			System.out.println("Port Already In Use");
		}catch(UnsupportedCommOperationException e){
			System.out.println("Unsupported Comm Operation");
		}catch(IOException e){
			System.out.println("Serial Port IOException");
		}
	}
	
	public void serialEvent(SerialPortEvent event) {
		System.out.println("Serial Port event");
		switch (event.getEventType()) {
		
			case SerialPortEvent.DATA_AVAILABLE:
				@SuppressWarnings("unused")
				String Interrupt = "";
	
			    try {
			    	while(reader.ready() == false){};
					Interrupt = reader.readLine();
			    } catch (IOException e) {}
				break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String RPC(String Name, String Method, String[] Args){
		//write to serial port and receive result
		String Response;
		String Arguments = "";
		if(Args != null){
			int s = Args.length;
			for(int i = 0; i < s; i++){
				Arguments = Arguments + " " + Args[i];
			}
		}
		try{
		to_mbed.println("/" + Name + "/" + Method + Arguments);
		mbedSerialPort.notifyOnDataAvailable(false);
		}catch(NullPointerException e){
			
		}
		boolean valid = true;
		try{
			while(reader.ready() == false){};
			do{
			Response = reader.readLine();
			
			if(Response.length() >= 1){
				valid = Response.charAt(0) != '!';
			}
			}while(valid == false);
		}catch(IOException e){
			System.err.println("IOException, error reading from port");
			Response = "error"; 
		}

		mbedSerialPort.notifyOnDataAvailable(true);  
		return(Response);
	}
	/**
	 * Close the serial port.
	 */
	public void delete(){
		//Close the serial port
		try{
			if (inputStream != null)
				inputStream.close();
			outputStream.close();
		}catch(IOException e){
		
		}
		mbedSerialPort.removeEventListener();
		mbedSerialPort.close();
		mbedPort.close();
	}
	
}
