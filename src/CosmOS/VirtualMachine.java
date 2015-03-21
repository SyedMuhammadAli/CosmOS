package CosmOS;
/* This is the 'main' class that holds
 * all the pieces together.
 * The virtual machine class provides an
 * abstraction of a real world computer system.
 * It has a ram and a processor. It facilitates
 * the interaction between all these different 
 * components, and provides a layered approach
 * to the whole VM and the Operating System.
 */
import java.lang.*;

public class VirtualMachine{
	/*
	private static Memory kingston;
	private static Processor intel;
	private static Scheduler processScheduler;
	private static LongTermScheduler programLoader;
	private static MemoryManagementUnit memManager;
	
	
	public VirtualMachine(){
		kingston			= new Memory();
		intel				= new Processor(kingston);
		memManager			= new MemoryManagementUnit(intel);
		kingston.setMemoryUnit(memManager);
		processScheduler	= new Scheduler(intel, kingston);
		programLoader		= new LongTermScheduler(kingston, processScheduler);
	}
	
	public static void main(String[] args){
		new VirtualMachine();
		
		//kingston.printAvailableFrames();
		
		programLoader.load("p1.proc");
		programLoader.load("p1.proc");
		
		//System.out.print(processScheduler.getProcessList("-a"));
		
		//processScheduler.run();
		
		//intel.printState();
		//intel.programStack.printState();
	}
	*/
}
