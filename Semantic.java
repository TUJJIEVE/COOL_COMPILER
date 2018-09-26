package cool;
import java.util.ArrayList;
public class Semantic{
	private boolean errorFlag = false;
	public void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
	}
	public boolean getErrorFlag(){
		return errorFlag;
	}

/*
	Don't change code above this line
*/

	public Semantic(AST.program program){
		//Write Semantic analyzer code here
		ProgramNode pr = new ProgramNode(program);
		Visitor v = new Visitor();
		pr.accept(v);


		
	}





}
// Implement the expression visitor for static dispatch , case , let 
// also for the second pass , Then see if data can be reused like the type 
// information, (can store type information of while new if etc.. in AST.expression class )
// Implement case condition in the expressionNode accept method
class ClassNode{
	public AST.class_ self;
	public ClassNode parent;
	public int isVisited;

	public ClassNode(AST.class_ c){
		this.self = c;
		this.parent = null;
		this.isVisited=0;
	}
}
class InheritanceGraph{
	ArrayList<ClassNode> listOfClasses;
	public void setGraph(ArrayList<ClassNode> listOfClasses){
		this.listOfClasses = listOfClasses;
	}
	// Function for checking if the inheritance graph is correct or not if not then return false else true
	public boolean checkInheritanceGraph(){

		for(int i=0;i<listOfClasses.size();i++){
			ClassNode temp = listOfClasses.get(i);
			if (temp.self.parent.equals("Object")) {
				temp.parent=null;
				continue;	
			}
			boolean cont = false;
			for (int j=0;j<listOfClasses.size();j++){
				if (temp.self.parent.equals(listOfClasses.get(j).self.name)){
					System.out.println(temp.self.name + " -> " + listOfClasses.get(j).self.name );
					temp.parent = listOfClasses.get(j);
					cont =true;
				}
			}
			if (!cont){
				System.out.println("No matching parentclass found for class " + temp.self.name);
				return false;
			}
		}	

		for (ClassNode cn : listOfClasses){
			boolean isCyclic = checkCycles(cn);
			if (isCyclic) {
				System.out.println("The inheritance graph contains cycles");
				return false;
			}
		}

		return true;

	}
// Method to check if the inheritance graph contains cycles returns true if contains else false
	public boolean checkCycles(ClassNode classNode){

		if (classNode.self.parent.equals("Object")){
			return false;
		}
		else if (classNode.isVisited == 1){
			return true;
		}
		else {
			classNode.isVisited=1;
			boolean check = checkCycles(classNode.parent);
			classNode.isVisited=0;
			return check;
		}
	}
}


interface ASTvisitor {
	public boolean visit(ProgramNode P);
	public boolean visit(CNode C);
	public boolean visit(FeatureNode F);
	public boolean visit(ExpressionNode E);

}

class Visitor implements ASTvisitor {

	public ScopeTable<AST.ASTNode> Table = new ScopeTable<AST.ASTNode>();
	public InheritanceGraph IG = new InheritanceGraph();	
	
	@Override
	public boolean visit(ProgramNode P){
		P.visitPcount++;
		ArrayList<ClassNode> listOfClasses = new ArrayList<ClassNode>();

		for (AST.class_ cl : P.prog.classes){   // For creating the inheritance graph
			listOfClasses.add(new ClassNode(cl));
		}

		IG.setGraph(listOfClasses);
		if (!IG.checkInheritanceGraph()) return false;
		return true;
	}
	
	@Override
	public boolean visit(CNode C){
		System.out.println("Visiting :" +C.c.name);
		C.visitCcount++;
		Table.insertClass(C.c);
		System.out.println("Visited :" +C.c.name);
		return true;
	}
	
