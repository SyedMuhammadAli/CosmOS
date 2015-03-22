/* Instruction set will be implemented here
 * This class will interact with Registers and
 * The memory and perform operations in the
 * Instruction set.
 * */

import java.util.Stack;

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
	
	/* Constants for flag register's bit numbers.
	 * */
	private static final int	CARRY = 0, ZERO = 1, SIGN = 2, OVERFLOW = 3;
	
	/* 0-15 General Purpose Registers
	 * 15-31 Special Purpose Registers
	 * */
	private Register R[] = new Register[MAX_REGISTERS];
	
	private Stack<Register> stack = new Stack<Register>();
	
	/* Memory Unit Reference:
	 * The Memory unit is outside the Processor. Therefore, only
	 * a reference to it is declared inside the processor to allow
	 * access. The Memory Unit, however, is declared in VirtualMachine. 
	 * */
	private Memory M;
	
	/* Constructor: Allocate register objects to register array */
	public Processor(Memory mem){
		//Attach memory
		this.M = mem;
		
		//Allocating Register Objects
		for(int i=0; i<MAX_REGISTERS; i++){
			R[i] = new Register();
			R[i].setValue((short) 0);
		}
		
		//Initializing Register Objects
		R[CB].setValue((short) 2048);
		R[CC].setValue((short) 2048);
		
		//Debugging
		//R[2].setValue((short)3);
		//R[3].setValue((short)5);
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
		
		switch(getInstructionType(opcode)){
			case 1: //register reference instruction
				execRRI(opcode);
				instCount = 3;
				break;
			
			case 3: //register immediate instruction
				execRII(opcode);
				instCount = 4;
				break;
			
			case 5: //memory instruction
				execMI(opcode);
				instCount = 4;
				break;
			
			case 7: //single operand instruction
				execSOI(opcode);
				instCount = 2;
				break;
			
			case 15: //no operand instruction
				execNOI(opcode);
				instCount = 1;
				break;
			
			default: //invalid instruction
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
				System.out.println("Unidentified opcode passed to execRRI().");
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
				stack.push(R[CC]);
				R[CC].setValue(R[T2]);
				break;
				
			case 0x3D: //ACT - will call the ISR for service
				//Will be implemented when the ISR in known
				break;
			
			default: //unidentified statement
				System.out.println("Unidentified opcode passed to execRII().");
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
				
				M.write((int)R[T2].value(), up ); //Write Upper Order 8 Bits
				M.write((int)R[T2].value()+1, lw ); //Write Lower Order 8 Bits
				break;
			
			default: //unidentified statement
				System.out.println("Unidentified opcode passed to execMI().");
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
				stack.push(R[ R[T1].value() ]);
				break;
			
			case 0x78: //Pop register from stack
				R[ R[T1].value() ].copy( stack.pop() );
				break;
				
			default: //unidentified statement
				System.out.println("Unidentified opcode passed to execSOI().");
				break;
		}
	}
	
	/* execNOI - Handles the No-Operand Instruction */
	private void execNOI(short opcode){
		switch(opcode){
			case 0xF1:
				R[CC].setValue(stack.pop());
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
				R[T1].setValue( M.read( R[AR].value()+1 ) ); // T1 <= M[AR+1]
				R[T2].setValue( M.read( R[AR].value()+2 ) ); // T2 <= M[AR+2]
				break;
			
			case 0x3:
			case 0x5:
				R[AR].copy( R[CC] ); // AR <= CC
				R[T1].setValue( M.read( R[AR].value()+1 ) ); // T1 <= M[AR+1]
				R[T2].setValue( M.readAddr( R[AR].value()+2 ) ); // T2 <= M[AR+2]
				//R[T2].setValue( M.read( R[T2].value() )); // T2 <= M[T2] : to resolve indirect address
				break;
			
			case 0x7:
				R[AR].copy( R[CC] ); // AR <= CC
				R[T1].setValue( M.read( R[AR].value()+1 ) ); // T1 <= M[AR+1]
				break;
			
			case 0xF: //Do nothing
				break;
			
			default:
				System.out.println("Unidentified instructionType passed to readOperands().");
				break;
		}
	}
	
	/* printState - Prints out the state of all the
	 * general and special purpose register to the
	 * output.
	 * */
	public void printState(){
		/* Print out the state of the processor */
		
		for(int i=0; i<32; i++){
			System.out.printf("R[%2h]: " + R[i].value() + "\t", i);
			
			if(i%8 == 7){
				System.out.println();
			}
		}
	}

	public void execProgram(short addr) {
		R[CC].setValue(addr);
		int instOffset = 0;
		
		//int i=0;
		while(M.read(R[CC].value()) != (byte)0xF3){
			//System.out.printf("%h ", M.read(R[CC].value()));
			
			instOffset = exec(M.read( R[CC].value() ));
			R[CC].inc( instOffset );
			
			//if(i==10) break; i++;
		}
	}
}
