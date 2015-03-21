package CosmOS;

import java.util.*;

import javax.swing.JTextArea;


/* This class holds and maintains all the
 * queues in the Operating System. It schedules
 * CPU time and allows execution of multiple 
 * processes concurrently.
 * 
 * The Scheduler executes the processes in the ready
 * queue by executing its instructions one by one
 * through the Processor Class.
 * 
 * Class Walk-through:
 * Firstly, the class fetches a PCB from the ready queue
 * and passes it to the processor using the setCurrentProcess
 * interface provided by the Processor Class. After that,
 * the restoreRegisterState function is called from the
 * processor class and the registers from the PCB are passes
 * to it so that the program could resume it's executed from
 * where it was interrupted.
 * Everything is pretty much in its place after that. The
 * program could be executed one instruction at a time by
 * calling the execNextInstruction procedure from the Processor
 * class.
 * 
 * See the documentation of the aforementioned procedures in
 * the Processor class to get a more firm overview.
 * */

public class Scheduler{
	private static final int MAX_KERNEL_FRAMES = 32;
	
	private int timeSlice = 8;
	
	private ArrayList<PCB> readyQlow;
	private ArrayList<PCB> readyQhigh;
	private ArrayList<PCB> blockedProcQ;
	
	public static Queue<Integer> freeKernelFrames = new LinkedList<Integer>();
	
	Processor cpu;
	Memory M;
	
	//For Debug Mode Support
	private JTextArea debugConsole;
	private boolean debugMode = false;
	
	/* Constructor */
	public Scheduler(Processor p, Memory ram){
		readyQlow = new ArrayList<PCB>();
		readyQhigh = new ArrayList<PCB>();
		blockedProcQ = new ArrayList<PCB>();
		
		//Prepare Free Kernel FrameList
		for(int i=0; i<MAX_KERNEL_FRAMES; i++)
			freeKernelFrames.add(i);
		
		cpu = p;
		M = ram;
	}
	
	/* pushIntoQueue
	 * Provides an interface to the external world to
	 * add PCBs to the internal queues of the Scheduler
	 * Class effectively hiding the Queue processing
	 * algorithm.
	 * */
	public void pushIntoQueue(PCB currPCB, byte priority){
		currPCB.setWaitingTime(cpu.clock()); //Set Internal Clock
		
		if(priority < 16)
			readyQhigh.add(currPCB);
		else
			readyQlow.add(currPCB);
	}
	
	/* setTimeSlice */
	public void setTimeSlice(int arg){
		timeSlice = arg;
	}
	
	/* Debug Opeations */
	public void setDebugConsole(JTextArea consoleRef){
		debugConsole = consoleRef;
	}
	
	public void enableDebugging(boolean flag){
		debugMode = flag;
	}
	
	/* getPCB */
	public PCB getPCB(int pid){
		for(int i=0; i<readyQhigh.size(); i++)
			if(readyQhigh.get(i).getPID() == pid) return readyQhigh.get(i);
		
		for(int i=0; i<readyQlow.size(); i++)
			if(readyQlow.get(i).getPID() == pid) return readyQlow.get(i);
		
		for(int i=0; i<blockedProcQ.size(); i++)
			if(blockedProcQ.get(i).getPID() == pid) return blockedProcQ.get(i);
		
		if(cpu.currentProcess != null && cpu.currentProcess.getPID() == pid) return cpu.currentProcess;
		
		//If all else fails
		return null;
	}
	
	/* blockProcess
	 * Sends the process with the given process ID to the blocked queue.
	 * Assumes that a valid PID has been passed in.
	 */
	public boolean blockProcess(int procID){
		PCB tmpPCB = getPCB(procID);

		if(blockedProcQ.contains(tmpPCB)){
			return false;
		} else {
			blockedProcQ.add(tmpPCB);
			
			if(readyQhigh.contains(tmpPCB)){
				readyQhigh.remove(tmpPCB);
			} else { //readyQlow has it
				readyQlow.remove(tmpPCB);
			}
			
			return true;
		}
	}
	
	/* unblockProcess
	 * Adds the process with the given process ID back to the ready queue.
	 * Assumes that a valid PID has been passed in.
	 */
	public boolean unblockProcess(int procID){
		PCB tmpPCB = getPCB(procID);
		
		if(blockedProcQ.contains(tmpPCB)){
			//Add to ready Q
			if(tmpPCB.getPriority() < 16)
				readyQhigh.add(tmpPCB);
			else
				readyQlow.add(tmpPCB);
			
			//Remove from blocked Q
			blockedProcQ.remove(tmpPCB);
			
			return true;
		} else { //do nothing
			return false;
		}
	}
	
	/* run
	 * The main entry point to the Scheduler Class.
	 * It start the Scheduler engine, and start the
	 * execution of programs inside the queues as
	 * defined by the algorithm.
	 * */
	public void run(){
		while(readyQhigh.size()+readyQlow.size() != 0){
			processHighPriorityQueue();
			processLowPriorityQueue();
		}
	}
	
	private void processHighPriorityQueue(){
		PCB currPCB;
		
		Collections.sort(readyQhigh);
		while(readyQhigh.size() != 0){
			currPCB = readyQhigh.get(0); readyQhigh.remove(0); //Pop PCB from Queue
			
			cpu.setDebugInfoString(" ***************** Switching Context.");
			cpu.switchProcessTo(currPCB);
			
			/* Execute all instruction unless F3 instruction is encountered */
			while(cpu.execNextIntruction()){
				cpu.setDebugInfoString("\n\nExecuting Instruction for " + currPCB.getPID() + " with priority " + currPCB.getPriority());
				
				//Send Debug Into String To Console if debugging is on
				if(debugMode == true)
					debugConsole.append(cpu.getDebugInfoString());
			}
			
			killProcess(currPCB.getPID());
			//cleanUp(currPCB);
		}
	}
	
