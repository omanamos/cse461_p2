
public class RFIDWindow {
	
	private final int window;
	
	/**
	 * @param window should be less than 256
	 * @throws IllegalArgumentException if window is greater than 255
	 */
	public RFIDWindow(int window){
		if(window > 255)
			throw new IllegalArgumentException();
		this.window = window;
	}
	
	/**
	 * @return byte representation of this window
	 */
	public byte toByte(){
		return (byte)this.window;
	}
	
	/**
	 * @return the window size
	 */
	public int getWindow(){
	    return this.window;
	}
	
	/**
	 * Java creates a signed bit sequence when converting from an integer to a byte, 
	 * which only gives us a window range from 0 to 127 of positive numbers. We can 
	 * still use the negative half too though. When a number is negative, it means that
	 * it was greater than 127 (it wrapped around). Since we never use negative numbers
	 * as a window, we can infer that a negative number is between 127 and 256.
	 * 
	 * @param window byte to decode
	 * @return RFIDWindow of decoded byte
	 */
	public static RFIDWindow unpack(byte window){
		int x = window < 0 ? window + 256 : window;
		
		return new RFIDWindow(x);
	}
	
	//Test window logic
	public static void main(String[] args){
		for(int i = 0; i < 256; i++){
			RFIDWindow w = new RFIDWindow(i);
			w = RFIDWindow.unpack(w.toByte());
			if(w.window != i)
				System.err.println("Error: Expected: " + i + " Got: " + w.window);
		}
		System.out.println("Finished!");
	}
}
