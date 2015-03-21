import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

public class FileSystem {
	Vector<Byte> v;
	Memory m;
	int base_addr;
	
	public FileSystem(Memory mem, int addr){
		m = mem;
		base_addr = addr;
		
		int b = 0;
		v = new Vector<Byte>();
		FileInputStream fin = null;
		
		try {
			fin = new FileInputStream("add_mul.proc");
		} catch(FileNotFoundException fnfe) {
			System.out.println("Can't find file.");
		}
		
		try{
			while( (b = fin.read()) != -1){
				v.add((byte)b);
			}
		} catch(IOException ioe) {
			//foo
		}
	}

	public void printFileRead(){
		for(int i=0; i<v.size(); i++){
			System.out.printf("%h ", v.elementAt(i));
		}
	}
	
	public void writeToMemory(){
		byte b = 0;
		
		for(int i=0; i<v.size(); i++)
			m.write(base_addr+i, v.elementAt(i));
	}
}
