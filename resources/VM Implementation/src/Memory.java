public class Memory {
	public final int MEM_SIZE = 65536;
	private byte register[] = new byte[MEM_SIZE];
	public byte buffer;
	
	public byte read(int addr){
		return register[addr];
	}
	
	public short readAddr(int addr){
		int tmp = 0; //holds intermediate results
		tmp = register[addr];
		tmp = tmp << 8;
		tmp = tmp | register[addr+1];
		
		return (short)tmp;
	}
	
	public void write(int addr){
		register[addr] = buffer;
	}
	
	public void write(int addr, byte value){
		register[addr] = value;
	}
}
