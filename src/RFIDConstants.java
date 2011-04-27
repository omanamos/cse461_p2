//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin

//RFIDConstants simply provides a single place to define
//various constants used in a frame, so both
//the reader and the tags are more easily updated.

public class RFIDConstants {

    //Add or change frame components as needed
    public static final byte NEW_QUERY = 'n';       // the previous query was succesful (implicit ACK)
    public static final byte COLLISION_QUERY = 'c'; // there was a collision in the previous round (tell tags to decrease window)
    public static final byte DESPERATE_QUERY = 'd'; // no tags responded last time, so everyone should decrease their window size
}