	@Override
	public boolean visit(FeatureNode F){
		System.out.println("Visiting feature Node");
		if (F.feat instanceof AST.method){
			System.out.println("This feature is a method:"+F.method.name);
			F.visitMcount++;
			if (F.visitMcount<2){
				
				if (F.method.typeid.equals("NULL")){
					System.out.println("Return Type is NULL");
					return false;
				}
				// Change the below condition to take into account the overriden methods
				else if (Table.lookUpClassSpace(F.method.name)!=null){
					System.out.println("Feature with this name id already exists");
					return false;
				}

				for (AST.formal fo1 : F.method.formals){
					for (AST.formal fo2 : F.method.formals){
						if (fo1 !=fo2 && fo1.name.equals(fo2.name)){
							System.out.println("The formal parameters of the Function "+F.method.name+" have same ID's");
							return false;
						}
					}
				}
				for (AST.formal fo : F.method.formals){
					Table.insert(fo.name,fo);
				}
				Table.insert(F.method.name,F.method);
				
			}
			else{	
				// Check if no two features with same name
				// Check if the type of expression body is equal to the return type


			}
		
		}
		else if (F.feat instanceof AST.attr){

			F.visitAcount++;
			if (F.visitAcount <2){
				if (F.attribute.typeid.equals("NULL")){
					System.out.println("Typeid is NULL");
					return false;
				}
				// Change this to also take into account the inherited attr
				else if (Table.lookUpClassSpace(F.attribute.name)!=null){
					System.out.println("Attribute with this nameId :" + F.attribute.name + " already exitst");
					return false;
				}
				System.out.println("Type of attribute:"+F.attribute.typeid);
				if (!(F.attribute.value instanceof AST.no_expr) && !getType(F.attribute.value).equals(F.attribute.typeid)){
					System.out.println("Type of expression doesn't match typeid");
					return false;
				}
				Table.insert(F.attribute.name,F.attribute);

			}
			else {
				// Check for two features with same name


			}
		
		}




		return true;
	}


	@Override
	public boolean visit(ExpressionNode E){

		E.visitEcount++;
		
		if (E.expr instanceof AST.block){
			if (E.visitEcount<2) { 
				getType(E.expr);
				System.out.println("The type of block is:"+E.expr.type);
				return true;
			}
			else {
				// Set the type of the expression node as the type of the last expression in 
				// the expList. 
			}
	
		}
		else if (E.expr instanceof AST.no_expr){
			System.out.println("No expression type");
			return true;
		}
		else if (E.expr instanceof AST.assign){
			// ON visiting first time don't flag error if in the second time no such identifier is found then flag an error
			System.out.println("assign expression");

			AST.assign ag = (AST.assign) E.expr;
			AST.ASTNode node;
			if (!E.flag && E.visitEcount<2) {
				// Check if there is an attribute in the class name space if not then flag error;
				node =Table.lookUpClassSpace(ag.name);
				if (node == null || node instanceof AST.method){
					System.out.println("assign operation to an invalid Identifier with name "+ag.name);
					E.flag = true;
					return false;

				}
				else{
					AST.attr at = (AST.attr) node;
					if (getType(E.expr).equals(at.typeid)){
						Table.insert(ag.name,E.expr);
					}else {
						System.out.println("The type of assign expression doesn't match");
					}
				}


			}
			else if (E.flag){
				// Check the inheritance graph
			}
			// Else check the type of id and the expression if it matches then insert into the symbol table
			
			

		}

		else if (E.expr instanceof AST.dispatch){
			System.out.println("dispatch expression");
			
			AST.dispatch dp = (AST.dispatch) E.expr;
			AST.ASTNode node;
			if (!E.flag && E.visitEcount<2){
				node =Table.lookUpClassSpace(dp.name);
				if (node ==null || node instanceof AST.attr){
					System.out.println("Flagged");
					E.flag=true;
					return false;
				}

				else {
					AST.method m = (AST.method) node;
					if (m.formals.size() != dp.actuals.size()){
						System.out.println("The number of parameters passes doesn't match");
						return false;
					}
					for (int i=0;i<m.formals.size();i++){
						if (!m.formals.get(i).typeid.equals(getType(dp.actuals.get(i)))){
							System.out.println("The parameters type don't match with the signature");
							return false;
						}
					}
					// Check condition of caller

					
				}

			}
			else if (E.flag){
				// Check the inheritance graph;
			}
			
			
		}

		else if (E.expr instanceof AST.static_dispatch){
			System.out.println("static d expression");

		}

		else if (E.expr instanceof AST.cond){
			System.out.println("if expression");
			getType(E.expr);
			return true;
		}
		else if (E.expr instanceof AST.loop){
			System.out.println("loop expression");
			getType(E.expr);
			return true;
		}
		else if (E.expr instanceof AST.let){
			System.out.println("let expression");

		}

		else if (E.expr instanceof AST.typcase){
			System.out.println("case expression");

		}
		else if (E.expr instanceof AST.new_){
			System.out.println("New expression");
			AST.new_ newExp = (AST.new_) E.expr;
			if (!E.flag && E.visitEcount<2){
			
				if ((newExp.typeid.equals("Int") || newExp.typeid.equals("Bool") || newExp.typeid.equals("String"))){
					return true;

				}
				else{
					System.out.println("Flagged");
					E.flag=true;
					return false;
				}

			}
			else if (E.flag){
				// Check in the inheritance graph

			}
		}
		else {
			// Check the type of the expressions. and get the type using 
			 // getType method
			System.out.println("expression");

			getType(E.expr);
		}

		return true;
	}

