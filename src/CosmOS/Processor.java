package CosmOS;
/* Processor:
 * 
 * Every beloved object is the center point of a paradise ~Novalis
 * 
 * Instruction set will be implemented here
 * This class will interact with Registers and
 * The memory and perform operations in the
 * Instruction set.
 * */


public class Processor {
	
	/* Class Constants */
	public static final int MAX_REGISTERS = 32;
	
	/* Register index aliases:
	 * This naming convention used the acronyms for register names to increase readability.
	 * AC stands for Accumulator, DR for Data Register, IR for Instruction Register, AR for Address Register
	 * T[1....N] for Temporary Registers
	 * ZV stands for Zero Valued Register
	 * */
	private static final int	ZV=0x10, CB=0x11, CL=0x12, CC=0x13, SB=0x14, SL=0x15,
								SC=0x16, DB=0x17, DL=0x18, FL=0x19;
	
	private static final int	AC=0x1A, DR=0x1B, IR=0x1C, AR=0x1D, T1=0x1E, T2=0x1F;
	
	/* Constants for flag register's bit numbers. */
	private static final int	CARRY = 0, ZERO = 1, SIGN = 2, OVERFLOW = 3;
	
	/* 0-15 General Purpose Registers
	 * 15-31 Special Purpose Registers
	 * */
	public Register R[] = new Register[MAX_REGISTERS];
	
	/* Memory Unit Reference:
	 * The Memory unit is outside the Processor. Therefore, only
	 * a reference to it is declared inside the processor to allow
	 * access. The Memory Unit, however, is declared in VirtualMachine. 
	 * */
	private Memory M;
	
	/* runningProcess reference points to the PCB of the process currently under execution.
	 * This allows the stack inside the PCB to be modified by the Processor. Also, it allows
	 * other components of the VM to access information about the process which is currently
	 * under execution.
	 * */
	public PCB currentProcess;
	
	/* programStack - Helps the Processor Class push and pop elements out of the stack */
	public ProgramStack programStack;
	
	private String debugInfoString = new String();
	
	private short clock;
	
	/* Constructor: Allocate register objects to register array */
	public Processor(Memory mem){
		//Attach memory
		this.M = mem;
		
		//Allocating Register Objects
		for(int i=0; i<MAX_REGISTERS; i++){
			R[i] = new Register();
			R[i].setValue((short) 0);
		}
		
		//Configure Stack Class
		programStack = new ProgramStack(R[SB], R[SL], R[SC]);
		programStack.setMemoryLink(M);
	}
	
	/* Returns the value of the internal CPU Clock */
	public short clock(){ return clock; }
	
	/* Provides an interface to the external entities to set the runningProcess reference */
	public void switchProcessTo(PCB currProc){
		//Save state of current PCB
		if(currentProcess != null)
			currentProcess.updateResigsterState(R);
		
		//Restore Processor State with new PCB
		currentProcess = currProc;
		restoreRegistersState(currentProcess.getRegisterState());
	}
	
	/* restoreRegisters - Restores the state of the registers given an array of registers as parameter */
	private void restoreRegistersState(Register Reg[]){
		for(int i=0; i<MAX_REGISTERS; i++){
			R[i].setValue( Reg[i].value() ); //To ensure that refs of registers inside CPU aren't changed
		}
	}
	
	/* executeNextInstructions - executes the next instruction pointed to be the CodeCounter
	 * Returns true if last instruction has been executed or if the process needs to terminate.
	 * */
	public boolean execNextIntruction(){
		setDebugInfoString(String.format("Processor: CodeCounter => %h", R[CC].value()));
		byte opcode = M.read( R[CC].value() );
		int instOffset = exec(opcode);
		clock += instOffset; //Increment CPU clock - 1 ms =~ 1 instr.
		
		if(currentProcess != null){
			currentProcess.incrementExecutionTime(); //Increment +2 in Execution Time inside PCB
		} else {
			setDebugInfoString("Critical Error: No PCB loaded into the CPU. Something really bad happened in the execNextInstruction().");
			return false;
		}
		
		if(instOffset == 0) { //If Illegal Instruction is encountered
			setDebugInfoString("Terminating process due to illegal instruction.");
			//Generate Trap To OS
			return true;
		} else if(opcode != 0xF3) { //If last instruction is NOT encountered
			R[CC].inc( instOffset );
			return false;
		} else { //If last instruction IS encountered
			return true;
		}
	}
	
	/* getInstructionType - returns the classification
	 * of given opcode. For example: returns 1 for RRI,
	 * 3 for RII, 5 for MI, 7 for SOI, and 15 for NOI.
	 * */
	private byte getInstructionType(byte opcode){
		return (byte)(opcode >> 4);
	}
	
