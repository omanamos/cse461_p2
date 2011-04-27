//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin

//RFIDConstants simply provides a single place to define
//various constants used in a frame, so both
//the reader and the tags are more easily updated.

public class RFIDConstants {

    public static final int MAX_WINDOW = 255;

    // ===== frame components =====
    public static final byte NEW_QUERY = 'n';       // the previous query was succesful (implicit ACK)
    public static final byte COLLISION_QUERY = 'c'; // there was a collision in the previous round (tell tags to increase window)
    public static final byte DESPERATE_QUERY = 'd'; // no tags responded last time, so everyone should decrease their window size
}
