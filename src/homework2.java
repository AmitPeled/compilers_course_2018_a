import java.util.Scanner;
import java.util.Vector;
import java.util.Stack;
import java.util.LinkedList;


/*
* Hints in doing the HW:
*   a) Make sure you first understand what you are doing.
*   b) Watch Lecture 2 focusing on the code described
 */

class homework2 {

    // Abstract Syntax Tree
    static final class AST {
        public final String value;
        public final AST left; // can be null
        public final AST right; // can be null
        public AST father; //for switch. can be null for root
        
        public static int LAB = 0;
        public static Stack<Integer> loopLabStack = new Stack<Integer>();
        
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
        public void setFathers(AST father){
        	this.father = father;
        	if(this.left != null)
        		this.left.setFathers(this);
        	if(this.right != null)
        		this.right.setFathers(this);
        }
    }
    
    static final class sepAdjusted{
    	public static int sep;
        public static int curr_ep;
        private static boolean sepMode = false;
        
    	static void print(String s, int growth) {
    		if(sepMode) {
    			curr_ep += growth;
    			sep = curr_ep > sep ? curr_ep : sep; // sep is maximum stack growth
    		}
    		else {
    			System.out.println(s);
    		}
    	}
    	private static void calcSep(AST ast) {
    	
        	// lda/ldc -> sep++;
    		// mult/add/sub/and/or/ixa->sep--;
    		// sto->sep-=2; function call->sep+=correspondingFunc.sep
    		if(ast.right.left != null) { // scope node exists
    			calcNestedFuncs(ast.right.left.right);
    		}
    	    Variable funcvar = SymbolTable.funcById(ast.left.left.left.value);
    	    sep = 0;
    	    curr_ep = 0;
    	    sepMode = true;
    	    code(ast.right.right, funcvar.name);
    	    sepMode = false;
    	    ((VariableFunction)funcvar).sep = sep;
    	}
    	private static void calcNestedFuncs(AST funcList) {
    		if(funcList == null)
    			return;
    		calcNestedFuncs(funcList.left);
    		Variable funcvar = SymbolTable.funcById(funcList.right.left.left.left.value);
    		calcSep(funcList.right);
    	}
    }
    static class Variable{
        // Think! what does a Variable contain?
    	String name;
    	String type; 
    	int address; 
    	int offset; //instead of address, in "attribute mode"
    	int size;
    	boolean isAttri;
    	boolean isFunc = false;
    	boolean isByVar; //for parameters. parameters can be by Reference (var)
    	int nd; //nesting level of which the variable is defined
    	String nestingFunc; //the function that the variable is nested inside
    	public Variable(String name,String type,int addrOrOffset, int size, boolean isAttri,
    			boolean isByVar, int nd, String nestingFunc) {
    		this.name=name;
    		this.type=type;
    		if(!isAttri){
    			this.address = addrOrOffset;
    			this.offset = -1;
    		}
    		else{
    			this.address = -1;
    			this.offset = addrOrOffset;
    		}
    		
    		this.size=size;
    		this.isAttri = isAttri;
    		this.isByVar = isByVar;
    		this.nd = nd;
    		this.nestingFunc = nestingFunc;
    	}
    	public Variable(Variable other, String name, int addrOrOffset, boolean isAttri, boolean isByVar, 
    			int nd, String nestingFunc) {
    		this.name=name;
    		this.type=other.type;
    		if(!isAttri){
    			this.address = addrOrOffset;
    			this.offset = -1;
    		}
    		else{
    			this.address = -1;
    			this.offset = addrOrOffset;
    		}
    		if(!isByVar)
    			this.size = other.size; //by value
    		else
    			this.size = 1; //by reference
    		this.isAttri = isAttri;
    		this.isByVar = isByVar;
    		this.nd = nd;
    		this.nestingFunc = nestingFunc;
    	}
    	
    	public String getName() {return this.name;}
    	public String getType() {return this.type;}
    	public int getAdrr() {return this.address;}
    	public int getOffset() {return this.offset;}
    	public int getSize() {return this.size;}
    	public boolean getIsAttri() {return this.isAttri;}
    	
    }
    
    static class VariablePointer extends Variable{
    	String pointsTo; //points to which type?
    	
    	public VariablePointer(String name,String type,int addrOrOffset, int size, boolean isAttri,
    			boolean isByVar, int nd, String nestingFunc, String pointsTo){
    		super(name, type, addrOrOffset, size, isAttri, isByVar, nd, nestingFunc);
    		this.pointsTo = pointsTo;
    	}
    	public VariablePointer(VariablePointer other, String name, int addrOrOffset, boolean isAttri,
    			boolean isByVar, int nd, String nestingFunc) {
    		super(other, name, addrOrOffset, isAttri, isByVar, nd, nestingFunc);
    		this.pointsTo = other.pointsTo;
    	}
    	public String getPointsTo(){ return this.pointsTo; }
    }
    