	/* execute - executes the operation specified
	 * by the opcode.
	 * 
	 * This function identifies the type of instruction
	 * and calls their respective handlers. Note that
	 * those handlers use the function readOperands that
	 * reads the operands into R[T1] and R[T2]. 
	 * */
	public int exec(byte opcode){
		short instCount = 0;
		setDebugInfoString(String.format("__Instruction: %2h\n", opcode));
		switch(getInstructionType(opcode)){
			case 0x1: //register reference instruction
				execRRI(opcode);
				instCount = 3;
				break;
			
			case 0x3: //register immediate instruction
				execRII(opcode);
				instCount = 4;
				break;
			
			case 0x5: //memory instruction
				execMI(opcode);
				instCount = 4;
				break;
			
			case 0x7: //single operand instruction
				execSOI(opcode);
				instCount = 2;
				break;
			
			case 0xF: //no operand instruction
				execNOI(opcode);
				instCount = 1;
				break;
			
			default: //invalid instruction
				setDebugInfoString("Unidentified opcode passed to exec(). The instruction code does not exist.");
				instCount = 0;
				break;
		}
		
		return instCount;
	}
	
	/* execRRI - Handles the Register-Register Instruction */
	private void execRRI(byte opcode){
		readOperands( getInstructionType(opcode) );
		
		switch(opcode){
			case 0x16: //R[T1] <= R[T2]
				R[R[T1].value()].copy( R[R[T2].value()] );
				break;
				
			case 0x17: //R[T1] <= R[T1] + R[T2]
				R[R[T1].value()].add( R[R[T2].value()] );
				break;
			
			case 0x18: //R[T1] <= R[T1] - R[T2]
				R[R[T1].value()].sub( R[R[T2].value()] );
				break;
			
			case 0x19: //R[T1] <= R[T1] * R[T2]
				R[R[T1].value()].mul( R[R[T2].value()] );
				break;
			
			case 0x1A: //R[T1] <= R[T1] / R[T2]
				R[R[T1].value()].div( R[R[T2].value()] );
				break;
			
			case 0x1B: //R[T1] <= R[T1] && R[T2]
				R[R[T1].value()].and( R[R[T2].value()] );
				break;
			
			case 0x1C: //R[T1] <= R[T1] || R[T2]
				R[R[T1].value()].or( R[R[T2].value()] );
				break;
			
			default: //unidentified statement
				setDebugInfoString("Unidentified opcode passed to execRRI().");
				//Generate Trap To OS
				break;
		}
	}
	
	/* execRII - Handles the Register-Immediate Instruction */
	private void execRII(byte opcode){
		readOperands( getInstructionType(opcode) );
		
		switch(opcode){
			case 0x30: //R[i] <= M[AR]
				R[R[T1].value()].copy( R[T2] );
				break;
				
			case 0x31: //R[i] <= R[i] + M[AR]
				R[R[T1].value()].add( R[T2] );
				break;
			
			case 0x32: //R[i] <= R[i] - M[AR]
				R[R[T1].value()].sub( R[T2] );
				break;
			
			case 0x33: //R[i] <= R[i] * M[AR]
				R[R[T1].value()].mul( R[T2] );
				break;
			
			case 0x34: //R[i] <= R[i] / M[AR]
				R[R[T1].value()].div( R[T2] );
				break;
			
			case 0x35: //R[i] <= R[i] && M[AR]
				R[R[T1].value()].and( R[T2] );
				break;
			
			case 0x36: //R[i] <= R[i] || M[AR]
				R[R[T1].value()].or( R[T2] );
				break;
				
			case 0x37: // CC = T2 if ZeroFlag is 0
				if(R[FL].bitIsSet(ZERO) == 0){
					R[CC].setValue(R[T2]);
				}
				break;
				
			case 0x38: // CC = T2 if ZeroFlag is 1
				if(R[FL].bitIsSet(ZERO) == 1){
					R[CC].setValue(R[T2]);
				}
				break;
				
			case 0x39: // CC = T2 if CarryFlag is 1
				if(R[FL].bitIsSet(CARRY) == 1){
					R[CC].setValue(R[T2]);
				}
				break;
				
			case 0x3A: // CC = T2 if SignFlag is 1
				if(R[FL].bitIsSet(SIGN) == 1){
					R[CC].setValue(R[T2]);
				}
				break;
				
			case 0x3B: //CC = T2
				R[CC].setValue(R[T2]);
				break;
				
			case 0x3C: //CALL
				programStack.push(R[CC]);
				R[CC].setValue(R[T2]);
				break;
				
			case 0x3D: //ACT - will call the ISR for service
				//Will be implemented when the ISR in known
				break;
			
			default: //unidentified statement
				setDebugInfoString("Unidentified opcode passed to execRII().");
				//Generate Trap To OS
				break;
		}
	}
	
