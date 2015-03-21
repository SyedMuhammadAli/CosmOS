package CosmOS;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

/* This class takes the filename of the process to be
 * loaded from the backing store and creates it's PCB.
 * Firstly, the program is read into an intermediate
 * buffer. If there are enough memory frames available,
 * the program is written into the available frames and
 * the PCB is updates to reflect the changes. After the
 * program is loaded into the memory and it's PCB has been
 * created, PCB is added to the ready queue. 
 */

public class LongTermScheduler {
	public static final int PAGE_SIZE = 128;
	public static final int STACK_SIZE = 50;
	
	private static final int	CB=0x11, CL=0x12, CC=0x13,
								SB=0x14, SL=0x15, SC=0x16,
								DB=0x17, DL=0x18;
	
	private static final int	DS_INSTR_ADDR = 0x0000, //0 - use '0c4000' for 1
								CS_INSTR_ADDR = 0x4000, //1 - use '0c8000' for 2
								SS_INSTR_ADDR = 0x8000; //2 - use '0xC000' for 3
	
	private Scheduler processScheduler;
	private Vector<Byte> buffer;
	private Memory M;
	
	public LongTermScheduler(Memory mem, Scheduler procSch){
		M = mem;
		buffer = new Vector<Byte>();
		processScheduler = procSch;
		PCB.setMemoryLink(mem); //call to statc fn;
	}
	
	public void printFileRead(){
		for(int i=0; i<buffer.size(); i++){
			System.out.printf("%h ", buffer.elementAt(i));
		}
	}
	
	public boolean load(String procFileName){
		String fileName = procFileName;
		int b = 0;
		
		buffer.clear(); //destroy old data in buffer
		
		FileInputStream fin = null;
		
		try {
			fin = new FileInputStream(fileName);
		} catch(FileNotFoundException fnfe) {
			System.out.println(fileName + ": File Not Found.");
			return false;
		}
		
		try{
			while( (b = fin.read()) != -1){
				buffer.add((byte)b);
			}
		} catch(IOException ioe) {
			System.out.println("IOException caught @ Dispatcher load function.");
			return false;
		}
		
		this.writeProgramToMemory(procFileName);
		return true;
	}
	
	private PCB createPCB(){
		PCB p;
		
		if(processScheduler.freeKernelFrames.size() == 0){
			System.out.println("The System is already at it's peak degree of multi programming. Can't create any more PCBs.");
			//Generate Trap To OS
			return new PCB(-1);
		} else {
			int tmp_pid = buffer.elementAt(1);
			p = new PCB(processScheduler.freeKernelFrames.poll());
			
			p.setPriority(buffer.elementAt(0));
			
			//Assign Unique PID
			while(processScheduler.getPCB(tmp_pid) != null) ++tmp_pid;
			p.setPID((byte)tmp_pid);
			
			p.setProcessSize((short)buffer.size());
			
			
			return p;
		}
	}
	
	public void initRegisters(PCB currPCB){
		int tmp = 0;
		
		//Computing Size of DS
		tmp = buffer.elementAt(3);
		tmp = tmp << 8;
		tmp = tmp | buffer.elementAt(4);
		
		currPCB.setRegister(DB, (short)(5)); //2 header bytes + 3 info bytes
		currPCB.setRegister(DL, (short)tmp);

		//Computing Base of Code Segment
		tmp += 5;
		currPCB.setRegister(CB, (short)( tmp+3 )); //+3 info offset
		currPCB.setRegister(CC, (short)( CS_INSTR_ADDR )); //first instruction of code is at segment 1 offset 0 ie. 0x4000
		
		//Computing Size of CS
		tmp = (buffer.elementAt(tmp+1) << 8) | buffer.elementAt(tmp+2);
		currPCB.setRegister(CL, (short)tmp);
		
		//Computing Base of Stack Segment
		tmp = buffer.size(); //Because stack is at the very end of the process
		currPCB.setRegister(SB, (short)(tmp));
		currPCB.setRegister(SC, (short)(SS_INSTR_ADDR));
		currPCB.setRegister(SL, (short) STACK_SIZE);
	}
	
