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

    private RFIDWindow window;

    public RFIDTag() {
        //generate random unsigned 64 bit identifier for this Tag
        tagEPC = new byte[8];
        generator.nextBytes(tagEPC);

        beenInventoried = false;
        previouslySentEPC = false;
        window = new RFIDWindow(INITIAL_WINDOW);
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

        if(!Arrays.equals(message, RFIDChannel.GARBLE)){
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

            if(flag == RFIDConstants.ACK){
                // someone was inventoried, so we know that we have one less
                // tag to contend with
                window = new RFIDWindow(Math.max(window.getWindow() - 1, 0));
                
                if(previouslySentEPC) {
                    //we've been inventoried, so don't respond anymore.
                    beenInventoried = true;
                }
            } else if(flag == RFIDConstants.QUERY && !beenInventoried){
               //this is a query, update our window..
                try {
                    window = RFIDWindow.unpack(dataIn.readByte());
                } catch (Exception e) {
                    //System.err.println("Error during read in Tag");
                    //return null;
					//XXX If the window hasn't changes, no need to send it again, so I just send the query flag, so just use the old window.
                }
                
                // and roll the die to see if we reply
                if(window.getWindow() == 0 || generator.nextInt(window.getWindow()) == 0){
                    //success, send out our EPC to be inventoried
                    previouslySentEPC = true;
                    return tagEPC;
                }
            }
        }

        //if we reach here, we didn't respond with tag.  Return null (no reply).
        previouslySentEPC = false;
        return null;
    }
}