    static class VariableArray extends Variable{
    	int g; //size of type of the array
    	String typeElement; //type of the elements of the array
    	int dim; //number of dimensions
    	int[] d_size; //size of each dimension
    	int subpart; // constant to each array

    	public VariableArray(String name,String type,int addrOrOffset, int size, boolean isAttri, 
    			boolean isByVar, int nd, String nestingFunc,
    			int g, int dim, int[] d_size, String typeElement, AST rangeList){
    		super(name, type, addrOrOffset, size, isAttri, isByVar, nd, nestingFunc);
    		this.g = g;
    		this.dim = dim;
    		this.d_size = d_size; //the array was created in inputHandling. no need for deep copying
    		this.typeElement = typeElement;
    		
    		this.subpart = calcSubpart(rangeList, dim - 1);
    	}
    	
    	public VariableArray(VariableArray other, String name, int addrOrOffset, boolean isAttri,
    			boolean isByVar, int nd, String nestingFunc) {
    		super(other, name, addrOrOffset, isAttri, isByVar, nd, nestingFunc);
    		this.g = other.g;
    		this.dim = other.dim;
    		this.d_size = new int[other.d_size.length];
    		for(int i=0; i<this.d_size.length; i++)
    			this.d_size[i] = other.d_size[i]; //deep copy
    		this.typeElement = other.typeElement;
    		this.subpart = other.subpart;
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
    
    static class VariableRecord extends Variable{
    	String[] attris;
    	public VariableRecord(String name,String type,int addrOrOffset, int size, boolean isAttri, 
    			boolean isByVar, int nd, String nestingFunc, String[] attris){
    		super(name, type, addrOrOffset, size, isAttri, isByVar, nd, nestingFunc);
    		this.attris = attris; //no need for deep copy
    	}
    	public VariableRecord(VariableRecord other, String name, int addrOrOffset, boolean isAttri,
    			boolean isByVar, int nd, String nestingFunc) {
    		super(other, name, addrOrOffset, isAttri, isByVar, nd, nestingFunc);
    		this.attris = other.attris; //no need for deep copy
    	}
    	public String[] getAttris(){ return this.attris; }
    }

    static class VariableFunction extends Variable{
    	//static link attribute is "nestingFunc" that all Variables has!
    	String functionType; // function/procedure/program
    	String[] paraArr; //our function's parameters' names
    	String ret_varName; //the return value 'type'. not necessary because it can only be primitive
    	int sep;
    	int sizePara;
    	
    	public VariableFunction(String name,String type,int addrOrOffset, int size, boolean isAttri,
    			boolean isByVar, int nd, String SL_varName, String functionType, String[] paraArr,
    			String ret_varName, int sep) {
    		
    		super(name, type, addrOrOffset, size, isAttri, isByVar, nd, SL_varName);
			this.isFunc = true;
    		this.functionType = functionType;
    		this.paraArr = paraArr; //low copy - used only here
    		this.ret_varName = ret_varName;
    		this.sep = sep;
    		this.sizePara = 0; //will be set after params were defined
    		
    	}
    	
    	public VariableFunction(VariableFunction other, String name, int addrOrOffset, boolean isAttri,
    			boolean isByVar, int nd, String SL_varName) {
    		super(other, name, addrOrOffset, isAttri, isByVar, nd, SL_varName);
    		this.isFunc = true;
    		this.functionType = other.functionType;
    		this.paraArr = other.paraArr; //I think it has to be low-copy
    		this.ret_varName = other.ret_varName;
    		this.sep = other.sep;
    		this.sizePara = other.sizePara;
    		
    	}
    	
    	public void setRemainingAttrs(LinkedList<String> paramsNames, int size) {
    		paraArr = new String[paramsNames.size()];
        	for(int i=0; i<paraArr.length; i++) {
        		paraArr[i] = paramsNames.get(i);
        	}
        	this.size = size;
        	Variable var;
    		for(int i = 0; i < paraArr.length; i++) {
    			var = SymbolTable.varById(paraArr[i], this.name);
    			sizePara += var.size;
    		}
    	}
    	
    	public String getFunctionType() {return this.functionType;}
    	public String[] getParaArr() {return this.paraArr;}
    	public String getRet_varName() {return this.ret_varName;}
    	public int getSep() {return this.sep;} 
    }
    
    public static final class SymbolTable{
        // Think! what does a SymbolTable contain?
    	static Vector<LinkedList<Variable>> hashTable;
    	static int ADR=5; //address counter
    	final static int hashSize = 100;
        public static SymbolTable generateSymbolTable(AST tree){
        	// TODO: create SymbolTable from AST
        	hashTable = new Vector<LinkedList<Variable>>();
            
        	//create #hashSize cells in hashTable.
        	//maybe should count number of declarations, and then create the symbolTable
        	hashTable = new Vector<LinkedList<Variable>>(SymbolTable.hashSize);
        	//set vector's size by adding #size cells
        	for(int i = 0; i < SymbolTable.hashSize; i++) {
        		hashTable.addElement(new LinkedList<Variable>()); 
        	}
            
            functionsList(tree, 0, null, true); //null - program has no SL
            
        	
        	
        	
            
            
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
        			if(t.get(j).type.equals("function")){
        				System.out.print("First parameter: ");
        				if(((VariableFunction)(t.get(j))).paraArr.length > 0)
        					System.out.println( ((VariableFunction)(t.get(j))).paraArr[0] );
        			}
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
        

        public static VariableFunction funcById(String funcName) {
        	// funcById doesn't need nesting function for searching, because we assumed no duplicate
        	// functions with the same name
        	// just like the old 'varById'
        	
        	LinkedList<Variable> t = hashTable.elementAt(hashFunction(funcName));
        	Variable var = null;
        	boolean found = false;
        	for(int i = 0; i < t.size(); i++) {
        		var = t.get(i);
        		if(var.name.equals(funcName) && var.type.equals("function")) {
        			found = true;
        			break;
        		}
        	}
        	if(found){
        		if(var instanceof VariableFunction)
        			return (VariableFunction)var;
        		else
        			System.out.println("ERROR: func variable is type 'function' but isn't instance"
        					+ " of VariableFunction");
        	}
        	return null;
        }
    
        
        public static Variable varById(String id, String initNestingFunc) {
        	//input: variable's name & nesting function
        	//output: variable's object
        	
        	String nestingFuncName = initNestingFunc;
        	Variable var, currNestingFunc = null;
        	/*
        	* varById is sometimes called from inputHandling, so initNestingFunc is not defined yet
        	* because local vars are defined before the func herself.
        	* for example:
        	* function g:
        	* c:int
        	* b[1..2] of c
        	* 
        	* so we call varById("c","g") and g is not defined.
        	* so currNestingFunc is null!
        	*/
        	
        	boolean first = true;
        	LinkedList<Variable> t = hashTable.elementAt(hashFunction(id)); //get entry's linkedList
        	while(currNestingFunc != null || first) {
        		for(int i = 0; i < t.size(); i++) {
        			var = t.get(i);
        			if(var.name.equals(id)) {
        				if(var.nestingFunc != null && var.nestingFunc.equals(nestingFuncName)) {
        					return var;
        				}
        			}
        		}
        		first = false;
        		//go look at the next nestingFunc
        		currNestingFunc = funcById(nestingFuncName);
        		nestingFuncName = currNestingFunc.nestingFunc; //get next SL
        	}
        	System.out.println("ERROR: varById, didn't find any variable");
        	return null; //didn't found
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
        
        private static void functionsList(AST functions, int nd, String SL_varName, boolean isProgram) {
        	if(functions == null)
        		return;
        	
        	if(!isProgram) //program doesn't have brothers
        		functionsList(functions.left, nd, SL_varName, false); //func-brothers are from down to up
        	
        	AST currFunc;
        	//if it's the first time we call functionsList. the functions = program node
        	//otherwise, as usual (currFunc: right son of functionsList)
        	if(!isProgram)
        		currFunc = functions.right;
        	else
        		currFunc = functions;
        	
        	AST idNparamaters = currFunc.left;
        	String currFuncName = idNparamaters.left.left.value;
        	String type = "function";
        	String functionType = currFunc.value; //"function" or "procedure" or "program"
        	boolean isVoid = functionType.equals("function")? false : true; //program & procedure is void
        	String ret_varName = isVoid? "void" : idNparamaters.right.right.value;

        	
        	
        	AST content = currFunc.right;
        	AST scope = null;
        	if (content != null) { //content can be null!
        		scope = content.left;
        	}
        	boolean noscope = false; //M MMM MM MM LLL L L L GG G G
        	if(scope == null) //no local-vars & nested funcs
        		noscope = true;
        	
        	//***create function variable himself, and add to hashTable
        	
        	//paramsNames will be set after the constructor (after they're defined)
        	//size will be set after the constructor (after they're defined)
        	//don't forget: address=0
        	int size = 0; //will be set after the creation of the parameters & locals
        	String[] paraArr = null; //will be set after the creation of the parameters
        	
        	VariableFunction currFuncVar = new VariableFunction(currFuncName, type, 0, size, false,
        			false, nd, SL_varName, functionType, paraArr, ret_varName, 0); //sep=0 for now
        	int hash_entrance = hashFunction(currFuncName);
            hashTable.elementAt(hash_entrance).addLast(currFuncVar);
        	
        	
        	
        	
        	SymbolTable.ADR = 5; //adr reset. adr is relative to the current function
        	//***create parameters (parametersList)
        	LinkedList<String> paramsNames = new LinkedList<String>();
        	
        	int paramsSize;
        	if(idNparamaters.right != null){
        		paramsSize = inputHandling(idNparamaters.right.left, false, nd + 1, currFuncName,
        				true, paramsNames);
        	}
        	else
        		paramsSize = 0;
        	
        	//***create local vars

        	int localVarsSize; //size's of function var will be size of local variables
        	if(noscope == false)
        		localVarsSize = inputHandling(scope.left, false, nd + 1, currFuncName, false, null);
        	else
        		localVarsSize = 0;
        	
        	
        	//***set size & paraArr
        	
        	size = localVarsSize + paramsSize;
        	
        	//paramsNames: from LinkedList to array of strings
        	currFuncVar.setRemainingAttrs(paramsNames, size);
        	
        	
        	
        	
        	//TODO: add nested functions to Symbol Table - recursive call!
            if(noscope == false) {
        		functionsList(scope.right, nd + 1, currFuncName, false); //create sons
        	}
        }
        
        private static void setFunctionsListSep(AST functions) {
        	//TODO: go through all functions in AST and set for each 'sep' attr
        	
        	
        }
        
        private static int inputHandling(AST declarations, boolean isAttri, int nd, String nestingFunc,
        		boolean isParam, LinkedList<String> paramsNames) {
        	//coded - reads the declaration and add the new variable to the hashTable
        	//recursively goes over all declarationsList
        	//returns size of all the variables before it (for records). to set the offset
        	if(declarations == null) return 0;
        	
        	int sumofSizesBefore = inputHandling(declarations.left, isAttri, nd, nestingFunc,
        			isParam, paramsNames);
        	
        	
        	int hash_entrance, size = 1, addrOrOffset;
            String id,type;
            Variable var = null;
            
            boolean isByVar = (declarations.right.value.equals("byReference")); //for parameters
            
            
            id = declarations.right.left.left.value;
            type = declarations.right.right.value;
            
            
            if(type.equals("array")) {
            	//TODO: prepare attributes for variableArray and call the constructor
            	/*
            	 * type of Elements can be as:
            	 * 1. when Elements are not primitives:
            	 * declarations->var->array->identifier->name
            	 * 
            	 * 2. when Elements are primitives:
            	 * declarations->var->array->name
            	 */
            	String typeElement;
            	if(declarations.right.right.right.value.equals("identifier"))
            		//1. non-primitive
            		typeElement = declarations.right.right.right.left.value;
            	else
            		//2. primitive
            		typeElement = declarations.right.right.right.value;
            	int g = typesize(typeElement, nestingFunc); //size of the array's elements.
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
            	if(!isAttri)
            		addrOrOffset  = ADR; //address
            	else
            		addrOrOffset = sumofSizesBefore; //offset
            	ADR += size;
            	
            	
            	//subpart attribute is calculated with rangeList
            	var = new VariableArray(id, type, addrOrOffset, size, isAttri, isByVar, nd, nestingFunc, 
            			g, dims_count, array_dims, typeElement, rangeList);
            	
            	if(isParam)
            		paramsNames.add(id);
            }
            else if(type.equals("record")){
            	//TODO
            	String[] attris;
            	AST ourDeclarations = (declarations.right.right).left; //type.left
            	int attris_num = 0;
            	while(ourDeclarations != null){
            		attris_num++;
            		ourDeclarations = ourDeclarations.left;
            	}
            	attris = new String[attris_num];
            	ourDeclarations = (declarations.right.right).left; //reset
            	int i=0;
            	while(ourDeclarations != null){
            		attris[i] = ourDeclarations.right.left.left.value;
            		i++;
            		ourDeclarations = ourDeclarations.left;
            	}
            	//attributes in the array are backwards (I don't think it matters)
            	/*
            	String tmp;
            	int j;
				for(i=0, j=attris.length; i <= j; i++){
            		
            	}*/ //will add this later :)
            	
            	//first set record's address. then, his attributes' address
            	if(!isAttri)
            		addrOrOffset = ADR; //address
            	else
            		addrOrOffset = sumofSizesBefore; //offset
            	
            	ourDeclarations = (declarations.right.right).left; //reset
            	//size of the record is the latest undefined attribute
            	size = inputHandling(ourDeclarations, true, nd, nestingFunc, isParam, paramsNames);
            	
            	//ADR += size;
            	//-- no need for "ADR+=" because the attributes does it for us
            	
            	var = new VariableRecord(id, type, addrOrOffset, size, isAttri, isByVar,
            			nd, nestingFunc, attris);
            	
            	//I don't think you can define record inside of parameters but why not
            	if(isParam) 
            		paramsNames.add(id);
            }
            else if(type.equals("pointer")){
            	//pointers are not primitives.
            	//we need to save additional attribute for which type they point to.
            	
            	
            	size = 1;
            	if(!isAttri)
            		addrOrOffset = ADR; //address
            	else
            		addrOrOffset = sumofSizesBefore; //offset
            	ADR += size;
            	/*
            	 * two possibilities for where the pointsTo type
            	 * 1. none-primitive
            	 * (type).left.left
            	 * 
            	 * 2. primitive
            	 * (type).left
            	 */

            	String pointsTo;
            	if((declarations.right.right).left.value.equals("identifier"))
            		//1. none-primitive
            		pointsTo = (declarations.right.right).left.left.value;
            	else
            		//2. primitive
            		pointsTo = (declarations.right.right).left.value;
            	
            	var = new VariablePointer(id, type, addrOrOffset, size, isAttri, isByVar, 
            			nd, nestingFunc, pointsTo);
            	if(isParam)
            		paramsNames.add(id);
            }
            else if(type.equals("identifier")) {
            	//TODO: defined variable/parameter of the form b:a
            	
            	//maybe just for params, but nahh
            	//for this kind of declaration:
            	//b: a
            	
            	//(type).left.value
            	String copiedVarName = (declarations.right.right).left.value;
            	if(!isAttri)
            		addrOrOffset = ADR; //address
            	else
            		addrOrOffset = sumofSizesBefore; //offset
            	
            	//using copy constructor:
            	Variable copiedVar = varById(copiedVarName, nestingFunc); //find the copying var
            	if(copiedVar instanceof VariablePointer) {
            		VariablePointer vp = (VariablePointer)copiedVar;
            		var = new VariablePointer(vp, id, addrOrOffset, isAttri, isByVar, nd, nestingFunc);
            	}
            	else if(copiedVar instanceof VariableArray) {
            		VariableArray va = (VariableArray)copiedVar;
            		var = new VariableArray(va, id, addrOrOffset, isAttri, isByVar, nd, nestingFunc);
            	}
            	else if(copiedVar instanceof VariableRecord) {
            		VariableRecord vr = (VariableRecord)copiedVar;
            		var = new VariableRecord(vr, id, addrOrOffset, isAttri, isByVar, nd, nestingFunc);
            	}
            	else if(copiedVar instanceof Variable) {
            		var = new Variable(copiedVar, id, addrOrOffset, isAttri, isByVar, nd, nestingFunc);
            	}
            	else {
            		System.out.println("Error: inputHandling: copiedVar is weird");
            	}
            	size = var.size;
            	ADR += size;
            	
            	if(isParam)
            		paramsNames.add(id);
            }
            else {
            	//primitives
            	
            	size = 1;
            	if(!isAttri)
            		addrOrOffset = ADR; //address
            	else
            		addrOrOffset = sumofSizesBefore; //offset
            	
            	ADR += size;
            	var = new Variable(id, type, addrOrOffset, size, isAttri, isByVar, nd, nestingFunc);
            	
            	if(isParam)
            		paramsNames.add(id);
            }
            hash_entrance = hashFunction(id);
            hashTable.elementAt(hash_entrance).addLast(var);
            
            return sumofSizesBefore + size; //used for records - next offset
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
    
    public static int typesize(String type, String nestingFunc) {
    	
    	if(type.equals("int") || type.equals("boolean") || type.equals("pointer") || type.equals("real")) {
    		return 1;
    	}
    	return SymbolTable.varById(type, nestingFunc).size;
    	
    }
    
	private static void array_case(AST indexList, int[] dim_size, int dim_num, int curr_dim_num ,int size_type, String nestingFunc) { // handles ldc+ixa for each index accessed

    	if(indexList == null) {
    		return;
    	}
    	array_case(indexList.left, dim_size, dim_num, curr_dim_num - 1, size_type, nestingFunc);

    	coder(indexList.right, nestingFunc); // prints the index
    	int N = 1;
    	for(int i = curr_dim_num + 1; i < dim_num; i++) {
    		N *= dim_size[i];
    	}
    	sepAdjusted.print("ixa " + N*size_type, (-1));

    	
    }
    private static void codei(AST indexList, VariableArray var, String nestingFunc) {
    	
    	int dim_num = var.dim;
    	int[] dim_size = var.d_size;
    	int subpart = var.subpart;
    	int size_type = typesize(var.typeElement, nestingFunc);
    	
    	array_case(indexList, dim_size, dim_num, dim_num - 1, size_type, nestingFunc);
    	
    	sepAdjusted.print("dec " + subpart, 0);
    	return;
    }
    
    // prints code for address and return the type of that address
    private static String codel(AST statements, String nestingFunc) {
    	if(statements.value.equals("identifier")) {
    		String id = statements.left.value;
    		Variable var = SymbolTable.varById(id, nestingFunc);
    		Variable currfunc = SymbolTable.funcById(nestingFunc);
    		if(var.isFunc) {
    			//sepAdjusted.print("ldc " + var.name , 0);
    			if(var.name.equals(currfunc.name)) {
    				sepAdjusted.print("lda 0 0", 1);
    			}
    			else {
    				sepAdjusted.print("ldc " + var.name, 1);
    				//add lda..
    			}
    		}
    		else if(!var.isAttri) { //regular variable
    			sepAdjusted.print("lda "+ (currfunc.nd - var.nd + 1)+' '+ var.address, 1);
    			if(var.isByVar) //if reference also add ind
    				sepAdjusted.print("ind", 0);
    		}
    		else
    			sepAdjusted.print("inc " + var.offset, 0);
    		return var.name;
    	}
    	if(statements.value.equals("call")){ // case of call inside call (f(g();) - the first call takes the return value of the second call as argument
    		code(statements,nestingFunc);
    	}
    	if(statements.value.equals("array")) {
    		String name = codel(statements.left, nestingFunc); // identifier of the array
    		Variable var = SymbolTable.varById(name, nestingFunc);
    		VariableArray varArr = (VariableArray)var;
    		
    		// sends the beginning of array index's list +  the variable corresponding to the array
    		codei(statements.right, varArr, nestingFunc);
    		
    		return varArr.getTypeElement();
    	}

    	if(statements.value.equals("pointer")) {
    		// operator^ (* in c++)
    		String name = codel(statements.left, nestingFunc);
    		sepAdjusted.print("ind", 0);
    		
    		Variable var = SymbolTable.varById(name, nestingFunc);
    		
    		return (((VariablePointer)var).pointsTo);
    	}
    	
    	if(statements.value.equals("record")) {
    		codel(statements.left, nestingFunc);
    		String name = codel(statements.right, nestingFunc);
    		
    		return name;
    	}
    	
    	
    	
    	
    	sepAdjusted.print("ERROR: codel couldn't find case for this case", 0);
    	return null; //java-compiler wanted default return
    }
     
    private static void coder(AST statements, String nestingFunc) {
    	if(statements.value.equals("identifier")){
    		codel(statements, nestingFunc);
    		sepAdjusted.print("ind", 0);
    	}
    	if(statements.value.equals("call")) { // call handle for functions (with return value)
    		callHandle(statements, nestingFunc);
    	}
    	if(statements.value.equals("array")){
    		codel(statements, nestingFunc);
    		sepAdjusted.print("ind", 0);
    	}
    	if(statements.value.equals("record")){
    		codel(statements, nestingFunc);
    		sepAdjusted.print("ind", 0);
    	}
    	if(statements.value.equals("pointer")){
    		codel(statements, nestingFunc);
    		sepAdjusted.print("ind", 0);
    	}
    	if(statements.value.equals("plus")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("add", (-1));
    	}
    	if(statements.value.equals("multiply")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		System.out.println("mul");
    	}
    	if(statements.value.equals("divide")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("div", (-1));
    	}
    	if(statements.value.equals("minus")){
    		coder(statements.left ,nestingFunc);
    		coder(statements.right ,nestingFunc);
    		sepAdjusted.print("sub", (-1));
    	}
    	if(statements.value.equals("negative")){
    		coder(statements.left, nestingFunc);
    		sepAdjusted.print("neg", 0);
    	}
    	if(statements.value.equals("constInt") || statements.value.equals("constReal")){
    		
    		sepAdjusted.print("ldc " + statements.left.value, 1);
    	}
    	
    	//option 1 - no constBool before boolean value? :'(
    	if(statements.value.equals("false")){
    		sepAdjusted.print("ldc 0", 1);
    	}
    	if(statements.value.equals("true")){
    		sepAdjusted.print("ldc 1", 1);
    	}
    	//option 2 - constBool before boolean value.
    	if(statements.value.equals("constBool")){
    		if(statements.left.value.equals("false"))
    			sepAdjusted.print("ldc 0", 1);
    		else
    			sepAdjusted.print("ldc 1", 1);
    	}
    	
    	if(statements.value.equals("and")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("and", (-1));
    	}
    	if(statements.value.equals("or")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("or", (-1));
    	}
    	if(statements.value.equals("not")){
    		coder(statements.left, nestingFunc);
    		sepAdjusted.print("not", 0);
    	}
    	
    	
    	if(statements.value.equals("notEquals")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("neq", (-1));
    	}
    	if(statements.value.equals("equals")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("equ", (-1));
    	}
    	if(statements.value.equals("greaterOrEquals")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("geq", (-1));
    	}
    	if(statements.value.equals("lessOrEquals")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("leq", (-1));
    	}
    	
    	if(statements.value.equals("greaterThan")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("grt", (-1));
    	}
    	if(statements.value.equals("lessThan")){
    		coder(statements.left, nestingFunc);
    		coder(statements.right, nestingFunc);
    		sepAdjusted.print("les", (-1));
    	}
    }
    
    private static void codec(AST caseList, int switch_end_label, String nestingFunc) {
    	int case_label = AST.LAB++;
    	sepAdjusted.print("L" + case_label + ":", 0);
    	if(caseList.right.right != null)
    		code(caseList.right.right, nestingFunc); //caseList->case->statementsList
    	sepAdjusted.print("ujp L" + switch_end_label, 0);
    	if(caseList.father.value.equals("caseList"))
    		codec(caseList.father, switch_end_label, nestingFunc); //call the next case
    	sepAdjusted.print("ujp L" + case_label, 0);
    }

	private static void handleArgs(AST argList, String nestingFunc, String toFunc, int parameterNum) { // basic args_handling
		//handles arguments pushing right before 'cup' (before going to the called function).
		if(argList == null)
			return;
		handleArgs(argList.left, nestingFunc, toFunc, parameterNum - 1);
		Variable func = SymbolTable.funcById(toFunc);
		Variable varParam = SymbolTable.varById(((VariableFunction)func).paraArr[parameterNum], func.name); // getting the current parameter of the function
		if(varParam.isByVar) {
			codel(argList.right, nestingFunc);
		}
		else {
			//non-var parameters. may push data > 1 (records & arrays)
			if(varParam.type.equals("array") || varParam.type.equals("record")) {
				codel(argList.right, nestingFunc);
				sepAdjusted.print("movs " + varParam.size, varParam.size); //maybe problem with sep
			}
			else
				coder(argList.right, nestingFunc);
		}
	}
	
	private static void callHandle(AST currStatement, String nestingFunc) {
		Variable ToFunc = SymbolTable.funcById(currStatement.left.left.value);
		Variable FromFunc = SymbolTable.funcById(nestingFunc);
		if(ToFunc.hasFuncPara) {
			//sepAdjusted.print("mstf "+...
		}
		else sepAdjusted.print("mst "+ (FromFunc.nd - ToFunc.nd + 1), 5);
		
		handleArgs(currStatement.right, nestingFunc, ToFunc.name, ((VariableFunction)ToFunc).paraArr.length - 1);
		if(((VariableFunction)ToFunc).hasFuncPara) {
			//sepAdjusted.print("cupi " +...
		}
		else sepAdjusted.print("cup "+ ((VariableFunction)ToFunc).sizePara + " " + ToFunc.name, 0);
	}
	
    private static void code(AST statements, String nestingFunc) { // nestingFunc is the func that contains the code
    	if(statements == null) return;
    		code(statements.left, nestingFunc); //code next statement (from down to up)
    	    		
    	AST currStatement = statements.right; //first operator of the statement
    	
    	if(currStatement.value.equals("call")) {
    		callHandle(currStatement, nestingFunc); // for procedures
    	}
    	
    	if(currStatement.value.equals("break")) {
    		int label = AST.loopLabStack.lastElement();
    		sepAdjusted.print("ujp L" + label, 0);
    	}
    	if(currStatement.value.equals("assignment")) {
    		codel(currStatement.left, nestingFunc);
			coder(currStatement.right, nestingFunc);
    		sepAdjusted.print("sto", (-2));
    	}
    	else if(currStatement.value.equals("print")) {
    		coder(currStatement.left, nestingFunc);
    		sepAdjusted.print("print", 0);
    	}
    	else if(currStatement.value.equals("if")) {
    		if(currStatement.right.value.equals("else")) {
    			//if-else code:
    			int else_label = AST.LAB++;
    			int end_if_label = AST.LAB++;
    			coder(currStatement.left, nestingFunc); //condition
    			sepAdjusted.print("fjp L" + else_label, 0);
    			code(currStatement.right.left, nestingFunc); //if code
    			sepAdjusted.print("ujp L" + end_if_label, 0); //end of if-code scope
    			sepAdjusted.print("L" + else_label + ":", 0);
    			code(currStatement.right.right, nestingFunc); //else code
    			sepAdjusted.print("L" + end_if_label + ":", 0);
    		}
    		else {
    			//if code:
    			int end_if_label = AST.LAB++;
    			
    			coder(currStatement.left, nestingFunc); //condition
    			sepAdjusted.print("fjp L" + end_if_label, 0);
    			code(currStatement.right, nestingFunc);
    			sepAdjusted.print("L" + end_if_label + ":", 0);
    			
    		}
    			
    	}
    	else if(currStatement.value.equals("while")){
    		int currLab = AST.LAB++;
    		int end_while_label = AST.LAB++;
    		AST.loopLabStack.add(end_while_label);
    		
    		sepAdjusted.print("L" + currLab + ":", 0);
    		coder(currStatement.left, nestingFunc);
    		sepAdjusted.print("fjp L" + end_while_label, 0);
    		code(currStatement.right, nestingFunc);
    		sepAdjusted.print("ujp L" + currLab, 0);
    		sepAdjusted.print("L" + end_while_label + ":", 0);
    		AST.loopLabStack.pop();
    	}
    	else if(currStatement.value.equals("switch")){
    		//we will do this like in the lectures
    		//(problem is that the tree is upside down.
    		//so we will go to the most deep case, and call recursively from there.
    		
    		//1. create the code which is before the cases
    		int end_switch_label = AST.LAB++;
    		coder(currStatement.left, nestingFunc); //expression
    		sepAdjusted.print("neg", 0);
    		sepAdjusted.print("ixj L"+end_switch_label, 0);
    		
    		//2. find the deepest case (the first)
    		//and call the recursive function from there.
    		AST caseList = currStatement.right;
    		if(caseList == null){
    			//no cases at all.
    		}
    		//find the deepest case
    		else while(caseList.left != null){
    			caseList = caseList.left;
    		}
    		codec(caseList, end_switch_label, nestingFunc);
    		
    		//3. just print the switch' label (codec also prints all the ujp at the end)
    		sepAdjusted.print("L" + end_switch_label + ":", 0);
    	}
	
    	//else, do nothing
    }	
    
    

    private static void generatePCode(AST ast, SymbolTable symbolTable) {
    	if(ast == null)
    		return;
    	
    	Variable funcvar = SymbolTable.funcById(ast.left.left.left.value);
    	System.out.println(funcvar.name + ':');
    	int ssp = funcvar.size + 5;
    	System.out.println("ssp " + ssp);
    	
    	System.out.println("sep " + ((VariableFunction)funcvar).sep);
    	System.out.println("ujp " + funcvar.name + "_begin");
    	if(ast.right.left != null) { // scope node exists
    		handleFuncList(ast.right.left.right, symbolTable);
    	}
   
    	System.out.println(funcvar.name + "_begin:");
    	AST firstStatement = ast.right.right;
    	code(firstStatement, funcvar.name);
    	if(((VariableFunction)funcvar).functionType.equals("procedure")) {
    		System.out.println("retp"); 
    	}
    	else if(((VariableFunction)funcvar).functionType.equals("function")) {
    		System.out.println("retf");
    	}
    	else System.out.println("stp");
    	
    }
    private static void handleFuncList(AST ast, SymbolTable symbolTable) {
    	if(ast == null)
    		return;
    	handleFuncList(ast.left, symbolTable);
    	generatePCode(ast.right, symbolTable);
    }
    
   
 
    
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AST ast = AST.createAST(scanner);
        ast.setFathers(null); //root has no father
        SymbolTable symbolTable = SymbolTable.generateSymbolTable(ast);
        
        System.out.println("DEBUG: after symbolTable");
        sepAdjusted.calcSep(ast);  /** insert inside generateSymbolTable  **/
        System.out.println("DEBUG: after calaSep");
        generatePCode(ast, symbolTable);
    }

}