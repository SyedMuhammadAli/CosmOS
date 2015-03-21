package CosmOS;
import java.util.Vector;

/* MemoryManagementUnit:
 * An intermediary address translation layer
 * between the CPU and the Memory. Provides
 * support to the Memory class for address
 * translation.
 */
	
public class MemoryManagementUnit {
	private static final int	FRAME_SIZE = 128,
								INFO_SEGMENT = 0,
								DATA_SEGMENT = 1,
								CODE_SEGMENT = 2,
								STACK_SEGMENT = 3;
	
	private static final int	CB=0x11, CL=0x12, CC=0x13,
								SB=0x14, SL=0x15, SC=0x16,
								DB=0x17, DL=0x18;
	
	private Processor cpu;
	
	public MemoryManagementUnit(Processor p){
		cpu = p;
	}
	
	public short resolveLogicalAddress(short addr){
		addr = segmentationUnit(addr);
		addr = pagingUnit(addr);
		
		return addr;
	}
	
	/* segmentationUnit */
	private short segmentationUnit(short addr){
		short	mask = (short)0xC000,
				segmentNum = (short)( (addr >> 14) & 0x0003 ), //drop offset, kill sign bit
				offset = (short)(addr & 0x3FFF), //kill segment number
				new_addr = 0,
				registerBaseCode = 0,
				registerLimitCode = 0;
		
		switch(segmentNum){
			case 3:
				//generate trap to OS
				break;
				
			case 0: //Data Segment
				registerBaseCode = DB;
				registerLimitCode = DL;
				break;
				
			case 1: //Code Segment
				registerBaseCode = CB;
				registerLimitCode = CL;
				break;
				
			case 2: //Stack Segment
				registerBaseCode = SB;
				registerLimitCode = SL;
				break;
				
			default:
				//Generate Trap to OS
		}
		
		if(offset > cpu.getRegisterValue(registerLimitCode)){
			cpu.setDebugInfoString(String.format("Segmentation Fault @ Segment: %d, Offset: %d, RegBase: %d, RegLimit: %d, Addr: %h\n", segmentNum, offset, cpu.getRegisterValue(registerBaseCode), cpu.getRegisterValue(registerLimitCode), addr));
			//Generate Trap To OS
		}
		
		cpu.setDebugInfoString(String.format("\nSegment Access Request => Segment: " + segmentNum + " Offset: " + offset));
		new_addr = (short) (cpu.getRegisterValue(registerBaseCode) + offset);
		
		return new_addr;
	}
	
	private short pagingUnit(short addr){
		int offset = addr & 0x007F; //getting rid of the page offset
		int pageNum = (addr >> 7) & 0x01FF; //getting rid of page number, killing sign bit if any
		
		cpu.setDebugInfoString(String.format("Page Access Request => Page: " + pageNum + " Offset: " + offset));
		addr = (short) (cpu.currentProcess.getPageTableEntry(pageNum) * FRAME_SIZE);
		//addr = (short) (cpu.currentProcess.pageTable.elementAt(pageNum) * FRAME_SIZE);
		
		addr += offset;
		//System.out.println("Return Paging Unit: " + addr);
		return addr;
	}
	
	private boolean isKernelCode(int addr)
	{
		if( (addr & 0x8000) != 0 ) //if 15th bit is set
			return true;
		else
			return false;
	}
}
