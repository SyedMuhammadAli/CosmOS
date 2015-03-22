/* This is the 'main' class that holds
 * all the pieces together.
 * The virtual machine class provides an
 * abstraction of a real world computer system.
 * It has a ram and a processor. It facilitates
 * the interaction between all these different 
 * components, and provides a layered approach
 * to the whole VM and the Operating System.
 * 
 * Author: Syed Muhammad Ali
 **/

public class VirtualMachine {
	static Memory kingston = new Memory();
	static Processor intel = new Processor(kingston);
	static FileSystem hdd = new FileSystem(kingston, 2048);
	
	public static void main(String[] args){
		hdd.writeToMemory();
		
		kingston.write(4096, (byte)2);
		kingston.write(4097, (byte)3);
		
		intel.execProgram((short)2048);
		intel.printState();
	}
}
