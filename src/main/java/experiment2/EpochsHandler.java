package experiment2;

import java.util.ArrayList;
import java.util.List;

/** A class used in {@link Experiment2} to keep track of the epochs currently in use
 * */
public class EpochsHandler {
	
	
	/** The number of epochs kept in the class. It is used to deal with the value
	 * of epochs implemented in the time-sensitive strategy. For example, if this value is set to 2, 
	 * only the last 2 completed epochs are kept.
	 * */
	private int epochsNumber;
	
	/** A list of lists, the first list represents the epochs. The inner lists represent the single epoch.
	 * Each epoch is a list of array of Strings. Each array is a query.
	 * */
	private List<List<String[]>> epochs;
	
	private List<String[]> currentEpoch;
	
	public EpochsHandler(int eN) {
		// set the number of kept epochs
		this.epochsNumber = eN;
		this.epochs = new ArrayList<List<String[]>>();
		this.currentEpoch = new ArrayList<String[]>();
		this.epochs.add(currentEpoch);
	}
	
	public void startNewEpoch() {
		List<String[]> epoch = new ArrayList<String[]>();
		this.epochs.add(epoch);
		this.currentEpoch = epoch;
	}
	
	public void addQueryToCurrentEpoch(String[] query) {
		this.currentEpoch.add(query);
	}
	
	public void update() {
		// pop the head of the epochs list until it is of the desired size 
		while(this.epochs.size() > this.epochsNumber) {
			this.epochs.remove(0);
		}
	}

	public int getEpochsNumber() {
		return epochsNumber;
	}

	public void setEpochsNumber(int epochsNumber) {
		this.epochsNumber = epochsNumber;
	}

	public List<List<String[]>> getEpochs() {
		return epochs;
	}

	public void setEpochs(List<List<String[]>> epochs) {
		this.epochs = epochs;
	}

	public List<String[]> getCurrentEpoch() {
		return currentEpoch;
	}

	public void setCurrentEpoch(List<String[]> currentEpoch) {
		this.currentEpoch = currentEpoch;
	}

}
