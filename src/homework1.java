import java.util.Scanner;
import java.util.Vector;
import java.util.LinkedList;

/*
* Hints in doing the HW:
*   a) Make sure you first understand what you are doing.
*   b) Watch Lecture 2 focusing on the code described
 */
class homework1 {

    // Abstract Syntax Tree
    static final class AST {
        public final String value;
        public final AST left; // can be null
        public final AST right; // can be null
        
        public static int LAB = 0;
        
        private AST(String val,AST left, AST right) {
            value = val;
            this.left = left;
            this.right = right;
        }

        public static AST createAST(Scanner input) {
            if (!input.hasNext())
                return null;

            String value = input.nextLine();
            if (value.equals("~"))
                return null;

            return new AST(value, createAST(input), createAST(input));
        }
    }

    static class Variable{
        // Think! what does a Variable contain?
    	String name;
    	String type; 
    	int address;
    	int size;
    	public Variable(String name,String type,int address, int size) {
    		this.name=name;
    		this.type=type;
    		this.address = address;
    		this.size=size;
    		
    	}
    	public String getName() {return this.name;}
    	public String getType() {return this.type;}
    	public int getAdrr() {return this.address;}
    	public int getSize() {return this.size;}
    	
    	
    }
    
    static class VariableArray extends Variable{
    	int g; //size of type of the array
    	int dim; //number of dimensions
    	int[] d_size; //size of each dimension
    	
    	public VariableArray(String name,String type,int address, int size){
    		super(name, type, address, size);
    		
    	}
    	
    	
    }
    
    

    public static final class SymbolTable{
        // Think! what does a SymbolTable contain?
    	static Vector<LinkedList<Variable>> hashTable;
    	static int ADR=5; //address counter
    	
        public static SymbolTable generateSymbolTable(AST tree){
            // TODO: create SymbolTable from AST
            hashTable = new Vector<LinkedList<Variable>>();
            AST declarations = null;
            if(tree!=null && tree.right!=null &&tree.right.left!=null) {
        		declarations = tree.right.left.left; //first declaration
            }
        	int dec_num = 0;
        	while(declarations!=null) { // counts the number of the variables
        		declarations=declarations.left;
        		dec_num++;
        	}            
            hashTable = new Vector<LinkedList<Variable>>(dec_num);
            
            for(int i = 0; i < dec_num; i++) {
            	hashTable.addElement(new LinkedList<Variable>());
            }
            
            
            if(tree!=null && tree.right!=null &&tree.right.left!=null) {
        		declarations = tree.right.left.left;
            }
            inputHandling(declarations);
        	
            //printHashTable();
            /*
        	LinkedList<Variable> t;
        	for(int i = 0; i < hashTable.size(); i++) {
        		t = hashTable.elementAt(i);
        		
        		System.out.println("hashTable[" + i + "]:");
        		
        		for(int j = 0; j < t.size(); j++) {
        			System.out.print("name: " + t.get(j).name + ", ");
        			System.out.print("type: " + t.get(j).type + ", ");
        			System.out.print("address: " + t.get(j).address + ", ");
        			System.out.println("size: " + t.get(j).size);
        			
        		}
        	}
            */
            return null;
        }
        
        public static void printHashTable() {
            System.out.println("fs2");

        	LinkedList<Variable> t;
            System.out.println("fs2");

        	for(int i = 0; i < hashTable.size(); i++) {
        		t = hashTable.elementAt(i);
        		
        		System.out.println("hashTable[" + i + "]:");
        		
        		for(int j = 0; j < t.size(); j++) {
        			System.out.print("name: " + t.get(j).name + ", ");
        			System.out.print("type: " + t.get(j).type + ", ");
        			System.out.print("address: " + t.get(j).address + ", ");
        			System.out.println("size: " + t.get(j).size);
        			
        		}
        	}
        }
        
        public static Variable varById(String id) {
        	//input: variable's name
        	//output: variable's object
        	LinkedList<Variable> t = hashTable.elementAt(hashFunction(id));
        	for(int i = 0; i < t.size(); i++) {
        		if(t.get(i).name.equals(id)) {
        			return t.get(i);
        		}
        	}
        	return null;
        }
        
        private static int inputHandling(AST declarations) {
        	//coded - reads the declaration and add the new variable to the hashTable
        	//returns size of all the variables before it (for records)
        	if(declarations == null) return 0;
        	
        	inputHandling(declarations.left);
        	
        	
        	int hash_entrance, size = 1;
            String id,type;
            Variable var = null;
            
            
            id = declarations.right.left.left.value;
            type = declarations.right.right.value;
            
            if(type.equals("array")) {
            	//TODO
            }
            else if(type.equals("record")){
            	//TODO
            }
            else {
            	size = 1;
            	var = new Variable(id, type, ADR++, size);
            }
            hash_entrance = hashFunction(id);
            hashTable.elementAt(hash_entrance).addLast(var);
             	
            return size;
            
        	
        }
        
