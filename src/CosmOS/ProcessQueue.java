package CosmOS;

import java.util.Vector;

public class ProcessQueue {
	private final int MAX_SIZE = 64;
	private int base;
	private Vector<Integer> freeFrameList;
	private static Memory M;
	
	public ProcessQueue(int b){
		base = b;
		freeFrameList = new Vector<Integer>();
		
		for(int i=1; i<MAX_SIZE; i++) //initialize free frame list for queue
			freeFrameList.add(base+i);
	}
	
	public static void setMemoryLink(Memory mem){ M = mem; }
	
	public void add(PCB p){
		if(freeFrameList.size() == 0){
			//Generate Trap To OS - Can't Load Process
			return;
		}
		
		int frameAddr = freeFrameList.elementAt(0) * 128;
		freeFrameList.remove(0);
	}
}
