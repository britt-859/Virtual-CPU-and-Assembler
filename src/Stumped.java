import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;
/* Main class that runs all code. Code will be in a single executable file
 * 
 */

public class Stumped {
	public static int in;
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("USAGE: java Stumped.java <object file> [integer in value]");
		} else {
			String fileName = "";
			if (args.length == 1) {
				fileName = args[0];
			} else {
				for (int i = 0; i < args.length-1; i++) {
					fileName = fileName + args[i];
				}
				in = Integer.parseInt(args[args.length-1]);
			}
			Memory memory = new Memory(fileName);
			Stack<Integer> st = new Stack<>();  //the stack used to run the program
			RunProgram program = new RunProgram(memory, st, in);
			program.executeProgram();
		}
	}	
}

/* Performs execution of the code
 * 
 */
class RunProgram {
	private Memory memory;
	private Stack<Integer> st;
	int in; //IN value
	
	public RunProgram(Memory m, Stack<Integer> s, int n) {
		memory = m;
		st = s;
		in = n;
	}
	
	public void executeProgram() {
		int opcode;
		opcode = memory.getValueAtAddress(memory.getProgramCounter());
		while (opcode != 3840) { //while opcode is not HALT
			//execute the code for the given opcode
			if (opcode == 61443 && st.peek() == 0) //cannot divide by 0
				break;
			if (opcode != 0) {
				execute(opcode, st, memory);
			} else 
				memory.setProgramCounter(memory.getProgramCounter() + 1);
			opcode = memory.getValueAtAddress(memory.getProgramCounter());
		}
	}
	
	private void execute(int opcode, Stack<Integer> st, Memory memory) {
		if (opcode < 4096) { //opcodes 0 - 0900
			if (opcode == 256)
				pushPc();
			else if (opcode == 512)
				popPc();
			else if (opcode == 768)
				ld();
			else if (opcode == 1024)
				st();
			else if (opcode == 1280)
				dup();
			else if (opcode == 1536)
				drop();
			else if (opcode == 1792)
				over();
			else if (opcode == 2048)
				dNext();
			else  
				swap();
		} else if (opcode >= 4096 && opcode < 61440) { //opcodes 1000 - E000
			if (opcode < 8192) 
				pushI(opcode - 4096);
			else if (opcode < 12288)
				pushA(opcode - 8192);
			else if (opcode < 16384)
				popA(opcode - 12288);
			else if (opcode < 20480)
				jmpA(opcode - 16384);
			else if (opcode < 24576)
				jzA(opcode - 20480);
			else if (opcode < 53248)
				jnzA(opcode - 24576);
			else if (opcode < 57344)
				in();  //port 0, default port
			else 
				out();
		} else {  //opcodes F000 - F014
			if (opcode == 61440)
				add();
			else if (opcode == 61441)
				sub();
			else if (opcode == 61442)
				mul();
			else if (opcode == 61443) {
				div();
			}
			else if (opcode == 61444)
				mod();
			else if (opcode == 61445)
				shl();
			else if (opcode == 61446)
				shr();
			else if (opcode == 61447)
				band();
			else if (opcode == 61448)
				bor();
			else if (opcode == 61449)
				bxor();
			else if (opcode == 61450)
				and();
			else if (opcode == 61451)
				or();
			else if (opcode == 61452)
				eq();
			else if (opcode == 61453)
				ne();
			else if (opcode == 61454)
				ge();
			else if (opcode == 61455)
				le();
			else if (opcode == 61456)
				gt();
			else if (opcode == 61457)
				lt();
			else if (opcode == 61458)
				neg();
			else if (opcode == 61459)
				bnot();
			else if (opcode == 61460)
				not();
			else {
				memory.setProgramCounter(memory.getProgramCounter() - 1);
				int neg = (opcode - 61461) * -1;
				pushI(neg);
			}
			memory.setProgramCounter(memory.getProgramCounter() + 1);
		}
	}
		
