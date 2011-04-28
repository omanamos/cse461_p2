//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
//Credit to RFIDChannel.java, zahorjan, 2009/01/28

//RFIDChannel.java controls the channel linking the
//RFIDReader and RFIDTags.  It takes a message from the
//reader, transmits that message to every tag, then
//collects responses and responds back to the reader.
//There is a given percent chance that the message
//in either direction will be garbled.  Messages in both
//directions are byte[]'s.

import java.util.*; 

public class RFIDChannel {

	public static final byte[] GARBLE = new byte[0];
	private RFIDTag[] tags;
	private long numBytes; //total bytes sent and received through channel
	private Random generator;
	private double errorRate;
	
	public RFIDChannel(RFIDTag[] tagArray, double errorRate){
		tags = tagArray;
		numBytes = 0;
		generator = new Random();
		this.errorRate = errorRate / 100;  //convert to decimal
	}
	
	
	//This method takes a message from the reader,
	//encoded as an array of bytes, and transmits the
	//message to every tag, with a small chance
	//of corruption for each tag.  The responses (if any)
	//from the tags are then collected.  If multiple
	//responses are received, a collision occurs
	//and the returned message to the reader is garbled.
	//if only one tag replies, that message is returned,
	//with a small chance that it became garbled in transit.
	public byte[] sendMessage(byte[] message){
		numBytes += message.length;
				 
		List<byte[]> responses = new ArrayList<byte[]>();
		for(RFIDTag t : tags){
			byte[] response;
			if(generator.nextDouble() < errorRate){
				response = t.respond(GARBLE);
			}else{
				response = t.respond(message);
			}
			
			if(response != null){
				responses.add(response);
			}
		}
		
		
		if(responses.size() == 1){
			numBytes += responses.get(0).length;
			if(generator.nextDouble() < errorRate){
				return GARBLE;
			} else {
				return responses.get(0);
			}
		} else if(responses.size() > 1){
			numBytes += responses.get(0).length;
			return GARBLE;
		} else
			return null;
	}
	
	public long getBytes(){
		return numBytes;
	}


}