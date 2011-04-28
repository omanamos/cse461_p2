//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
//Credit to RFIDTag.java, zahorjan, 2009/01/28

//RFIDTag.java represents an RFID tag, with a random
//64 bit EPC assigned during creation.  The tag only has
//the capability to respond to an incoming message, and can
//maintain internal state as necessary.

import java.util.*;
import java.io.*;

public class RFIDTag {
    private static final int INITIAL_WINDOW = 32;
    private static Random generator = new Random();

    //64 bit tag identifier
    private byte[] tagEPC;

    //Insert other needed state here
    private boolean beenInventoried;
    private boolean previouslySentEPC;

    private int window;

    public RFIDTag() {
        //generate random unsigned 64 bit identifier for this Tag
        tagEPC = new byte[8];
        generator.nextBytes(tagEPC);

        beenInventoried = false;
        previouslySentEPC = false;
        window = INITIAL_WINDOW;
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
      return null if the tag does not reply to the message.
    */
    public byte[] respond(byte[] message) {
        if(message == null)
            throw new AssertionError("Message should not be null!");

        if(!Arrays.equals(message, RFIDChannel.GARBLE)) {
            //unpack the message
            //see comment in RFIDReader concerning use of Streams
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(message);
            DataInputStream dataIn = new DataInputStream(bytesIn);

            byte flag = 0;
            try {
                flag = dataIn.readByte();
            } catch (Exception e) {
                System.err.println("Error during read in Tag");
                return null;
            }

            switch(flag) {
            case RFIDConstants.NEW_QUERY:
                // someone was inventoried, so we know that we have one less
                // tag to contend with
                window = Math.max(window - 1, 0);
                
                if(previouslySentEPC) {
                    //we've been inventoried, so don't respond anymore.
                    beenInventoried = true;
                }

                break;
            case RFIDConstants.COLLISION_QUERY:
                // There was a collision last round, so increase our window
                // size
                window = Math.min(window * 2 + 1, RFIDConstants.MAX_WINDOW);
                break;
            case RFIDConstants.DESPERATE_QUERY: 
                // Nobody responded last period, so we halve our window size
                window = Math.max(window / 2 - 1, 0);
                break;
            }

            // if the Reader explicitly put in a window byte, we override the 
            // decision we made earlier
            // XXX Why isn't there a DataInputStream.hasNextByte()?
            try {
                window = RFIDWindow.unpack(dataIn.readByte()).getWindow();
            } catch(Exception e) {}
            

            //and roll the die to see if we reply
            if(!beenInventoried && (window == 0 || generator.nextInt(window) == 0)){
               //success, send out our EPC to be inventoried
               previouslySentEPC = true;
               return tagEPC;
            }
        }

        //if we reach here, we didn't respond with tag.  Return null (no reply).
        previouslySentEPC = false;
        return null;
    }
}
