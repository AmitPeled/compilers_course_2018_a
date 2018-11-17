import java.util.Scanner;
import java.util.Vector;

import javax.lang.model.element.Element;

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
    
    static class VariablePointer extends Variable{
    	String pointsTo; //points to which type?
    	
    	public VariablePointer(String name,String type,int address, int size, String pointsTo){
    		super(name, type, address, size);
    		this.pointsTo = pointsTo;
    	}
    	
    	public String getPointsTo(){ return this.pointsTo; }
    }
    
    static class VariableArray extends Variable{
    	int g; //size of type of the array
    	String typeElement; //type of the elements of the array
    	int dim; //number of dimensions
    	int[] d_size; //size of each dimension
    	int subpart; // constant to each array

    	public VariableArray(String name,String type,int address, int size, int g, int dim, int[] d_size,
    			String typeElement, AST rangeList){
    		super(name, type, address, size);
    		this.g = g;
    		this.dim = dim;
    		this.d_size = d_size; //the array was created in inputHandling. no need for deep copying
    		this.typeElement = typeElement;
    		
    		this.subpart = calcSubpart(rangeList, dim - 1);
    		
    	}
    	
    	public int calcSubpart(AST rangeList, int i) {
    		if(rangeList == null)
    			return 0;
    		int prev = calcSubpart(rangeList.left, i-1);
    		
    		
    		int current = 0;
    		
    		/*
    		 * example: array A[3..5, 0..4, 0..2] of __ (typesize=g)
    		 * 
    		 * current(i=0) = leftRange * multi = 3 * (5*3*g)
    		 * current(i=1) = leftRange * multi = 0 * (3*g)
    		 * current(i=2) = leftRange * multi = 0 * (g)
    		 * 
    		 * subpart = current(i=0) + current(i=1) + current(i=2)
    		 */
    		
    		AST range = rangeList.right;
    		int leftRange = Integer.parseInt(range.left.left.value);
    		
    		//calc number of elements the left range represents
    		int multi = this.g;
    		for(int j=i+1; j<this.dim; j++) {
    			multi *= this.d_size[j];
    		}
    		current = leftRange * multi;
    		
    		return prev + current; //return the sum of all previous including himself
    	}
    	
    	public int getG(){ return this.g; }
    	public String getTypeElement(){ return this.typeElement; }
    	public int getDim(){ return this.dim; }
    	public int[] getD_size(){ return this.d_size; }
    	public int getSubpart(){ return this.subpart; }
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
        
        public static void array_dims(AST rangeList, int[] array_dims, int i) {
        	//sets a given array of size of dimensions with rangeLists
        	//goes from last to first 
        	if(rangeList == null)
        		return;
        	array_dims(rangeList.left, array_dims, i-1);
        	
        	AST range = rangeList.right;
        	int rangeRight = Integer.parseInt(range.right.left.value);
        	int rangeLeft = Integer.parseInt(range.left.left.value);
        	int rangeSize = rangeRight - rangeLeft + 1;
        	array_dims[i] = rangeSize;
        	
        	return;
        	
        }
        
        private static int inputHandling(AST declarations) {
        	//coded - reads the declaration and add the new variable to the hashTable
        	//returns size of all the variables before it (for records)
        	if(declarations == null) return 0;
        	
        	inputHandling(declarations.left);
        	
        	
        	int hash_entrance, size = 1, address;
            String id,type;
            Variable var = null;
            
            
            id = declarations.right.left.left.value;
            type = declarations.right.right.value;
            
            if(type.equals("array")) {
            	//TODO: prepare attributes for variableArray and call the constructor
            	
            	/*
            	 * type of Elements can be as:
            	 * 1. when Elements are not primitives:
            	 * declarations->var->array->identifeir->name
            	 * 
            	 * 2. when Elements are primitives:
            	 * declarations->var->array->name
            	 * 
            	 * 
            	 */
            	String typeElement;
            	if(declarations.right.right.right.value.equals("identifier"))
            		//1. non-primitve
            		typeElement = declarations.right.right.right.right.value;
            	else
            		//2. primtive
            		typeElement = declarations.right.right.right.value;

            	int g = typesize(typeElement); //size of the array's elements.
            	int dims_count = 0; //number of dimensions
            	int[] array_dims;
            	//count number of dimensions
            	AST rangeList = declarations.right.right.left; //first rangeList
            	while (rangeList != null){
            		dims_count++;
            		rangeList = rangeList.left;
            	}

            	
            	array_dims = new int[dims_count];
            	rangeList = declarations.right.right.left; //reset to first rangeList
            	array_dims(rangeList, array_dims, dims_count - 1); //from last to first

            	//count number of elements
            	//multiplication of all dims_sizes
            	size = 1;
            	for(int i=0; i<dims_count; i++){
            		size *= array_dims[i];
            	}
            	size *= g; //(number of elements * element's size)
            	address  = ADR;
            	ADR += size;
            	
            	
            	//subpart attribute is calculated with rangeList
            	var = new VariableArray(id, type, address, size, g, dims_count, array_dims,
            			typeElement, rangeList);
            }
            else if(type.equals("record")){
            	//TODO
            }
            else if(type.equals("pointer")){
            	//pointers are not primitives.
            	//we need to save additional attribute for which type they point to.
            	
            	
            	size = 1;
            	address = ADR++;
            	//type.left.left is the type of the identifier that the pointer points to.
            	String pointsTo = (declarations.right.right).left.left.value;
            	var = new VariablePointer(id, type, address, size, pointsTo);	
            }
            else {
            	//primitives
            	
            	
            	
            	size = 1;
            	address = ADR++;
            	var = new Variable(id, type, address, size);
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
    
    public static int typesize(String type) {
    	
    	if(type.equals("int") || type.equals("boolean") || type.equals("pointer") || type.equals("real")) {
    		return 1;
    	}
    	return SymbolTable.varById(type).size;
    	
    }
  
    private static void array_case(AST indexList, int[] dim_size, int dim_num, int curr_dim_num ,int size_type) { // handles ldc+ixa for each index accessed
    	if(indexList == null) {
    		return;
    	}
    	array_case(indexList.left, dim_size, dim_num, curr_dim_num - 1, size_type);

    	coder(indexList.right); // prints the index
    	int N = 1;
    	for(int i = curr_dim_num + 1; i < dim_num; i++) {
    		N *= dim_size[i];
    	}
    	System.out.println("ixa " + N*size_type);

    	
    }
    private static void codei(AST indexList, VariableArray var) {
    	
    	int dim_num = var.dim;
    	int[] dim_size = var.d_size;
    	int subpart = var.subpart;
    	int size_type = typesize(var.typeElement);
    	
    	array_case(indexList, dim_size, dim_num, dim_num - 1, size_type);
    	
    	System.out.println("dec " + subpart);
    	return;
    }
    
    // prints code for address and return the type of that address
    private static String codel(AST statements) {
    	if(statements.value.equals("identifier")) {
    		String id = statements.left.value;
    		Variable var = SymbolTable.varById(id);
    		
    		System.out.println("ldc " + var.address);
    		return var.name;
    	}
    	if(statements.value.equals("array")) {
    		String name = codel(statements.left); // identifier of the array
    		Variable var = SymbolTable.varById(name);
    		VariableArray varArr = (VariableArray)var;
    		
    		// sends the beginning of array index's list +  the variable corresponding to the array
    		codei(statements.right, varArr);
    		
    		return varArr.getTypeElement();
    	}

    	if(statements.value.equals("pointer")) {
    		// operator^ (* in c++)
    		String name = codel(statements.left);
    		System.out.println("ind");
    		
    		Variable var = SymbolTable.varById(name);
    		
    		return (((VariablePointer)var).pointsTo);
    	}
    	
    	System.out.println("ERROR: codel couldn't find case for this case");
    	return null; //java-compiler wanted default return
    }
    
    private static void coder(AST statements) {
    	if(statements.value.equals("identifier")){
    		codel(statements);
    		System.out.println("ind");
    	}
    	if(statements.value.equals("pointer")){
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