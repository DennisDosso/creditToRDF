package it.unipd.dei.ims.data;

/** Class that contains data that I can return from methods
 * */
public class ReturnBox {
	
	/** time spent in an execution*/
	public long time;
	
	public long nanoTime;
	
	public int size;

	public int resultSetSize;
	
	public boolean foundSomething = false;

	public ReturnBox() {
		this.resultSetSize = 0;
	}
}