	public String getType(AST.expression E){
			System.out.println("Type checking");
			if (!E.type.equals("_no_type")){
				return E.type;
			}

			else if (E instanceof AST.object){
				AST.object o = (AST.object) E;
				AST.ASTNode node= Table.lookUpGlobal(o.name);
				if (node != null) ;//return node.typeid;
				return "hello";
			}
			else if (E instanceof AST.plus){
				if (getType(((AST.plus)E).e1).equals("Int") && getType(((AST.plus)E).e2).equals("Int")){
					E.type = "Int";
					return "Int";
				}else{

				}
			}
			else if (E instanceof AST.sub){
	
				if (getType(((AST.sub)E).e1).equals("Int") && getType(((AST.sub)E).e2).equals("Int")){
					E.type = "Int";
					return "Int";
				}else{

				}
			}
			else if (E instanceof AST.mul){
				if (getType(((AST.mul)E).e1).equals("Int") && getType(((AST.mul)E).e2).equals("Int")){
					E.type = "Int";
					return "Int";
				}else{

				}
			}
			else if (E instanceof AST.divide){

				if (getType(((AST.divide)E).e1).equals("Int") && getType(((AST.divide)E).e2).equals("Int")){
					E.type = "Int";
					return "Int";
				}else{

				}
			}

			else if (E instanceof AST.comp){
				if(getType(((AST.comp)E).e1).equals("Int")){
					E.type = "Int" ;
					return "Int";
				}
				else{

				}
	/*			if (E.type.equals("_no_type") && getType())
	*/		}

			else if (E instanceof AST.lt){
				if (getType(((AST.lt)E).e1).equals("Int") && getType(((AST.lt)E).e2).equals("Int") ){
					E.type = "Bool";
					return "Bool";
				}else{
	                /* attach error to stderr 
	                  take it as bool and continue checking semantics
	                  */
				}
			}
			else if (E instanceof AST.leq){
			
				if (getType(((AST.leq)E).e1).equals("Int") && getType(((AST.leq)E).e2).equals("Int") ){
					E.type = "Bool";
					return "Bool" ;
				}else{
	                /* attach error to stderr 
	                  take it as bool and continue checking semantics
	                  */
				}
			}
			else if (E instanceof AST.eq){

				if(getType(((AST.eq)E).e1).equals("Int") || getType(((AST.eq)E).e2).equals("Int") || getType(((AST.eq)E).e1).equals("String") || getType(((AST.eq)E).e2).equals("String") || getType(((AST.eq)E).e1).equals("Bool") || getType(((AST.eq)E).e2).equals("Bool")){
					if(getType(((AST.eq)E).e1).equals(getType(((AST.eq)E).e2))){
						// E.type = "bool" ;
						// return "bool";
					}else{
						//error
					}
				}
				E.type = "Bool";
				return "Bool";
			}
			else if (E instanceof AST.neg){ //debug //check this
				if(getType(((AST.neg)E).e1).equals("Bool")){
					return E.type;
				}
				
			}
			else if (E instanceof AST.new_){
				return E.type ;
			}
			else if(E instanceof AST.typcase){ //should write join function
				AST.typcase typcase_ = (AST.typcase)E ;
				ArrayList <AST.branch> branches_ = typcase_.branches ;
				AST.expression e1 = typcase_.predicate ;
				String type1  = getType(e1) ;
				ArrayList <String> type_ ;
				bool flag = true ;
				for(AST.branch b1 : branches_){
					AST.expression e = b1.value ;
					String s = getType(e) ;
					if(subtype(s,t)){ // s <= t 
						flag = false ;
					}
					type_.add(getType(e));
				}
				if(flag){
					//warning : case may not match to any thing
				}
				type_ = join(type_);
				E.type = type_ ;
				return type_;
			}
			else if(E instanceof AST.let){
				// AST.let let_ = (AST.let)E ;
				// AST.expression = e2 = let_.value ; //is it possible that there is no value
				// AST.expression e1 = let_.body ;
				String valuetype = getType((AST.let)E.value);
				String type_ = getType((AST.let)E.body);

				if(type_ != (AST.let)E.type){
					//error
					System.out.println("The type doesn't match");
				}
				return E.type ;
			}
			else if(E instanceof AST.block){ //dont know whether to check all expr
			
				int index = (AST.block)E.l1.size() - 1; 
				E.type = getType((AST.block)E.l1.get(index)) ;
				return E.type;
			
			}
			else if(E instanceof AST.loop){ // dont know whether to check loop body 
				if(!getType((AST.loop)E.predicate).equals("Bool")){
					//error : invalid loop variable
				}
				E.type = "Object" ;
				return "Object" ;
			}
			else if(E instanceof AST.cond){
				
				if(getType((AST.cond)E.predicate) != "Bool" ){
					//error : no bool in branch
					System.out.println("The predicate of if is not Bool");
				}
				List <String> type_ ;
				type_.add(getType((AST.cond)E.ifbody));
				type_.add(getType((AST.cond)E.elsebody));
				type_ = join(type_);
				E.type = type_ ;
				return type_;
			}
/*			else if(E instanceof AST.){ //<expr>.<id>(<expr>,...,<expr>) , <id>(<expr>,...,<expr>) =>DISPATCH CLASS

			}
			else if (E instanceof AST.){ //STATIC DISPATCH

			}
*/			else if (E instanceof AST.assign){
				AST.assign assign = (AST.assign) E;
				AST.expression e1 = compl.e1 ;
			    String type_ = getType(e1) ;
				E.type = type_ ;
				return type_;
			}

		return "Object";
	}
			
		
}

