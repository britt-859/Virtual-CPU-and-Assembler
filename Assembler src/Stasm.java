import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Stasm {
	public static void main(String[] args) {
		String fileName = "";
		String outputFile = "";
		boolean list = false;
		if (args.length >= 2) {
			fileName = args[0];
			outputFile = args[1];
			if (args.length >= 3 && args[2].equals("-l"))
				list = true;
			Map<String, String> instruction = new HashMap<>();  //key: mnemonic, value: opcode
			initializeInstructions(instruction);  //initialize the instructions with their proper hex values
			
			Map<String, String> symbolTable = new HashMap<>();  //key: symbol, value: Address in memory
			
			ArrayList<ArrayList<String>> program = new ArrayList<>();  //holds an ArrayList representing each line
			ArrayList<String> machineCode = new ArrayList<>();  //a list of the converted machine code. 
			
			ArrayList<String> assemblyCode = new ArrayList<>();
			
			processFile(fileName, program);  
			firstPass(program, symbolTable);
			secondPass(program, symbolTable, machineCode, instruction, assemblyCode, outputFile);
			System.out.println();
			if (list)
				listing(program, symbolTable, machineCode, assemblyCode);
		} else {
			System.out.println("USAGE: java Stasm.java <source file> <object file> [-l]");
			System.out.println("-l : print listing to standard error");			
		}
	}
	
	public static void listing(ArrayList<ArrayList<String>> program, Map<String, String> symbolTable,
								ArrayList<String> mCode, ArrayList<String> assemblyCode) {
		System.err.println("***  LABEL LIST  ***");
		for (String key : symbolTable.keySet()) {
			System.err.printf("%-8s %s\n", key, symbolTable.get(key));
		}
		System.err.println();
		System.err.println("***  MACHINE PROGRAM  ***");
		int i = 0; 
		int j = 0; //change depending on labels
		while (i < mCode.size()) {
			if (!mCode.get(i).contains(":")) {
				System.err.printf("%03d:%-12s %s\n", j, mCode.get(i), assemblyCode.get(i).replace(":", ": "));
				j++;
			} else
				if (mCode.get(i).charAt(mCode.get(i).length()-1) == ':') {
					System.err.printf("%17s%s \n", " ", mCode.get(i));
				} else {
					System.err.printf("%03d:%-12s %s\n", j, mCode.get(i).substring(mCode.get(i).indexOf(":")+ 1), assemblyCode.get(i).replace(":", ": "));
					j++;
				}
			i++;
		}
	}
	
	/*
	 * First pass through assembly code. Saves label address' 
	 * in symbol table.
	 */
	public static void firstPass(ArrayList<ArrayList<String>> program, Map<String, String> symbolTable) {
		int address = 0;  //used for saving the address in the symbol table
		for (int i = 0; i < program.size(); i++) {
			
			//if the last symbol in the first string in each line is : then it is a label
			if (program.get(i).get(0).charAt(program.get(i).get(0).length()-1) == ':') {
				String symbol = program.get(i).get(0).substring(0, program.get(i).get(0).length()-1);
				String h = Integer.toHexString(address);  //make sure address is converted to hex
				String ad = String.format("%3s", h).replace(' ', '0').toUpperCase();
				symbolTable.put(symbol, ad);
				
				//if the label is not on a line by itself and the next token is not a comment
				if ((program.get(i).size() > 1 && program.get(i).get(1).charAt(0) != ';' )) {
					address++;
				}
			}
			else {
				//the line is not a comment
				if (program.get(i).get(0).charAt(0) != ';') {
					address++;
				}   
			}
		}
	}
	
	/*
	 * Second pass through assembly code. Converts to
	 * Machine program. 
	 */
	public static void secondPass(ArrayList<ArrayList<String>> program, Map<String, String> symbolTable, ArrayList<String> mCode,
									Map<String, String> instruc, ArrayList<String> assembly, String outputFile) {
	    
		try {
			FileWriter fileWriter = new FileWriter(outputFile);
			String s;
			fileWriter.write("v2.0 raw\n");
			for (int i = 0; i < program.size(); i++) {
				s = decode(program.get(i), symbolTable, instruc, assembly);  //decode the line into the machine instruction
				if (s != "") {
					mCode.add(s);
					if (s.contains(":")) {
						String sub = s.substring(s.indexOf(":") + 1);
						if (!sub.equals("")) 
							fileWriter.write(sub + "\n");
					} else
						fileWriter.write(s + "\n");  //write to output file
				}
			}
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String decode(ArrayList<String> line, Map<String, String> symbolTable, Map<String, String> instruc, 
			ArrayList<String> assemblyCode) {
		String s = "";
		String assemble = "";
		int n = -1;  //determines where the first instruction mnemonic is in line
		
		//if the line is not a comment
		if (line.get(0).charAt(0) != ';') {
			
			//if the line does not begin with a label
			if (line.get(0).charAt(line.get(0).length()-1) != ':') {
				n = 0;
			} else {
				assemble = assemble + line.get(0);
				s = s + line.get(0);
				//if the line has instructions following a label
				if (line.size() > 1 && line.get(1).charAt(0) != ';')
					n = 1;
			}
		
			//if the line contains more than just comments and labels by themselves
			if (n >= 0) {
				if (line.get(n).equals("DW")) {
					assemble = assemble + line.get(n + 1);
					int v = Integer.parseInt(line.get(n + 1));
					String hex = String.format("%h", v);
					String sub;
					if (hex.length() > 4) {
						sub = hex.substring(4).toUpperCase();
					} else {
						sub = String.format("%4s", hex).replace(' ', '0').toUpperCase();
					}
					
					s = sub;
					assemblyCode.add(assemble);
					return s;
				} else {
					if (instruc.containsKey(line.get(n))) {
						s = s + instruc.get(line.get(n)); //instruction mnemonic
						assemble = assemble +  line.get(n);
					}
				}
				
	
				//operand
					if (line.size() > n + 1 && line.get(n + 1).charAt(0) != ';') {
						String op = "";
						//if operand is a label
						if (symbolTable.containsKey(line.get(n + 1))) {
							op = symbolTable.get(line.get(n + 1));
							assemble = assemble +  " " + line.get(n + 1);
						//operand is an immediate
						} else {
							String ad = line.get(n + 1);
							assemble = assemble +  " " + ad;
							int val = Integer.parseInt(line.get(n + 1));
							String h = String.format("%h", val);
							String substring;
							if (h.length() > 4) {
								substring = h.substring(5).toUpperCase();
							} else {
								substring = String.format("%3s", h).replace(' ', '0').toUpperCase();
							}
							op = substring;
						}
						s = s + op;
					}
					
				}
			}
		if (assemble != "")
			assemblyCode.add(assemble);
		return s;
	}
	
	@SuppressWarnings("resource")
	/*
	 * Processes the inputed file line by line. 
	 * Each line is split into tokens which are 
	 * added into an ArrayList. The arrayList 
	 * is then added to the program ArrayList 
	 */
	public static void processFile(String fileName, ArrayList<ArrayList<String>> program) {
		String line;
		String token;
		try {
			File f = new File(fileName);
			Scanner input = new Scanner(f);
			while (input.hasNextLine()) {
				ArrayList<String> tempLine = new ArrayList<>();
				line = input.nextLine();
				Scanner ln = new Scanner(line);
				while (ln.hasNext()) {
					token = ln.next();
					tempLine.add(token);
				}
				if (!tempLine.isEmpty())
					program.add(tempLine);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			System.exit(0);
		}
	}
	
	public static void initializeInstructions(Map<String, String> instruc) {
		instruc.put("NOP", "0000");
		instruc.put("HALT", "0F00");
		instruc.put("PUSHPC", "0100");
		instruc.put("POPPC", "0200");
		instruc.put("LD", "0300");
		instruc.put("ST", "0400");
		instruc.put("DUP", "0500");
		instruc.put("DROP", "0600");
		instruc.put("OVER", "0700");
		instruc.put("DNEXT", "0800");
		instruc.put("SWAP", "0900");
		instruc.put("PUSHI", "1");
		instruc.put("PUSH", "2");
		instruc.put("POP", "3");
		instruc.put("JMP", "4");
		instruc.put("JZ", "5");
		instruc.put("JNZ", "6");
		instruc.put("IN", "D000");
		instruc.put("OUT", "E000");
		instruc.put("ADD", "F000");
		instruc.put("SUB", "F001");
		instruc.put("MUL", "F002");
		instruc.put("DIV", "F003");
		instruc.put("MOD", "F004");
		instruc.put("SHL", "F005");
		instruc.put("SHR", "F006");
		instruc.put("BAND", "F007");
		instruc.put("BOR", "F008");
		instruc.put("BXOR", "F009");
		instruc.put("AND", "F00A");
		instruc.put("OR", "F00B");
		instruc.put("EQ", "F00C");
		instruc.put("NE", "F00D");
		instruc.put("GE", "F00E");
		instruc.put("LE", "F00F");
		instruc.put("GT", "F010");
		instruc.put("LT", "F011");
		instruc.put("NEG", "F012");
		instruc.put("BNOT", "F013");
		instruc.put("NOT", "F014");
	}
} 