        private static int hashFunction(String identifier) {/* the function returns the sum of the ascii values
        									of the string's characters modulo by the num of the variables */
        	int value=0;
        	for(int i=0;i<identifier.length();i++) {
        		value+=identifier.charAt(i);
        	}
        	value = value % hashTable.size();
        	return value;
        }
      
    }
    
    
    private static void codel(AST statements) {
    	if(statements.value.equals("identifier"))
    		System.out.println("ldc " + SymbolTable.varById(statements.left.value).address);
    }
    
    private static void coder(AST statements) {
    	if(statements.value.equals("identifier")){
    		codel(statements);
    		System.out.println("ind");
    	}
    	if(statements.value.equals("plus")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("add");
    	}
    	if(statements.value.equals("multiply")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("mul");
    	}
    	if(statements.value.equals("divide")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("div");
    	}
    	if(statements.value.equals("minus")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("sub");
    	}
    	if(statements.value.equals("negative")){
    		coder(statements.left);
    		System.out.println("neg");
    	}
    	if(statements.value.equals("constInt") || statements.value.equals("constReal")){
    		
    		System.out.println("ldc " + statements.left.value);
    	}
    	
    	//option 1 - no constBool before boolean value? :'(
    	if(statements.value.equals("false")){
        	System.out.println("ldc 0");
    	}
    	if(statements.value.equals("true")){
        	System.out.println("ldc 1");
    	}
    	//option 2 - constBool before boolean value.
    	if(statements.value.equals("constBool")){
    		if(statements.left.value.equals("false"))
    			System.out.println("ldc 0");
    		else
    			System.out.println("ldc 1");
    	}
    	
    	if(statements.value.equals("and")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("and");
    	}
    	if(statements.value.equals("or")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("or");
    	}
    	if(statements.value.equals("not")){
    		coder(statements.left);
    		System.out.println("not");
    	}
    	
    	
    	if(statements.value.equals("notEquals")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("neq");
    	}
    	if(statements.value.equals("equals")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("equ");
    	}
    	if(statements.value.equals("greaterOrEquals")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("geq");
    	}
    	if(statements.value.equals("lessOrEquals")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("leq");
    	}
    	
    	if(statements.value.equals("greaterThan")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("grt");
    	}
    	if(statements.value.equals("lessThan")){
    		coder(statements.left);
    		coder(statements.right);
    		System.out.println("les");
    	}
    }
    
    private static void code(AST statements) {
    	if(statements == null) return;
    	code(statements.left); //code next statement (from down to up)
    	
    	AST currStatement = statements.right; //first operator of the statement
    	
    	if(currStatement.value.equals("assignment")) {
    		codel(currStatement.left);
    		coder(currStatement.right);
    		System.out.println("sto");
    	}
    	else if(currStatement.value.equals("print")) {
    		coder(currStatement.left);
    		System.out.println("print");
    	}
    	else if(currStatement.value.equals("if")) {
    		if(currStatement.right.value.equals("else")) {
    			//if-else code:
    			int else_label = currStatement.LAB++;
    			int end_if_label = currStatement.LAB++;
    			coder(currStatement.left); //condition
    			System.out.println("fjp L" + else_label);
    			code(currStatement.right.left); //if code
    			System.out.println("ujp L" + end_if_label); //end of if-code scope
    			System.out.println("L" + else_label + ":");
    			code(currStatement.right.right); //else code
    			System.out.println("L" + end_if_label + ":");
    		}
    		else {
    			//if code:
    			int end_if_label = currStatement.LAB++;
    			
    			coder(currStatement.left); //condition
    			System.out.println("fjp L" + end_if_label);
    			code(currStatement.right);
    			System.out.println("L" + end_if_label + ":");
    			
    		}
    			
    	}
    	else if(currStatement.value.equals("while")){
    		int currLab = currStatement.LAB++;
    		int end_while_label = currStatement.LAB++;
    		System.out.println("L" + currLab + ":");
    		coder(currStatement.left);
    		System.out.println("fjp L" + end_while_label );
    		code(currStatement.right);
    		System.out.println("ujp L" + currLab);
    		System.out.println("L" + end_while_label + ":");
    	}
    		
    	//else, do nothing
    }
    
    
    private static void generatePCode(AST ast, SymbolTable symbolTable) {
    	if(ast == null)
    		return;
    	
    	AST firstStatement = ast.right.right;
    	code(firstStatement);
    	
    	
    	
    	
        // TODO: go over AST and print code
    }
 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AST ast = AST.createAST(scanner);
        SymbolTable symbolTable = SymbolTable.generateSymbolTable(ast);
        generatePCode(ast, symbolTable);
        //SymbolTable.printHashTable();
    }

}