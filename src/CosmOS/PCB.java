package CosmOS;
import java.util.*;

public class PCB implements Comparable<PCB>{
	public static final int MAX_REGISTERS = 32,
							SB=0x14, SL=0x15, SC=0x16,
							//Following are the offset constants of the PCB in memory
							PID_ = 64,
							PRIORITY_ = 65,
							PROC_SIZE_ = 66,
							WAITING_TIME_ = 68,
							EXECUTION_TIME_ = 70,
							PAGETABLE_SIZE_ = 73,
							PAGETABLE_ = 74,
							MAX_ENTRIES = 27;
	
	public static Memory M;
	
	private byte baseFrame;
	
	/* Constructor */
	public PCB(int frameNum){
		this.baseFrame = (byte)frameNum;
	}
	
	/* setMemoryLink
	 * Establishes a memory link so that PCB could be written to the
	 * Memory directly.
	 */
	public static void setMemoryLink(Memory mem){
		M = mem;
	}
	
	/* Address Generation Helper Function */
	private short addrOf(int offset){ return (short) ((baseFrame*128)+offset); }
	
	/* PageTable Interface */
	private boolean addPageTableEntry(short frameNum){
		//Add Entry
		//System.out.printf("Writing %d to Page Table Entry at Addr: %d\n", frameNum, addrOf(PAGETABLE_+(getPageTableSize()*2)));
		M.writeToPhysical(addrOf(PAGETABLE_+(getPageTableSize()*2)), frameNum);
		
		//Increment Size
		M.writeByteToPhysical(addrOf(PAGETABLE_SIZE_), (byte)(getPageTableSize()+1));
		return true;
	}
	
	/* attachPageTable
	 * Takes a Vector<Integer> as argument and copies the
	 * page table into the internal memory of the system
	 * where the PCB has been mapped.
	 */
	public boolean attachPageTable(Vector<Integer> pTable){
		int currElt = 0;
		
		if(pTable.size() > MAX_ENTRIES){
			System.out.println("Can't load program into memory. Limit for page table entries is 27.");
			//Generate Trap To OS
			return false;
		} else {
			for(int i=0; i<pTable.size(); i++){
				currElt = pTable.elementAt(i);
				addPageTableEntry((short) currElt);
			}
			
			return true;
		}
	}
	
	/* getPageTableEntry
	 * Fetches the pageTableEntry at location 'index' from
	 * the internal memory where the PCB has been mapped.
	 */
	public Short getPageTableEntry(int index){
		if(index >= getPageTableSize()){ //max index is size-1
			System.out.println("Page Table Array Out of Bound Access");
			//Generate Trap To OS
			return 0;
		}
		
		//System.out.printf("Reading %d from PageTableEntry @ index %d\n", M.readShortFromPhysical(addrOf( PAGETABLE_+(index*2))), addrOf( PAGETABLE_+(index*2)));
		return M.readShortFromPhysical(addrOf( PAGETABLE_+(index*2)));
	}
	
	/* Interface Function for Data Members */
	public Byte getPID(){
		return M.readByteFromPhysical( addrOf(PID_) );
	}
	
	public void setPID(byte pid){
		M.writeByteToPhysical(addrOf(PID_), pid);
	}
	
	public Byte getPriority(){
		return M.readByteFromPhysical( addrOf(PRIORITY_) );
	}
	
	public void setPriority(byte priority){
		M.writeByteToPhysical(addrOf(PRIORITY_), priority);
	}
	
	public short getProcessSize(){
		return M.readShortFromPhysical( addrOf(PROC_SIZE_) );
	}
	
	public void setProcessSize(short procSize){
		M.writeToPhysical(addrOf(PROC_SIZE_), procSize);
	}
	
	public short getWaitingTime(){
		return M.readShortFromPhysical( addrOf(WAITING_TIME_) );
	}
	
	public void setWaitingTime(short waitTime){
		M.writeToPhysical(addrOf(WAITING_TIME_), waitTime);
	}
	
	public short getPageTableSize(){
		return M.readByteFromPhysical( addrOf(PAGETABLE_SIZE_) );
	}
	
	public void setPageTableSize(short pTableSize){
		M.writeToPhysical(addrOf(PAGETABLE_SIZE_), pTableSize);
	}
	
	public short getExecutionTime(){
		return M.readShortFromPhysical( addrOf(EXECUTION_TIME_) );
	}
	
	public void setExecutionTime(short execTime){
		M.writeToPhysical(addrOf(EXECUTION_TIME_), execTime);
	}
	
	public void incrementExecutionTime(){
		M.writeToPhysical(addrOf(EXECUTION_TIME_), (short)(getExecutionTime()+2));
	}
	
	public Integer getBaseFrame(){
		return (int)baseFrame;
	}
	
	
	/* updateRegisterState
	 * Provides an interface to the Processor class to
	 * save the state of the process inside PCB in the 
	 * event of a context switch.
	 * 
	 * Reference: Processor.switchProcessTo()
	 * */
	public void updateResigsterState(Register Reg[]){
		for(int i=0; i<MAX_REGISTERS; i++){ //Copy all registers
			this.setRegister(i, Reg[i].value());
		}
	}
	
	/* setRegister
	 * Provides an interface to set the value of a specific
	 * Register. It enables the LongTermScheduler class to
	 *  the initial values of the Code, Base and Stack Registers.
	 * */
	public void setRegister(int index, short value){
		if(index >= 0 && index <= 31){
			M.writeToPhysical( addrOf(index*2), value);
		} else {
			System.out.println("Invalid register code passes to setRegister in PCB class.");
			//Generate Trap To OS
			return;
		}
	}
	
	/* getRegisterState
	 * Returns an array of Registers.
	 * Helps the Processor class to restore the states of its
	 * internal registers in the event of a context switch.
	 * 
	 * Reference: Processor.restoreRegistersState()
	 * 
	 * Developer Tip:
	 * This function was modified to incorporate new PCB Architecture
	 * into the current framework of OS.
	 * */
	public Register[] getRegisterState(){
		Register R[] = new Register[32];
		
		for(int i=0; i<32; i++)
		{
			R[i] = new Register();
			R[i].setValue( M.readShortFromPhysical(addrOf(i*2)) );
		}
		
		return R;
	}
	
	/* getPcbInfoString:
	 * Returns a string containing information about the current PCB.
	 * */
	public String getPcbInfoString(){
		return new String(getPID() + "\t" + getPriority() + "\t" + getProcessSize() + "\n");
	}
	
	/* getPcbAsString
	 * Returns almost all the contents of the Pcb As a String
	 * */
	public String getPcbAsString(){
		String pcbInfo = new String();
		
		pcbInfo += String.format("PID\t: %d\n", getPID());
		pcbInfo += String.format("Priority\t: %d\n", getPriority());
		pcbInfo += String.format("Process Size\t: %d\n", getProcessSize());
		pcbInfo += String.format("Waiting Time\t: %d\n", getWaitingTime());
		pcbInfo += String.format("Execution Time\t: %d\n", getExecutionTime());
		pcbInfo += String.format("PageTable Size\t: %d\n", getPageTableSize());
		
		return pcbInfo;
	}
	
	/* compareTo
	 * Allows the Collections.sort to sort entries in
	 * Scheduler.processHighPriorityQueue functions.
	 * Provides a mechanism to compare objects of type PCB.
	 * */
	@Override
	public int compareTo(PCB op) {
		return this.getPriority().compareTo(op.getPriority());
	}
	/*
	@Override
	public boolean equals(Object ob) {
		return this.getPID() == ((PCB) ob).getPID();
	}
	*/
}