/*boolean subtype(a,b){
	List <String> pathOfa = getPathOf(a) ;
	for(int i = 0 ; i < pathOfa.size() ; i++){
		if(pathOfa.get(i).equals(b)){
			return true ;
		}
	}
	return false ; 
}
String join(List<string> a){ 
	if(a.size().equals(0)){
		return "Object" ; //should give error
	}
	else if(a.size().equals(1)){
		return a.get(0);
	}
	String res = a.get(0) ;
	for(int i = 1 ; i < a.size() ; i++){
		res =  join2(res,a.get(i));
	}
	return res ;

}
String join2(String a , String b){
	List <String> pathOfa = getPathOf(a) ;
	List <String> pathOfb = getPathOf(b);
	for(int i = 0 ; i < pathOfa.size() ; i++ ){
		for(int j = 0 ; j < pathOfb.size() ; j++){
			if(pathOfa.get(i).equals(pathOfb.get(j))){
				return pathOfa.get(i) ;
			}
		}
	}

}

List<String> getPathOf(String a){ //returns path it crossed till object
	List <String> path ;
	
	for (int i = 0; i < IG.listOfClasses.size(); i++) {
		if(IG.listOfClasses.get(i).name.equals(a)){
			ClassNode t = IG.listOfClasses.get(i).parent;
			
			path.add(a);
			while(t != null){
				path.add(t.name);
				t = t.parent;
				
			}
			break;
		};
	}
	path.add("Object") ;
	return path ;
}*/