	/* execMI - Handles the Memory Instruction */
	private void execMI(byte opcode){
		readOperands( getInstructionType(opcode) );
		byte up, lw;
		
		switch(opcode){
			case 0x51: //R[i] <= M[ M[AR] ]
				R[R[T1].value()].setValue( M.read( R[T2].value()  ) );
				break;
				
			case 0x52: //R[i] <= M[ M[AR] ]
				up = (byte) (R[T1].value() >> 8);
				lw = (byte) (R[T1].value() & 0xFF);
				
				M.write(R[T2].value(), up ); //Write Upper Order 8 Bits
				M.write((short)(R[T2].value()+1), lw ); //Write Lower Order 8 Bits
				break;
			
			default: //unidentified statement
				setDebugInfoString("Unidentified opcode passed to execMI().");
				//Generate Trap To OS
				break;
		}
	}
	
	/* execSOI - Handles the Single-Operand Instruction */
	private void execSOI(byte opcode){
		readOperands( getInstructionType(opcode) );
		
		switch(opcode){
			case 0x71: //Shift Left
				R[R[T1].value()].shl();
				break;
				
			case 0x72: //Shift Right
				R[R[T1].value()].shr();
				break;
			
			case 0x73: //Rotate Left
				R[R[T1].value()].rtl();
				break;
			
			case 0x74: //Rotate Right
				R[R[T1].value()].rtr();
				break;
			
			case 0x75: //Increment 
				R[R[T1].value()].inc();
				break;
			
			case 0x76: //Decrement
				R[R[T1].value()].dec();
				break;
			
			case 0x77: //Push register into stack
				programStack.push(R[ R[T1].value() ]);
				break;
			
			case 0x78: //Pop register from stack
				R[ R[T1].value() ].copy( programStack.pop() );
				break;
				
			default: //unidentified statement
				setDebugInfoString("Unidentified opcode passed to execSOI().");
				//Generate Trap To OS
				break;
		}
	}
	
	/* execNOI - Handles the No-Operand Instruction */
	private void execNOI(short opcode){
		switch(opcode){
			case 0xF1:
				R[CC].setValue(programStack.pop());
				break;
				
			case 0xF2:
				//No Operation
				break;
				
			case 0xF3:
				//End of Process
				break;
				
			default: //unidentified statement
				break;
		}
	}
	
	/* readOperands - Reads the operands from memory into registers T1 & T2.
	 * In case of a RRI, it loads the register codes from memory and stores them
	 * in T1 & T2.
	 * In case of RII or MI, it loads the register code, and the memory address into
	 * T1 & T2.
	 * In case of SOI, it loads the single register code into T1 and leaves T2 intact.
	 * */
	private void readOperands(short instructionType){
		switch(instructionType){
			case 0x1:
				R[AR].copy( R[CC] ); // AR <= CC
				R[T1].setValue( M.read( (short)(R[AR].value()+1) ) ); // T1 <= M[AR+1]
				R[T2].setValue( M.read( (short)(R[AR].value()+2) ) ); // T2 <= M[AR+2]
				break;
			
			case 0x3:
			case 0x5:
				R[AR].copy( R[CC] ); // AR <= CC
				R[T1].setValue( M.read( (short)(R[AR].value()+1) ) ); // T1 <= M[AR+1]
				R[T2].setValue( M.readAddr( (short)(R[AR].value()+2) ) ); // T2 <= M[AR+2]
				//R[T2].setValue( M.read( R[T2].value() )); // T2 <= M[T2] : to resolve indirect address
				break;
			
			case 0x7:
				R[AR].copy( R[CC] ); // AR <= CC
				R[T1].setValue( M.read( (short)(R[AR].value()+1) ) ); // T1 <= M[AR+1]
				break;
			
			case 0xF: //Do nothing
				break;
			
			default:
				setDebugInfoString("Unidentified instructionType passed to readOperands().");
				//Generate Trap to OS
				break;
		}
	}
	
	/* getRegisterInfoStr
	 * Returns the state of all the general and special
	 * purpose register to the output.
	 */
	public String getRegisterInfoStr(){
		String regInfoStr = new String(">> CPU Registers\n");
		
		for(int i=0; i<MAX_REGISTERS; i++){
			regInfoStr += String.format("R[%2h]: " + (R[i].value() & 0xFFFF) + "\t", i);
			
			if(i%6 == 5){
				regInfoStr += "\n";
			}
		}
		
		return regInfoStr+"\n";
	}
	
	public void execProgram(short addr) {
		R[CC].setValue(addr);
		int instOffset = 0;
		
		int i=0;
		while(M.read(R[CC].value()) != (byte)0xF3){
			System.out.printf("%h ", M.read(R[CC].value()));
			
			instOffset = exec(M.read( R[CC].value() ));
			R[CC].inc( instOffset );
			
			if(i==20) break; i++;
		}
	}
	
	public short getRegisterValue(int registerCode){
		if(registerCode < 0 || registerCode > 31){
			System.out.println("Invalid Register Requested from Processor @ getRegisterValue.");
			//Generate Trap To OS
		}
		
		return R[registerCode].value();
	}
	
	//Debug String Functions
	public void setDebugInfoString(String msg){
		debugInfoString += msg + "\n";
	}
	
	public String getDebugInfoString(){
		String tmp = new String(debugInfoString);
		debugInfoString = ""; //clear string
		
		return tmp;
	}
}
