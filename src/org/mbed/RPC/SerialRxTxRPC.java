package org.mbed.RPC;
import java.io.*;


import gnu.io.*;   //Not sure whether to use thinputStream or RXTXcomm.jar and what to then call
/**
 *  This class is used to create an object which can communicate using RPC to an mbed Connected over Serial using RXTX.
 *  This class requires RXTX to be installed on your computer:  http://www.rxtx.org/
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
public class SerialRxTxRPC extends mbed{
	protected CommPortIdentifier mbedPortID;
	protected CommPort mbedPort;
	protected DataInputStream inputStream;
	protected OutputStream to_mbed; 
	protected BufferedReader from_mbed;
	/**
	 * Create an mbed object to control an mbed connected over serial.
	 * <br>
	 * This class requires RxTx to be installed.
	 *  
	 * @param PortName The serial port mbed is connected to eg "COM5"
	 * @param Baud The baud rate for the serial communication
	 */
	public SerialRxTxRPC(String PortName, int Baud) {
		//open serial port
		try{
			mbedPortID = CommPortIdentifier.getPortIdentifier(PortName);
			
			mbedPort = mbedPortID.open("mbed", 100000);
			SerialPort mbedSerialPort = (SerialPort) mbedPort;
			mbedSerialPort.setSerialPortParams(Baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			inputStream = new DataInputStream(mbedSerialPort.getInputStream());
			to_mbed = mbedSerialPort.getOutputStream();
			from_mbed = new BufferedReader(new InputStreamReader(inputStream));

		}catch(NoSuchPortException e){
			System.err.println("No Such Port");
		}catch(PortInUseException e){
			System.err.println("Port Already in Use");
		}catch(UnsupportedCommOperationException e){
			
		}catch(IOException e){
			
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public String RPC(String Name, String Method, String[] Args){
		//write to serial port and receive result
		String Arguments = "";
		String Response;
		if(Args != null){
			int s = Args.length;
			for(int i = 0; i < s; i++){
				Arguments = Arguments + " " + Args[i];
			}
		}
		
		String RPCString = "/" + Name + "/" + Method + Arguments + "\n";
		byte[] RPCasBytes = RPCString.getBytes();
		try{
			to_mbed.write(RPCasBytes);
		}catch(Exception e){
			System.err.println("Error Writing to mbed");
			e.printStackTrace();
		}
		
		try{
			while(from_mbed.ready() == false){};
			Response = from_mbed.readLine();
		}catch(IOException e){
			//Error reading a line from the port
			System.err.println("Error reading line");
			Response = "error"; 
		}
		return(Response);
	}
	/**
	 * {@inheritDoc}
	 */
	public void delete(){
		//Close the serial port
		try{
			if (inputStream != null)inputStream.close();
			to_mbed.close();
		}catch(IOException e){
			System.out.println("IO Exception, could not close port");
		}
		mbedPort.close();
	}
}


