package CosmOS;
public class Register {
	private short value = 0;
	
	//Constructor
	public Register(){ value = 0; }
	
	public Register(short v){ value = v; }
	
	//Prints the register value
	public void printValue(){
		System.out.println(value);
	}
	
	//Increment the register with the given offset
	public void inc(int off){
		value += off;
	}
	
	//Returns the register contents
	public short value(){
		return value;
	}
	
	/* setValue - Provides an interface to set the 
	 * register value either by passing in a byte,
	 * a short, or an object of type Register.
	 * */
	public void setValue(byte t){
		value = (short)t;
	}
	
	public void setValue(short t){
		value = t;
	}
	
	public void setValue(Register r){
		this.value = r.value();
	}
	
	/* copy - A more readable alternative to setValue.
	 * Provides a more logically readable code when copying
	 * a registers.
	 * */
	public void copy(Register r){
		value = r.value();
	}
	
	/* Returns one if flag bit is set, 0 otherwise.
	 * */
	public int bitIsSet(int bitNum){
		switch(bitNum){
			case 0: return (value & 1);
			case 1: return (value & 2);
			case 2: return (value & 4);
			case 3: return (value & 8);
			default: return 0;
		}
	}
	
	/* Instruction Set Implementation:
	 * All the following member functions implement the
	 * instruction set. They do exactly what their names
	 * suggests and therefore their individual description
	 * is omitted. 
	 * All of them either take an object of type Register as
	 * an argument, or take no arguments at all.
	 * */
	
	public void add(Register r){
		value += r.value();
	}
	
	public void sub(Register r){
		value -= r.value();
	}
	
	public void mul(Register r){
		value *= r.value();
	}
	
	public void div(Register r){
		value /= r.value();
	}
	
	public void and(Register r){
		//logical AND
	}
	
	public void or(Register r){
		//logical OR
	}
	
	public void shl(){
		value = (short)(value << 1);
	}
	
	public void shr(){
		value = (short)(value >> 1);
	}
	
	public void rtl(){
		short mask = 128;
		boolean setbit = false;
		
		if((value & mask) == 128){
			mask = 1;
			setbit = true;
		}
		
		value = (short)(value << 1);
		
		if(setbit){
			value = (short)(value | mask);
		}
	}
	
	public void rtr(){
		short mask = 1;
		boolean setbit = false;
		
		if((value & mask) == 1){
			mask = 128;
			setbit = true;
		}
		
		value = (short)(value >> 1);
		
		if(setbit){
			value = (short)(value | mask);
		}
	}
	
	public void inc(){
		++value;
	}
	
	public void dec(){
		--value;
	}
}