	private void writeProgramToMemory(String fileName){
		int numFramesRequired = 0;
		int bufferIndex = 0; //holds the index of buffer that holds the process to be written
		int frameBaseAddr = 0; //holds the base address of a memory frame
		int frameNum = 0; //holds the frame number to be written to the page table
		
		if(fileName != "clone_process")
			numFramesRequired = (int) Math.ceil(( (double) (buffer.size()+STACK_SIZE) / PAGE_SIZE )); //StackSize = 256
		else
			numFramesRequired = (int) Math.ceil(( (double) (buffer.size()) / PAGE_SIZE )); //StackSize = 256
		
		Vector<Integer> pTable = new Vector<Integer>(numFramesRequired);
		PCB currPCB = createPCB();
		
		if(M.freeFrames.size() < numFramesRequired){
			System.out.println("Error: Process could not be loaded because there are not enough free frames available.");
			//Generate Trap To OS
			return;
		}
		
		if(buffer.elementAt(0) < 0 || buffer.elementAt(0) > 31){
			System.out.println("Error: Invalid Priority; Can not load process into memory.");
			//Generate Trap To OS
			return;
		}
		
		for(int pageNum=0; pageNum<numFramesRequired; pageNum++){
			frameNum = M.freeFrames.elementAt(0);
			frameBaseAddr = frameNum * PAGE_SIZE; //frame number times frame size = frameBaseAddr
			M.freeFrames.remove(0); //remove frame from available frames list
			pTable.add(pageNum, frameNum); //add frame number to page table
			
			/*
			for(int addr = frameBaseAddr;
				(addr < frameBaseAddr+STACK_SIZE) && (bufferIndex < buffer.size()); addr++){ //write program to frame
				M.writeByteToPhysical(addr, buffer.elementAt(bufferIndex));
				++bufferIndex;
			}
			*/
			
			for(int addr = frameBaseAddr;
				(addr < frameBaseAddr+PAGE_SIZE) && (bufferIndex < buffer.size()); addr++){ //write program to frame
				M.writeByteToPhysical(addr, buffer.elementAt(bufferIndex));
				++bufferIndex;
			}
		}
		
		//Set Appropriate Registers
		initRegisters(currPCB);
		
		//Attach Page Table To PCB
		if(currPCB.attachPageTable(pTable) == false){
			System.out.println("Warning: The program requires more than 27 frames. Ternimating process load request.");
			//Generate Trap To OS
		}
		
		//Add Process To Ready Queue in processSceduler
		processScheduler.pushIntoQueue(currPCB, buffer.elementAt(0));
	}
	
	//For cloning support
	public void cloneProcess(PCB tmpPcbPtr){
		int tmp_pid = tmpPcbPtr.getPID();
		buffer.clear(); //clear old data
		
		for(int i=0; i<tmpPcbPtr.getPageTableSize(); i++){ //iterate over frames
			int base = tmpPcbPtr.getPageTableEntry(i) * M.FRAME_SIZE;
			
			for(int addr=base; addr<(base+M.FRAME_SIZE); addr++){//iterate over offset
				buffer.add(M.readByteFromPhysical(addr));
			}
		}
		
		//Calculate PID and return - PID can't be negative - add fix for negative numbers
		while(processScheduler.getPCB(tmp_pid) != null) ++tmp_pid;
		
		//Write Program to Ram - same pid should be assign from function below
		writeProgramToMemory("clone_process");
		
		//Copy Register State into Clone
		processScheduler.getPCB(tmp_pid).updateResigsterState(tmpPcbPtr.getRegisterState());
		processScheduler.getPCB(tmp_pid).setProcessSize(tmpPcbPtr.getProcessSize());
	}
}