	private void pushPc() {
		st.push(memory.getProgramCounter());
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void popPc() {
		memory.setProgramCounter(st.pop());
	}
		
	private void ld() {
		st.push(memory.userAccessMemory(st.peek()));
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void st() {
		int top = st.pop();
		int next = st.pop();
		memory.setMemory(next, top);
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void dup() {
		st.push(st.peek());
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void drop() {
		st.pop();
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void over() {
		int top = st.pop();
		int next = st.peek();
		st.push(top);
		st.push(next);
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void dNext() {
		int temp = st.pop();
		st.pop();
		st.push(temp);
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void swap() {
		int top = st.pop();
		int next = st.pop();
		st.push(top);
		st.push(next);
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void pushI(int n) {
		st.push(n);
		memory.setProgramCounter(memory.getProgramCounter() + 1); //move to next line
	}
		
	private void pushA(int address) {
		st.push(memory.userAccessMemory(((address))));
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void out() {
		System.out.println((st.pop()));
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void popA(int address) {
		memory.setMemory(address, st.pop());
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void jmpA(int n) {
		memory.setProgramCounter(n);
	}
		
	private void jzA(int n) {
		if (st.peek() == 0) {
			memory.setProgramCounter(n);
		} else {
			memory.setProgramCounter(memory.getProgramCounter() + 1);
		}
	}
		
	private void jnzA(int n) {
		if (st.peek() != 0) {
			memory.setProgramCounter(n);	
		} else {
			memory.setProgramCounter(memory.getProgramCounter() + 1);
		}
	}
	
	private void in() {
		st.push(in);
		memory.setProgramCounter(memory.getProgramCounter() + 1);
	}
		
	private void add() {
		int top = st.pop();
		int next = st.pop();
		st.push(next + top);
	}
		
	private void sub() {
		int top = st.pop();
		int next = st.pop();
		st.push(next - top);
	}
	
	private void mul() {
		int top = st.pop();
		int next = st.pop();
		st.push(next * top);
	}
		
	private void div() {
		int top = st.pop();
		int next = st.pop();
		st.push(next / top);
	}
		
	private void mod() {
		int top = st.pop();
		int next = st.pop();
		st.push(next % top);
	}
		
	private void shl() {
		int top = st.pop();
		int next = st.pop();
		int shifted = next << top;
		st.push(shifted);
	}
		
	private void shr() {
		int top = st.pop();
		int next = st.pop();
		st.push(next >> top);
	}
			
	private void bor() {
		int top = st.pop();
		int next = st.pop();
		st.push(next | top);
	}
		
	private void bxor() {
		int top = st.pop();
		int next = st.pop();
		st.push(next ^ top);
	}
		
	private void band() {
		int top = st.pop();
		int next = st.pop();
		st.push(next & top);
	}
		
	private void and() {
		int top = st.pop();
		int next = st.pop();
		st.push(top != 0 && next != 0 ? 1 : 0);
	}
		
    private void or() {
		int top = st.pop();
		int next = st.pop();
		st.push(top == 0 && next == 0 ? 0 : 1);
	}
		
    private void eq() {
		int top = st.pop();
		int next = st.pop();
		st.push(next == top ? 1 : 0);
	}
		
    private void ne() {
		int top = st.pop();
		int next = st.pop();
		st.push(next != top ? 1 : 0);
	}
		
	private void ge() {
		int top = st.pop();
		int next = st.pop();
		st.push(next >= top ? 1 : 0);
	}
		
	private void le() {
		int top = st.pop();
		int next = st.pop();
		st.push(next <= top ? 1 : 0);
	}
		
	private void gt() {
		int top = st.pop();
		int next = st.pop();
		st.push(next > top ? 1 : 0);
	}
		
	private void lt() {
		int top = st.pop();
		int next = st.pop();
		st.push(next < top ? 1 : 0);
	}
		
	private void neg() {
		int top = st.pop();
		st.push(-top);
	}
		
	private void bnot() {
		int top = st.pop();
		st.push(~top);
	}
		
	private void not() {
		int top = st.pop();
		st.push(top == 0 ? 1 : 0);
	}
}
	
class Memory {
	private int[] memory; //represents the memory as an array
	private int programCounter;
	private int i; //usable memory to load data after program is loaded in memory
		
	/* Given the name of the object file, load the instructions
	 * into memory. 
	 */
	public Memory(String fileName) {
		String opCode;
		memory = new int[4096];  //4k x 16-bit memory width
		i = 0;
		String sub;
		try {
			File f = new File(fileName);
			@SuppressWarnings("resource")
			Scanner input = new Scanner(f);
			while (input.hasNextLine()) {
				opCode = input.nextLine();
				if (!opCode.equals("v2.0 raw")) {
					if (opCode.charAt(0) == '1' ) { //pushi
						sub = opCode.substring(1);
						int leftMost = Integer.parseInt(sub, 16);
						int bit = (leftMost & 0xfff) >> 11;
						if (bit == 1) 
							sub = "F" + sub;
						else
							sub = "0" + sub;
						int result = Integer.valueOf(sub, 16).shortValue();
						if (result < 0) {
							result = result * -1;
							memory[i] = 61461 + result;
						} else 
							memory[i] = 4096 + result;
					} else {
						memory[i] = (Integer.decode("0x" + opCode));  //finds the decimal value of the hexadecimal opcode and adds to memory
					}
					i++;
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			System.exit(0);
		}
		programCounter = 0;  //initialize program counter to start of program
	}
	
	/*
	 * Sets location at memory address given to int n
	 */
	public void setMemory(int address, int n) {
		memory[address] = n;
	}
	
	public int getMemory(int address) {
		return memory[address];
	}
	/*
	 * returns the value at the location specified by the address
	 */
	public int getValueAtAddress(int address) {
		return memory[address];
	}
	
	public int userAccessMemory(int address) {
		int index = address + i;
		return memory[index];
	}
	
	public int getProgramCounter() {
		return programCounter;
	}
	
	public void setProgramCounter(int n) {
		programCounter = n;
	}
}