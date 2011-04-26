//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
//Credit to RFIDReader.java, zahorjan, 2009/01/28

//RFIDReader.java controls the reader of RFID tags.
//It sends out message on the channel, gets responses
//from the channel, and keeps a list of inventoried
//tag EPC id's.  When it believes that all tags have
//been inventoried, it returns the list of tag EPC id's.
//as a list of byte[]'s.

import java.util.*;
import java.io.*;

public class RFIDReader {

	private List<byte[]> currentInventory;
	private RFIDChannel channel;

	//frames used for the protocol
	byte[] ack;
	byte[] query;

	public RFIDReader(RFIDChannel chan) {
		currentInventory = new ArrayList<byte[]>();
		channel = chan;

		//Create needed output frames that don't change.
		//*Note: You may choose whether or not to use
		//Output/Input streams in your implementation.
		//They are offered here as one convenient option
		//for encoding/decoding a byte array.
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(bytesOut);

		try{
			//create "ack" frame
			dataOut.writeByte(RFIDConstants.ACK);
			dataOut.flush();
			ack = bytesOut.toByteArray();
			bytesOut.reset();

			//create "query" frame
			dataOut.writeByte(RFIDConstants.QUERY);
			dataOut.flush();
			query = bytesOut.toByteArray();
			bytesOut.reset();

			dataOut.close();
			bytesOut.close();
		} catch (Exception e){
			System.out.println("Error in creation of reader frames!");
		}
	}

	//This controls the behavoir of the Reader.
	//The inventory method should run
	//until the reader determines that it is unlikly
	//any other tags are uninventored, then return
	//the currentInventory.
	public List<byte[]> inventory() {
		/*
			Currently, the strawman protocol is implemented.
			Replace with your own protocol.
		*/
		int count = 0; //count of consecutive no-replies

		byte[] response;

		//Strawman protocol continues until
		//32 consecutive no-replies.
		while(count < 32) {
			response = channel.sendMessage(query);

			if(response == null){
				count++;
			} else if(Arrays.equals(response, RFIDChannel.GARBLE)){
				count = 0;
			} else {
				if(!currentInventory.contains(response)){
					currentInventory.add(response);
				}
				channel.sendMessage(ack);
				count = 0;

			}
		}

		return currentInventory;
	}
}

