package CosmOS;

public class ProgramStack {
	private Register base, limit, counter;
	private Memory M;
	
	public ProgramStack(Register b, Register l, Register c){
		base = b;
		limit = l;
		counter = c;
	}
	
	public void setMemoryLink(Memory mem){ M = mem; }
	
	public Register pop(){
		if(counter.value() <= base.value()){
			System.out.println("Stack Underflow.");
			//Generate Trap To OS
			return null;
		}
		
		short	value = (short) M.read( (short)(base.value()+counter.value()-1) ); //fetch lower order bits
				value = (short) ( value | M.read( (short)(base.value()+counter.value()-2  << 8) ) ); //fetch upper order bits
		
		counter.dec();
		counter.dec();
		
		return new Register(value);
	}
	
	public void push(Register r){
		if(base.value()+limit.value() <= counter.value()){
			System.out.println("Stack Overflow.");
			//Generate Trap To OS
			return;
		}
		
		byte	upper = (byte) (r.value() >> 8),
				lower = (byte) (r.value() & 0x00FF);
		
		//System.out.printf("Pusing @ %h", counter.value());
		M.write((short)(counter.value()), upper);
		
		//System.out.printf("Pusing @ %h", counter.value());
		M.write((short)(counter.value()+1), lower);
		
		counter.inc();
		counter.inc();
	}
	
	//Debug Function
	public void printState(){
		System.out.printf("Stack =>  Base: %d, Limit: %d, Counter: %h\n", base.value(), limit.value(), counter.value()&0xFFFF);
	}
}