interface ASTnode {
	public void accept(ASTvisitor visitor);
	public String typeInfo = "no_type";
}

class ExpressionNode implements ASTnode{

	AST.expression expr;
	AST.block blockexpr;
	ArrayList<ExpressionNode> expList;
	String typeInfo;
	boolean flag;
	int visitEcount;

	public ExpressionNode(AST.expression a){
		expr = a;
		visitEcount=0;
		if (a instanceof AST.block){
			blockexpr=(AST.block) a;
			expList = new ArrayList<ExpressionNode>();
			for (AST.expression e : blockexpr.l1){
				expList.add(new ExpressionNode(e));
			}
		}
	}
	// Problem when there is block inside block
	@Override
	public void accept(ASTvisitor visitor){
		System.out.println("ExpressionNode visiting");
		visitor.visit(this);
		if (expr instanceof AST.block){
			System.out.println("This is Block node");
			((Visitor)visitor).Table.enterScope();
			for (ExpressionNode e : expList){
				e.accept(visitor);
			}
			((Visitor)visitor).Table.exitScope();
		}
		// Have to search for case statement
		else {

		}
	}
}



class FeatureNode implements ASTnode{
	public AST.attr attribute;
	public AST.feature feat;
	public AST.method method;
	public int visitMcount,visitAcount;
	//int acceptMcount=0,acceptAcount=0;
	ExpressionNode expMethod,expAttr;
	public FeatureNode (AST.feature fe){
		feat=fe;
		if (fe instanceof AST.method){
			method = (AST.method) fe;
			expMethod = new ExpressionNode(method.body);
			visitMcount=0;
		}else{
			attribute = (AST.attr)fe;
			expAttr = new ExpressionNode(attribute.value);
			visitAcount=0;
		}
	}

	@Override
	public void accept(ASTvisitor visitor){

		visitor.visit(this);
	
		if (feat instanceof AST.method){
			((Visitor)visitor).Table.enterScope();
			expMethod.accept(visitor);
			((Visitor)visitor).Table.exitScope();

		}
		else if (feat instanceof AST.attr){
			expAttr.accept(visitor);
		}

	}
}

class CNode implements ASTnode{

	AST.class_ c;
	ArrayList<FeatureNode> featNodeList;
	public int visitCcount;
	public CNode(AST.class_ cl){
		c = cl;
		visitCcount=0;
		featNodeList = new ArrayList<FeatureNode>();
		for (AST.feature fe : c.features){
			featNodeList.add(new FeatureNode(fe));
		}

	}

	@Override
	public void accept(ASTvisitor visitor){
		System.out.println("Accepting class:"+c.name);
		visitor.visit(this);
		for (FeatureNode fe : featNodeList){
			fe.accept(visitor);

		}
		
	}
	 
}

class ProgramNode implements ASTnode {
	
	AST.program prog;
	ArrayList<CNode> cNodeList;
	public int visitPcount;
	int numMainClass=0;
	boolean flag=false;
	public ProgramNode(AST.program program){
		prog = program;
		cNodeList = new ArrayList<CNode>();
		visitPcount=0;
		for (AST.class_ cl : prog.classes){
			if (cl.name.equals("Main"))
				if (++numMainClass > 1) flag=true;

			cNodeList.add(new CNode(cl));
		}


	}

	@Override
	public void accept(ASTvisitor visitor){
		if (flag) {
			System.out.println("There are more than 2 Main class");
			return;
		}
		boolean cond = visitor.visit(this);  // Checking the inheritance graph
		if (!cond) return;
		
		for (CNode cl : cNodeList){
			cl.accept(visitor);
			System.out.println("printing ------");
			((Visitor)visitor).Table.printTable(cl.c);
			System.out.println("printed -------");
		}


	}
	
};




