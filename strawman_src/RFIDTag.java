//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
//Credit to RFIDTag.java, zahorjan, 2009/01/28

//RFIDTag.java represents an RFID tag, with a random
//64 bit EPC assigned during creation.  The tag only has
//the capability to respond to an incoming message, and can
//maintain internal state as necessary.

import java.util.*;
import java.io.*;

public class RFIDTag {
	private static Random generator = new Random();

	//64 bit tag identifier
	private byte[] tagEPC;

	//Insert other needed state here
	private boolean beenInventoried;
	private boolean sentEPC;

	public RFIDTag() {
		//generate random unsigned 64 bit identifier for this Tag
		tagEPC = new byte[8];
		generator.nextBytes(tagEPC);

		beenInventoried = false;
		sentEPC = false;
	}

	/*
		"magic" method used by the simulator to directly
	   access the EPC of the given tag.  Your reader can't do
	   magic, (or see the tags directly), so you should not
	   use this method.
	*/
	public byte[] getEPC() {
		return tagEPC;
	}


	/*
	  responds to an incoming message, encoded as a byte array.
     Return null if the tag does not reply to the message.
	*/
	public byte[] respond(byte[] message) {
		/*
			Currently, the strawman protocol is implemented.
			Replace with your own protocol.
		*/

		assert(message != null);

		if(Arrays.equals(message, RFIDChannel.GARBLE)){
			//do nothing (and don't check message further)
		} else {
			//unpack the message
			//see comment in RFIDReader concerning use of Streams
			ByteArrayInputStream bytesIn = new ByteArrayInputStream(message);
			DataInputStream dataIn = new DataInputStream(bytesIn);

			byte flag = 0;
			try {
				flag = dataIn.readByte();
			} catch (Exception e) {
				System.out.println("Error during read in Tag");
			}

			if(flag == RFIDConstants.ACK && sentEPC){
				//we've been inventoried, don't respond anymore.
				beenInventoried = true;
			} else if(flag == RFIDConstants.QUERY && !beenInventoried){
				//this is a query, roll the die to see if we reply
				if(generator.nextInt(32) == 0){
					//success, send out our EPC to be inventoried
					sentEPC = true;
					return tagEPC;
				}
			}
		}

		//if we reach here, we didn't respond with tag.  Return null (no reply).
		sentEPC = false;
		return null;
	}

}