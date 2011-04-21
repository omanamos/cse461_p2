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
    
    public List<byte[]> inventory() {
        int count = 0; //count of consecutive no-replies
        int window = 0;
        byte[] response;

        while(count < 32) {
            response = sendQuery();

            if(response == null){
            	count++;
            	boolean broadcast = window != 0;
            	window = Math.max(window / 2 - 1, 0);	//XXX Not sure what exactly to decrease window by
            	if(broadcast)
            		this.sendWindow(window);
            } else if(Arrays.equals(response, RFIDChannel.GARBLE)){
                count = 0;
                window = Math.min(window + 1, 255);		//XXX Not sure what exactly to increase window by
                // XXX if we send the window size as the payload of the next query packet, we could save ourselves
                //     a byte for the header. Then when a tag receives a query packet, it
                //     will /first/ update its window, and then decide whether to reply
                this.sendWindow(window); 
            } else {
                if(!currentInventory.contains(response)){
                    currentInventory.add(response);
                }
                sendAck(); // ACKS are implicit window decreases. By how much? Or, do we really need ACKs to be implicit window decreases?
                count = 0;
            }
        }

        return currentInventory;
    }
    
    private void sendAck(){
    	channel.sendMessage(ack);
    }
    
    private byte[] sendQuery(){
    	return channel.sendMessage(query);
    }
    
    private void sendWindow(int window){
    	channel.sendMessage(new byte[]{RFIDConstants.WINDOW, new RFIDWindow(window).toByte()});
    }
    
    //This controls the behavoir of the Reader.
    //The inventory method should run
    //until the reader determines that it is unlikly
    //any other tags are uninventored, then return
    //the currentInventory.
    public List<byte[]> strawManInventory() {
        /*
            Currently, the strawman protocol is implemented.
            Replace with your own protocol.
        */
        int count = 0; //count of consecutive no-replies

        byte[] response;

        //Strawman protocol continues until
        //32 consecutive no-replies.
        while(count < 32) {
            response = sendQuery();

            if(response == null){
                count++;
            } else if(Arrays.equals(response, RFIDChannel.GARBLE)){
                count = 0;
            } else {
                if(!currentInventory.contains(response)){
                    currentInventory.add(response);
                }
                sendAck();
                count = 0;

            }
        }

        return currentInventory;
    }
}