	private void processLowPriorityQueue(){
		PCB currPCB;
		boolean done = false;
		
		while(readyQlow.size() != 0){
			
			/* Stop execution of lower priority Queue if
			 * processes are available in the higher
			 * priority queue
			 * */
			if(readyQhigh.size() > 0)
				return;
			
			currPCB = readyQlow.get(0); readyQlow.remove(0); //Pop PCB from Queue
			
			cpu.setDebugInfoString(" ***************** Switching Context.");
			cpu.switchProcessTo(currPCB);
			
			for(int numCycles = 0; numCycles < timeSlice; numCycles+=2){ //RoundRobin Algorithm
				cpu.setDebugInfoString("\n#! Executing Instruction for PID " + currPCB.getPID() + " with priority " + currPCB.getPriority());
				done = cpu.execNextIntruction();
				
				//Send Debug Into String To Console if debugging is on
				if(debugMode == true)
					debugConsole.append(cpu.getDebugInfoString());
				
				if(done){ //if process is done
					/* Q. Why does cleanUp logic leaves the last process untouched in one of the queues? */
					
					killProcess(currPCB.getPID());
					//cleanUp(currPCB);
					break; //no need to give more time to current process
				}
			}
			
			/* Only add PCB to the queue again if the process is not done. No further
			 * action is required otherwise since the JVM garbage collector will automatically
			 * recycle the unreferenced object.
			 * */
			if(!done)
				readyQlow.add(currPCB);
			else
				done = false;
		}
	}
	
	/* cleanUp
	 * Provides house-keeping services. Frees the memory
	 * allocated to the process in the user and kernel space.
	 * */
	private void cleanUp(PCB currPCB){
		//Removing Process From Memory after Completion
		for(int i=0; i<currPCB.getPageTableSize(); i++){
			M.freeFrame(currPCB.getPageTableEntry(i));
		}
		
		//Free Space Taken Up by the PCB
		freeKernelFrames.add(currPCB.getBaseFrame());
		
		//Temp - Waiting Time Calculation
		/* Total Waiting Time = CPU.clock() - PCB.getWaitingTime - PCB.getExecutionTime => write to file */
	}
	
	/* killProcess
	 * Removes the process with the given process ID from the queues and
	 * deallocates any memory allocated to that process.
	 * */
	public boolean killProcess(int procID){
		PCB victimPCB = getPCB(procID);
		
		if(cpu.currentProcess != null && cpu.currentProcess.getPID() == victimPCB.getPID()){
			cpu.currentProcess = null; //remove from cpu
			
			/* else cause removed and added to if clause to provide added support to exec one process commands */
			if(readyQlow.contains(victimPCB))
				readyQlow.remove(victimPCB);
			else if(readyQhigh.contains(victimPCB))
				readyQhigh.remove(victimPCB);
			else if(blockedProcQ.contains(victimPCB))
				blockedProcQ.remove(victimPCB);
		}
		
		cleanUp(victimPCB);
		
		return true;
	}
	
	/* getProcessList
	 * Returns a Linked List of all the processes that
	 * are currently in the scheduler.
	 */
	public LinkedList getProcessList(){
		LinkedList<PCB> procList = new LinkedList<PCB>();
		
		if(cpu.currentProcess != null)
			procList.add(cpu.currentProcess);
		
		for(int i=0; i<readyQhigh.size(); i++)
			procList.add(readyQhigh.get(i));
		
		for(int i=0; i<readyQlow.size(); i++)
			procList.add(readyQlow.get(i));
		
		for(int i=0; i<blockedProcQ.size(); i++)
			procList.add(blockedProcQ.get(i));
		
		return procList;
	}
	
	/* getProcessListStr
	 * Returns a list containing the names of process in
	 * different lists based on the argument passed. 
	 */
	public String getProcessListStr(String arg){
		String	resultList = new String(),
				readyQHighStr = new String(),
				readyQLowStr = new String(),
				blockedQStr = new String(),
				runningQStr = new String();
		
		//Preparing List
		for(int i=0; i<readyQhigh.size(); i++){ //readyQHigh
			readyQHighStr += readyQhigh.get(i).getPcbInfoString();
		}
		
		for(int i=0; i<readyQlow.size(); i++){ //readyQLow
			readyQLowStr += readyQlow.get(i).getPcbInfoString();
		}
		
		for(int i=0; i<blockedProcQ.size(); i++){ //blockedProcQ
			blockedQStr += blockedProcQ.get(i).getPcbInfoString();
		}
		
		if(cpu.currentProcess != null)
			runningQStr = cpu.currentProcess.getPcbInfoString();
		
		//Preparing Result String
		resultList = "ID\tPriority\tSize\n";
		
		if(arg == "-a"){ //all
			resultList += readyQHighStr + readyQLowStr + blockedQStr;
		} else if(arg == "-b"){ //blocked
			resultList += blockedQStr;
		} else if(arg == "-r"){ //ready
			resultList += readyQHighStr + readyQLowStr;
		} else if(arg == "-e"){ //running
			resultList += runningQStr;
		}
		
		return resultList;
	}
}
