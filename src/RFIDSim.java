//RFID Simulation, CSE461 Fall 2009, by Josh Goodwin
//Credit to RFIDSim.java, zahorjan, 2009/01/28

//RFIDSim.java Runs a simulation to test an algorithm to inventory
//RFID tags from a single RFID reader.
//Argument format: <#trials> <#tags per trial> <% channel error rate>
//Upon completion, statistics regarding the performance of the algorithm
//are displayed.


import java.util.*;

public class RFIDSim {

	public static void main(String[] args){
		int numTags = 0;
		int numTrials = 0;
		double errorRate = 0;
		try {
			numTrials = Integer.parseInt(args[0]);
			numTags = Integer.parseInt(args[1]);
			errorRate = Double.parseDouble(args[2]);
		} catch (Exception e) {
			System.out.println("Error: incorrect argument list.  Usage: RFIDSim <#trials> <#tags per trial> <channel error rate (%)>");
			System.exit(1);
		}

		if(numTrials < 1 || numTags < 0 || errorRate < 0){
			System.out.println("Error: invaid arguments.  There must be at least 1 trial with 0+ tags and error rate.");
			System.exit(1);
		}

		int maxTagsFound = 0, maxTagsMissed = 0, sumTagsFound = 0, sumTagsMissed = 0;
		long maxRuntime = 0, minRuntime = Long.MAX_VALUE, maxBytes = 0, minBytes = Long.MAX_VALUE, sumRuntime = 0, sumBytes = 0;
		boolean dupTags = false; //see if inventory incorrectly returns duplicate tag ID's

		for(int times = 0; times < numTrials; times++){
			RFIDTag[] tags = new RFIDTag[numTags];
			for(int i=0; i<numTags; i++){
				tags[i] = new RFIDTag();
			}

			RFIDChannel channel = new RFIDChannel(tags, errorRate);
			RFIDReader reader = new RFIDReader(channel);

			List<byte[]> tagsFound = null;
			long startTime = 0, endTime = 0;
			try {
				startTime = System.nanoTime();
				tagsFound = reader.inventory();
				endTime = System.nanoTime();
			} catch (Exception e) {
				System.out.println("Error: unexpected error during inventory call!");
				System.exit(1);
			}
			long elapsedTime = endTime - startTime; //in nano seconds

			//verify tags found, by ensuring that the EPC codes
			//returned by the reader match the EPC codes of the
			//initial tags (with no duplicates).

			Set<String> dupTest = new HashSet<String>();
			for (byte[] b : tagsFound)
                          dupTest.add(new String(b));
			if(dupTest.size() != tagsFound.size()){
				dupTags = true;
			}
			int numTagsFound = 0;
			for(byte[] thisEPC : tagsFound){
				for(int j=0; j<tags.length; j++){
					if(Arrays.equals(thisEPC, tags[j].getEPC())){
						numTagsFound++;
						break; //once we've matched, we're done with this EPC
					}
				}
			}

			//Statistics updates
			int tagsMissed = numTags - numTagsFound;
			sumTagsFound += numTagsFound;
			sumTagsMissed += tagsMissed;
			sumRuntime += elapsedTime;
			sumBytes += channel.getBytes();

			if(numTagsFound > maxTagsFound){
				maxTagsFound = numTagsFound;
			}
			if(tagsMissed > maxTagsMissed){
				maxTagsMissed = tagsMissed;
			}
			if(elapsedTime > maxRuntime){
				maxRuntime = elapsedTime;
			}
			if(elapsedTime < minRuntime){
				minRuntime = elapsedTime;
			}
			if(channel.getBytes() > maxBytes){
				maxBytes = channel.getBytes();
			}
			if(channel.getBytes() < minBytes){
				minBytes = channel.getBytes();
			}
		}


		//Display Results
		System.out.println();
		System.out.println("Results for " + numTrials + " trials, " + numTags +
								 " tags per trial, " + errorRate + "% channel error rate:");
		System.out.println("---------------------------------------------------------");
		if(dupTags){
			System.out.println();
			System.out.println("WARNING: duplicate tag EPC's returned by reader!");
			System.out.println();
		}
		System.out.println("Average tags found:  " + (double)sumTagsFound / numTrials);
		System.out.println("Average tags missed: " + (double)sumTagsMissed / numTrials);
		System.out.println("Average runtime: " + sumRuntime / (double)numTrials + " nanoseconds");
		System.out.println("Average bytes sent and received through channel: " + sumBytes / (double)numTrials);
		System.out.println("Max tags found:  " + maxTagsFound);
		System.out.println("Max tags missed: " + maxTagsMissed);
		System.out.println("Max runtime: " + maxRuntime + " nanoseconds");
		System.out.println("Min runtime: " + minRuntime + " nanoseconds");
		System.out.println("Max bytes single run: " + maxBytes);
		System.out.println("Min bytes single run: " + minBytes);
	}
}