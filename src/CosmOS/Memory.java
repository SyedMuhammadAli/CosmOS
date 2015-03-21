package CosmOS;
import java.util.Vector;

public class Memory {
	public final int MEM_SIZE = 65536;
	public final int FRAME_SIZE = 128;
	
	private byte register[] = new byte[MEM_SIZE];
	private MemoryManagementUnit memManager;
	
	public byte buffer;
	
	public Vector<Integer> freeFrames;
	
	public Memory(){
		freeFrames = new Vector<Integer>(); //Initialize freeFrames Table
		
		//Reserving 32 frames for kernel
		
		for(int i=32; i<MEM_SIZE/FRAME_SIZE; i++){ //Generate Free Frames Tables
			freeFrames.add(i);
		}
		
		//printAvailableFrames();
	}
	
	public void setMemoryUnit(MemoryManagementUnit mmu){
		memManager = mmu;
	}
	
	public byte read(short addr){
		addr = memManager.resolveLogicalAddress(addr);
		return register[addr];
	}
	
	public short readAddr(short addr){
		addr = memManager.resolveLogicalAddress(addr);
		int tmp = 0; //holds intermediate results
		
		tmp = register[addr];
		tmp = tmp << 8;
		tmp = tmp | register[addr+1];
		
		return (short)tmp;
	}
	
	public void write(short addr){
		addr = memManager.resolveLogicalAddress(addr);
		register[addr] = buffer;
	}
	
	public void write(short addr, byte value){
		addr = memManager.resolveLogicalAddress(addr);
		register[addr] = value;
	}
	
	/* Allows data to be written using physical address bypassing the MMU */
	public void writeByteToPhysical(int addr, byte value){
		register[addr] = value;
	}
	
	public void writeToPhysical(int addr, short value){
		//System.out.printf("wtp: value %d, addr %d\n", value, addr);
		byte	upper = (byte) (value >> 8),
				lower = (byte) (value & 0x00FF);
		
		//writeToPhysical(addr, upper);
		//writeToPhysical(addr+1, lower);
		
		register[addr] = upper;
		register[addr+1] = lower;
		
		//System.out.println("-wtp: " + upper + ", " + lower);
	}
	
	public byte readByteFromPhysical(int addr){
		return register[addr];
	}
	
	public short readShortFromPhysical(int addr){
		short value;
		value = (short) register[addr];
		value = (short) (value << 8);
		value = (short) (value | register[addr+1]);
		
		return value;
	}
	
	/* freeFrame - Add the givens frame to the free frame list making it available for reuse */
	public void freeFrame(int frameNum){
		//check if frame is not in ready queue
		freeFrames.add(frameNum);
	}
	
	public String getAvailableFrameInfoStr(){
		String frameListStr = new String(">> Free Frames:\nFrame #\tPhysical Address\n");
		
		for(int i=0; i<freeFrames.size(); i++){ //Generate Free Frames Tables
			frameListStr += String.format("%h\t%h\n", freeFrames.elementAt(i), freeFrames.elementAt(i)*FRAME_SIZE);
		}
		
		return frameListStr;
	}
}
