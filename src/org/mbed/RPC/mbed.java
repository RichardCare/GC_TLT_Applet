package org.mbed.RPC;
/**
 *  This class is used to map a Java class on to the mbed API C++ class for AnalogIn.
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
public class mbed{

	public mbed() {
		// TODO Auto-generated constructor stub
		//Need to create all of the pin objects - globally or 
	}
	/**
     *
     * This method sends an RPC command and receives the response over the transport mechanisms that has been set up.
     * 
     *This method has been defined in the base class mbed but is overridden by the derived classes which actually implement the communication
     *
     * @param  Name	The Name of the object
     * @param  Method	The method of the object
     * @param  Args	An Array of the arguments which are to be passed 
     * @return Returns the response from the mbed RPC function.
     */
	public String RPC(String Name, String Method, String[] Args){
		//this should be over ridden by the derived class which inherits from this class
		String Arguments = "";
		int s = Args.length;
		for(int i = 0; i < s; i++){
			Arguments = Arguments + " " + Args[i];
		}
		//Print to console what would be written to mbed
		System.out.println("Using Demo Mode: Construct mbed object using one of the derived classes such as mbedTCP; Or your transport mechanism isn't overriding this method");
		System.out.println("Output: /" + Name + "/" + Method +  Arguments);
		System.err.println("RPC not sent");
		return("Response");
	}
	/**
	 * This closes the transport mechanism. 
	 * 
	 */
	public void delete(){
		System.out.println("Either no transport mechanism in use or delete is not overidden by the transport mechanim in use.");
		this.delete();
	}
}
