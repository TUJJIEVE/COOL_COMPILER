package cool;
import java.util.ArrayList;
import java.util.List;

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
		NodeVisitor v = new NodeVisitor(errorFlag);
		
		pr.accept(v);
		errorFlag = v.getEflag();
	}

}
/*
	Interface for the Visitor design pattern 

*/
interface ASTvisitor {
	public boolean visit(ProgramNode P);
	public boolean visit(CNode C);
	public boolean visit(FeatureNode F);
	public boolean visit(ExpressionNode E);

}
// Concrete Visitor class which implements the methods of the interface
class NodeVisitor implements ASTvisitor {
	// Conatains the Scope table 
	public ScopeTable<AST.ASTNode> Table = new ScopeTable<AST.ASTNode>();
	// COntains inheritance graph
	public InheritanceGraph IG = new InheritanceGraph();
	public boolean errorFlag;
	public void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
	}
	public boolean getEflag(){
		return errorFlag;
	}
	NodeVisitor(boolean errorFlag){
		this.errorFlag=errorFlag;
	}
	@Override
	public boolean visit(ProgramNode P){  // Method if the visitor is visiting Program Node
		P.visitPcount++;
		ArrayList<ClassNode> listOfClasses = new ArrayList<ClassNode>();

		for (AST.class_ cl : P.prog.classes){   // For creating the inheritance graph
			listOfClasses.add(new ClassNode(cl));
		}

		IG.setGraph(listOfClasses); // Creating the graph from the classes present in the program 
		if (!IG.checkInheritanceGraph()) return false; // checking if the graph has cycles or not
		return true;
	}
	
	@Override
	public boolean visit(CNode C){  // Method if the visitor is visiting the Class Node
		if (++C.visitCcount < 2 ) {
			// Inserting Class scope in the SCope table and some inbuilt functions 
			Table.insertClass(C.c);
			List<AST.formal> f1 = new ArrayList<AST.formal>();
			Table.insert("abort",new AST.method("abort",f1,"Object",null,0));
			List<AST.formal> f2 = new ArrayList<AST.formal>();
			Table.insert("type_name",new AST.method("type_name",f2,"String",null,0));
			List<AST.formal> f3 = new ArrayList<AST.formal>();
			Table.insert("copy",new AST.method("copy",f3,"SELF_TYPE",null,0));
			List<AST.formal> f4 = new ArrayList<AST.formal>();
			f4.add(new AST.formal("x","String",0));
			Table.insert("out_string",new AST.method("out_string",f4,"SELF_TYPE",null,0));
			List<AST.formal> f5 = new ArrayList<AST.formal>();
			f5.add(new AST.formal("x","Int",0));
			Table.insert("out_int",new AST.method("out_int",f5,"SELF_TYPE",null,0 ));
			List<AST.formal> f6 = new ArrayList<AST.formal>();
			Table.insert("in_string",new AST.method("in_string",f6,"String",null,0));
			List<AST.formal> f7 = new ArrayList<AST.formal>();
			Table.insert("in_int",new AST.method("in_int",f7,"Int",null,0));
			List<AST.formal> f8 = new ArrayList<AST.formal>();
			
			Table.insert("length", new AST.method("length",f8,"Int",null,0));
			List<AST.formal> f9 = new ArrayList<AST.formal>();
			f9.add(new AST.formal("s","String",0));
			Table.insert("concat", new AST.method("concat",f9,"String",null,0));
			List<AST.formal> f10 = new ArrayList<AST.formal>();
			f10.add(new AST.formal("i","Int",0));
			f10.add(new AST.formal("j","Int",0));
			Table.insert("substr", new AST.method("substr",f10,"String",null,0));
		}

		return true;
	}
	
	@Override
	public boolean visit(FeatureNode F){ // Method if the visitor is visiting Festure node 
		if (F.feat instanceof AST.method){
			//System.out.println("This feature is a method:"+F.method.name);
			F.visitMcount++;
			// If flagged first time then in the second pass it checks once agains
			if (F.visitMcount<2 && !F.flag){
				
				if (F.method.typeid.equals("NULL")){
					reportError(Table.currentClass.filename,F.feat.lineNo,"The return type is NULL");
					//System.out.println("Return Type is NULL");
					return false;
				}
				// Loop to check if formals satisfy the condition
				for (AST.formal fo1 : F.method.formals){
					for (AST.formal fo2 : F.method.formals){
						if (fo1 !=fo2 && fo1.name.equals(fo2.name)){
							reportError(Table.currentClass.filename,F.feat.lineNo,"The formal parameters of method with Id:" + F.method.name + "have same ID's");
							//System.out.println("The formal parameters of the Function "+F.method.name+" have same ID's");
							return false;
						}
					}
				}
				F.flag=true;
				Table.insert(F.method.name,F.method);
				
			}
			// Visited in the second pass
			else if (F.flag && F.visitMcount==2){	
				//System.out.println("method checking  second time checking");
				//ClassNode currCl;
				if (Table.lookUpClassSpace(F.method.name)!=null && Table.lookUpClassSpace(F.method.name)!=F.method && Table.lookUpClassSpace(F.method.name) instanceof AST.method){
					reportError(Table.currentClass.filename,F.feat.lineNo,"The method with Id: " + F.method.name + " already exists");
					//System.out.println("Feature with this name id already exists");
					return false;
				}
				ClassNode currCl= new ClassNode();
				ClassNode par = new ClassNode();
				for (int i=0;i<IG.listOfClasses.size();i++){
					if (IG.listOfClasses.get(i).self.equals(Table.currentClass)){
						currCl = IG.listOfClasses.get(i);
						par =currCl.parent;
						break;
					}
				}

				// Checking the inheritance graph for overriden methods
				while (par!=null){

					Table.setCurrentClass(par.self);
					AST.feature n =(AST.feature) Table.lookUpClassSpace(F.method.name);
					
					if (!F.method.name.equals("main") && n!=null && n instanceof AST.method){
						// The inherited feature is redefined;
						if ( !F.method.typeid.equals( n.typeid )){
							reportError(Table.currentClass.filename,F.feat.lineNo,"The inherited feature "+F.method.name+" is redefined and not overriden");
							//System.out.println("*******method redefined");
							break;

						}
						else if ( F.method.formals.size() != ((AST.method)n).formals.size() ){
							reportError(Table.currentClass.filename,F.feat.lineNo,"The inherited feature "+F.method.name +" is redefined and not overriden");
							//System.out.println("*******method redefined");
							break;
						}
					}
					par=par.parent;
				}
				Table.setCurrentClass(currCl.self); 



			}
		
		}
		else if (F.feat instanceof AST.attr){
			// If feature is an attribute handle differently
			if (++F.visitAcount <2){
				if (F.attribute.typeid.equals("NULL")){
					reportError(Table.currentClass.filename,F.feat.lineNo,"The typeid is NULL");
					System.out.println("Typeid is NULL");
					return false;
				}
				//System.out.println("Type of attribute:"+F.attribute.typeid);
				if (!(F.attribute.value instanceof AST.no_expr) && !getType(F.attribute.value).equals(F.attribute.typeid)){
					reportError(Table.currentClass.filename,F.feat.lineNo,"The type of expression doesn't match");
					//System.out.println("Type of expression doesn't match typeid");
					return false;
				}
				F.flag=true;
				Table.insert(F.attribute.name,F.attribute);

			}
			else if (F.flag){
				// Check for two features with same name
				if (Table.lookUpClassSpace(F.attribute.name)!=null && Table.lookUpClassSpace(F.attribute.name)!=F.attribute && Table.lookUpClassSpace(F.attribute.name) instanceof AST.attr){
					reportError(Table.currentClass.filename,F.feat.lineNo,"The attribute with Id: " + F.attribute.name + "already exists");
					//System.out.println("Attribute with this nameId :" + F.attribute.name + " already exitst");
					return false;
				}
				ClassNode currCl=new ClassNode();
				for (int i=0;i<IG.listOfClasses.size();i++){
					if (IG.listOfClasses.get(i).self.equals(Table.currentClass)){
						currCl = IG.listOfClasses.get(i);
						break;
					}
				}
				// Check if the inherited attribute is redefined or not
				ClassNode par=currCl.parent;
				while (par!=null){
					Table.setCurrentClass(par.self);
					if (Table.lookUpClassSpace(F.attribute.name)!=null && Table.lookUpClassSpace(F.attribute.name)!=F.attribute && Table.lookUpClassSpace(F.attribute.name) instanceof AST.attr){
						// The inherited feature is redefined;
						reportError(Table.currentClass.filename,F.feat.lineNo,"The inherited attribute "+F.attribute.name +" is redefined");
						//System.out.println("************The inherited feature is redefined");
						break;
					}
					par=par.parent;
				}
				Table.setCurrentClass(currCl.self);   // reset the current class in symbol table


			}
		
		}




		return true;
	}


	@Override
	public boolean visit(ExpressionNode E){ // Function to visit the expression node

		E.visitEcount++;
		// Type checking happens in the second pass
		if (E .expr instanceof AST.no_expr){
			return true;
		}
		if (E.visitEcount >1) {
		
			if (E.visitEcount > 1) getType(E.expr);
		}

		return true;
	} 
	// Function use to get the join of types 
	List<String> getPathOf(String a){ //returns path it crossed till object
		List <String> pathOFa = new ArrayList<String>() ;
		//System.out.println("Entering getPathOf Function with type ::"+a);
		if(a.equals("Int")){
			//System.out.println("Matched with Int");
			pathOFa.add("Int");
			pathOFa.add("Object") ;
		}else if(a.equals("String")){
			//System.out.println("Matched with String");
			pathOFa.add("String");
			pathOFa.add("Object") ;
		}else if(a.equals("Bool")){
			//System.out.println("Matched with Bool");
			pathOFa.add("Bool");
			pathOFa.add("Object") ;
		}else {
			
			for (int i = 0; i < IG.listOfClasses.size(); i++) {
				if(IG.listOfClasses.get(i).self.name.equals(a)){
					ClassNode t = IG.listOfClasses.get(i).parent;
				
					pathOFa.add(a);
					while(t != null){
						pathOFa.add(t.self.name);
						t = t.parent;

					}
					break;
				};
			}
			
			pathOFa.add("Object") ;
		}
		
		
		
		//System.out.println("Path  of :" + a);
		//System.out.println("Ended");
		return pathOFa ;
	}
	// Function to get the join of types
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

	    return "object";
	}
	// Function to compute the join
	String join(List<String> a){
		if(a.size() == 0){  //I think its unnessary
			return "Object" ; //should give error
		}
		else if(a.size()==1){
			return a.get(0);
		}
		String res = a.get(0) ;
		for(int i = 1 ; i < a.size() ; i++){
			res =  join2(res,a.get(i));
		}
		return res ;

	}
	// Function to get the subtype of a class
	Boolean subTypeOfClass(String a,String b){ //a <= b

		List <String> pathOfa = getPathOf(a) ; // gives path

		for(int ii = 0 ; ii < pathOfa.size() ; ii++){
			//System.out.println("\t printing in subTypeOfClass : "+pathOfa.get(ii)+"\t"+b+"\n");
			if(pathOfa.get(ii).equals(b)){
				//System.out.println("\t printing in subTypeOfClass : found ");
				return true ;
			}
		}
		return false ;
	}
/*	getType function is used to get the type of the epxression as well as to do type checking 

*/
		public String getType(AST.expression E){
				if (!E.type.equals("_no_type")){
					return E.type;
				}
				else if (E instanceof AST.isvoid){
					getType( ((AST.isvoid)E).e1 );
					E.type="Bool";
					return E.type; 
				}
				else if (E instanceof AST.object){
					
					AST.object o = (AST.object) E;
					//AST.ASTNode node= Table.lookUpGlobal(o.name,AST.attr.class);
					AST.ASTNode node = Table.lookUpGlobal(o.name,AST.formal.class);
					if (node !=null){
						E.type = ((AST.formal)node).typeid;
						return E.type;  //return node.name"CHECK THIS"  i.e object class name
					}
					node= Table.lookUpGlobal(o.name,AST.attr.class);
					if (node !=null){
						E.type = ((AST.feature)node).typeid;
						return E.type;   //return node.name"CHECK THIS"  i.e object class name
					}
					node = Table.lookUpParent(o.name, AST.attr.class);
					if (node !=null){
						E.type = ((AST.feature)node).typeid;
						return E.type;
					}

                        reportError(Table.currentClass.filename,E.lineNo," Cant find object "+o.name );
                        //System.out.println("***Error:cant find object in class");
				
				}
				
				else if (E instanceof AST.plus){
					if (getType(((AST.plus)E).e1).equals("Int") && getType(((AST.plus)E).e2).equals("Int")){
						E.type = "Int";
						return "Int";
					}else{
						reportError(Table.currentClass.filename,E.lineNo,"Both operand types are not Int for addition (+) operation ");
						E.type = "Object";
						return E.type;
					}
				}
				
				else if (E instanceof AST.sub){
					if (getType(((AST.sub)E).e1).equals("Int") && getType(((AST.sub)E).e2).equals("Int")){
						E.type = "Int";
						return "Int";
					}else{
						reportError(Table.currentClass.filename,E.lineNo,"Both operand types are not Int for subtraction (-) operation ");
						E.type = "Object";
						return E.type;}
				}
				
				else if (E instanceof AST.mul){
					if (getType(((AST.mul)E).e1).equals("Int") && getType(((AST.mul)E).e2).equals("Int")){
						E.type = "Int";
						return "Int";
					}else{
						reportError(Table.currentClass.filename,E.lineNo,"Both operand types are not Int for multiplication (*) operation ");
						E.type = "Object";
						return E.type;
					}
				}
				
				else if (E instanceof AST.divide){
					if (getType(((AST.divide)E).e1).equals("Int") && getType(((AST.divide)E).e2).equals("Int")){
						E.type = "Int";
						return "Int";
					}else{
						reportError(Table.currentClass.filename,E.lineNo,"both operand types are not Int for division (/) operation ");
						E.type = "Object";
						return E.type;
					}
				}

				else if (E instanceof AST.comp){
					if(getType(((AST.comp)E).e1).equals("Int")){
						E.type = "Int" ;
						return "Int";
					}
					else{
						reportError(Table.currentClass.filename,E.lineNo,(((AST.comp)E).e1).type + " type is not Int for comparing (~) operation ");
						E.type = "Object";
						return E.type;
					}
				}

				else if (E instanceof AST.lt){
					if (getType(((AST.lt)E).e1).equals("Int") && getType(((AST.lt)E).e2).equals("Int") ){
						E.type = "Bool";
						return "Bool";
					}else{
						reportError(Table.currentClass.filename,E.lineNo,(((AST.lt)E).e1).type + " and " +(((AST.lt)E).e2).type+ " both types are not Int for lessthan (<) operation ");
						E.type = "Object";
						return E.type;				/* attach error to stderr
											take it as bool and continue checking semantics
											*/
					}
				}
				else if (E instanceof AST.leq){
					if (getType(((AST.leq)E).e1).equals("Int") && getType(((AST.leq)E).e2).equals("Int") ){
						E.type = "Bool";
						return "Bool" ;
					}else{
						reportError(Table.currentClass.filename,E.lineNo,E.getString(" ")+(((AST.leq)E).e1).type + " and " +(((AST.leq)E).e2).type+ " both types are not Int for lessthanequal to (<=) operation ");
						E.type = "Object";
						return E.type;				/* attach error to stderr
											take it as bool and continue checking semantics
											*/
					}
				}
				else if (E instanceof AST.eq){
					if(getType(((AST.eq)E).e1).equals("Int") || getType(((AST.eq)E).e2).equals("Int") || getType(((AST.eq)E).e1).equals("String") || getType(((AST.eq)E).e2).equals("String") || getType(((AST.eq)E).e1).equals("Bool") || getType(((AST.eq)E).e2).equals("Bool")){
						if(getType(((AST.eq)E).e1).equals(getType(((AST.eq)E).e2))){
							E.type = "Bool";
							return "Bool";
						}else{
							//error
							 reportError(Table.currentClass.filename,E.lineNo,(((AST.eq)E).e1).type + " and " +(((AST.eq)E).e2).type+ " both types are not same for eq (=) operation ");
					
							E.type = "Object";
							return "Object";
						}
					}
					System.out.println("\t AST.eq types are not Int,String,Bool\n");
					E.type = "Bool";
					return "Bool";

				}
				else if (E instanceof AST.neg){ 
					if(getType(((AST.neg)E).e1).equals("Bool")){
						return E.type;
					}else{
                        reportError(Table.currentClass.filename,E.lineNo,(((AST.neg)E).e1).type +" Type is not bool");
                        System.out.println("***Error:not bool in neg");
                    }
                    System.out.println("\tPASSED AST.neg with no type");

				}
				else if (E instanceof AST.new_){
					AST.new_ newExp = (AST.new_) E;
					if ((newExp.typeid.equals("Int") || newExp.typeid.equals("Bool") || newExp.typeid.equals("String") || newExp.typeid.equals("IO") || newExp.typeid.equals("Object"))){
						E.type = newExp.typeid;
						return E.type;
					}
					else {
						for (ClassNode cn : IG.listOfClasses){
							if (cn.self.name.equals(newExp.typeid)){
								E.type = newExp.typeid ;
								return E.type;
							}
						}
					}
					reportError(Table.currentClass.filename,E.lineNo, newExp.typeid +" new Did not have Valid Type or  Type may not have been defined");
					System.out.println("***Error:The Type class corresponding to new is not found");
					
				}

				else if(E instanceof AST.typcase){ //should test join function
					ArrayList <String> type_  = new ArrayList<String>();
					Boolean flag = true ;
					for(AST.branch b1 :((AST.typcase)E).branches){
						Table.enterScope(); // Inserting scope
						Table.insert(b1.name,new AST.feature(b1.type));
						if(subTypeOfClass( getType(((AST.typcase)E).predicate), b1.type )){ // s <= t
							flag = false ;
						}
						type_.add(getType(b1.value));
						Table.exitScope(); // removing scope
					}
					if(flag){
						System.out.println("Warning :case may not have mathcing expression");
					}
					E.type = join(type_);
					return E.type;
				}


				else if(E instanceof AST.let){ //type of let is type of body
					if(!(((AST.let)E).value instanceof AST.no_expr) && !getType(((AST.let)E).value).equals(((AST.let)E).typeid )){ /*|| let.value = empty*/ //type checking let x : typeid <- value {body}
						reportError(Table.currentClass.filename,E.lineNo,((AST.let)E).typeid+" doesn`t match with "+(((AST.let)E).value).type);
					}
					Table.enterScope(); //inserting scope
					Table.insert(((AST.let)E).name,new AST.feature(((AST.let)E).typeid));
					E.type= getType(((AST.let)E).body);
					Table.exitScope(); // removing scope
					return E.type ;
				
				}
				// Checking the expressions in the block and returning the type of block as the type of last expression
				else if(E instanceof AST.block){ 
					
					Table.enterScope();
					for (AST.expression e : ((AST.block)E).l1){
						getType(e);
					}
					Table.exitScope();
					E.type = getType(((AST.block)E).l1.get(((AST.block)E).l1.size() - 1)) ;
					return E.type;

				}
				// Function to check the loop body
				else if(E instanceof AST.loop){ 
				
					if(!getType(((AST.loop)E).predicate).equals("Bool")){
						//error : invalid loop variable
						reportError(Table.currentClass.filename,E.lineNo,(((AST.loop)E).predicate).type + " predicate type  doesnt match with Type Bool " ) ;
						//System.out.println("***Error:in AST.loop , predicate is not Bool");
					}
					getType( ((AST.loop)E).body); // check the type of the body
					E.type = "Object" ;
					//System.out.println("PASSED loop:returning "+E.type);
					return "Object" ;
				}
				
				else if(E instanceof AST.cond){
					//System.out.println("Checking coND");
					if( !getType(((AST.cond)E).predicate).equals("Bool") ){ //type checking
						//error : no bool in branch
						 reportError(Table.currentClass.filename,E.lineNo,(((AST.cond)E).predicate).type + " predicate type  doesnt match with Type Bool " ) ;
						//System.out.println("***Error:The predicate of if is not Bool");
					}
					ArrayList <String> type_ =new ArrayList<String>();
					type_.add(getType(((AST.cond)E).ifbody));
					type_.add(getType(((AST.cond)E).elsebody));
					//System.out.println("\tchecking if then else for corresponding types:: " + getType(((AST.cond)E).ifbody) + "  "+ getType(((AST.cond)E).elsebody));
					E.type = join(type_);
					//System.out.println("\tPassed : Type of if then else :: "+E.type);
					return 	E.type;
				}
				// Checking the dispatch isntructions both static as well as normal dispatch
				else if(E instanceof AST.dispatches ){ // caller.f(e1,e2,e3....)  
                       
                       //	System.out.println("Checking dispatches");
                       	AST.object o = new AST.object("ujjieve",0);
                       	if ( ((AST.dispatches)E).caller  instanceof AST.object ){
                       		o = (AST.object) (((AST.dispatches)E).caller);
                       		//System.out.println("\tcaller is instance of AST.object");
                       	}
                       	// if no caller is present
                       	if (o.name.equals("self")){ //CHECKS WHETHER CALLER IS PRESENT OR NOT
                   			  	//System.out.println("***The caller expression is empty");
	                           	AST.ASTNode node = Table.lookUpClassSpace( ((AST.dispatches)E).name) ;
	                           	boolean flag=false;

	                           	if ( node==null){
									node = Table.lookUpParent(((AST.dispatches)E).name, AST.method.class);
									if (node == null){
										reportError(Table.currentClass.filename,E.lineNo," method not found  in any of the inherited classes and in  "+ Table.currentClass.name);
										E.type = "Object";
										return E.type;

									}
	                           	}

	                       		AST.method m = (AST.method) node;
								AST.dispatches d = (AST.dispatches)E;
								if (checkParams(m,d)){
									//System.out.println("\tParams matched");
									E.type = m.typeid;
									return E.type;
								}
								
								reportError(Table.currentClass.filename,E.lineNo," parameters dont match with method  "+ m.name);
	                           	
								E.type ="Object";
								return E.type;                           	
                       
                        }
                        else{
                        	//System.out.println("caller checking");
           					switch (getType( ((AST.dispatches)E).caller)){
           						case "Int" : 
           									 //System.out.println("The caller type is Int");
           									 return "Object";
           						case "String":
           									 //System.out.println("The caller type is String");
												AST.ASTNode n = Table.lookUpClassSpace( ((AST.dispatches)E).name );
												if (n==null){
													reportError(Table.currentClass.filename,E.lineNo,"There is no method corresponding to IO class");
													}
												else {
													if (checkParams((AST.method)n, (AST.dispatches)E )) {
														E.type=((AST.method)n).typeid;
														return E.type;
														
													}
													reportError(Table.currentClass.filename,E.lineNo," parameters dont match with method  "+ ((AST.method)n).name);
									
													E.type = "Object";
													return E.type;
												}
												reportError(Table.currentClass.filename,E.lineNo," method not found  in IO class ");
												E.type = "Object";
												return E.type;	   
											
           						case "Bool" : 
           									// System.out.println("The caller type is String");
											   
											   return "Object";
           						case "IO"   :
           									//System.out.println("The caller type is IO");
           									AST.ASTNode n1 = Table.lookUpClassSpace( ((AST.dispatches)E).name );
           									if (n1==null){
           										reportError(Table.currentClass.filename,E.lineNo,"There is no method corresponding to IO class");
           										}
           									else {
           										if (checkParams((AST.method)n1, (AST.dispatches)E )) {
           											E.type=((AST.method)n1).typeid;
        	   										return E.type;
    	       										
           										}
           										reportError(Table.currentClass.filename,E.lineNo," parameters dont match with method  "+ ((AST.method)n1).name);
	                           	
           										E.type = "Object";
           										return E.type;
           									}
           									reportError(Table.currentClass.filename,E.lineNo," method not found  in IO class ");
           									E.type = "Object";
           									return E.type;
           						case "Object":
           									return "Object";
           					}
           					ClassNode searchCl = new ClassNode();
           					AST.class_ currCl = Table.currentClass;
           					for (int i=0;i<IG.listOfClasses.size();i++){
								//if (IG.listOfClasses.get(i).self.equals(Table.currentClass))
								if (IG.listOfClasses.get(i).self.name.equals(getType( ((AST.dispatches)E).caller))){
									searchCl = IG.listOfClasses.get(i);

									
								}

							}
							if (searchCl.self ==null){
								reportError(Table.currentClass.filename,E.lineNo," No Class of identifier "+ (((AST.dispatches)E).caller).type +"found ");
								//System.out.println("***Error:Class of such expression is not defined");
								E.type="Object";
								return "Object";
							}
							AST.dispatches d = ((AST.dispatches)E);

							if (E instanceof AST.static_dispatch){
								AST.class_ curr = Table.currentClass;
								Table.setCurrentClass(searchCl.self);
								AST.ASTNode n = Table.lookUpClassSpace(d.name);
								Table.setCurrentClass(curr);
								System.out.println(((AST.method)n).name +" " + d.name);
								if ( n != null && n instanceof AST.method){
									if (checkParams( (AST.method)n, d) ){
										Table.setCurrentClass(currCl);
										if (subTypeOfClass(getType(((AST.static_dispatch)E).caller), ((AST.static_dispatch)E).typeid) ){
											E.type =  ((AST.method)n).typeid;
											return E.type;
										}
										else{
											reportError(Table.currentClass.filename,E.lineNo,(((AST.static_dispatch)E).caller).type + " type  is not subtype  of " + ((AST.static_dispatch)E).typeid);
											//ystem.out.println("***Error:staic dispatch type not matched");
											E.type = "Object";
											return E.type;
										}		
										
										
									}

								}
										
								
								Table.setCurrentClass(currCl);
								reportError(Table.currentClass.filename,E.lineNo,"No such method found");
								E.type = "Object";
								return E.type;
							}

							
							ClassNode par = searchCl;
							while (par!=null){
									Table.setCurrentClass(par.self);
									AST.ASTNode n = Table.lookUpClassSpace(d.name);
									
									if (n!=null && n instanceof AST.method){
										if (checkParams( (AST.method)n, d) ){
											Table.setCurrentClass(currCl);
											E.type = ((AST.method)n).typeid;
											return E.type;
										}
									}
										
								
									par=par.parent;
							}

							Table.setCurrentClass(currCl); 

							reportError(Table.currentClass.filename,E.lineNo," No Method found  with name "+  ((AST.dispatches)E).name +" is present");
							//System.out.println("Method not found in the expression class");
							E.type = "Object";
							return E.type;

                            
                        }
				}
				else if (E instanceof AST.assign){
					
					//System.out.println("Checking assignment expr");
					AST.ASTNode node;
					AST.assign ag = (AST.assign) E;
					node =Table.lookUpGlobal(ag.name,AST.formal.class);
					if (node == null){
						node = Table.lookUpGlobal(ag.name,AST.attr.class);
						if (node == null){
							node = Table.lookUpParent(ag.name, AST.attr.class);
							if (node == null){
							reportError(Table.currentClass.filename,E.lineNo ," The id with name "+ag.name +" is not found");
							//System.out.println("***Error in assign :The id with name "+ag.name +" is not found");
							E.type = "Object";
							return E.type;
							}

						}
						if(subTypeOfClass(getType(ag.e1),((AST.feature)node).typeid)){
						
							/*System.out.println("passed : assgin operation have valid types in both sides");
							*/E.type = getType(ag.e1);
							return E.type;
						}
						else{
							//error
							 reportError(Table.currentClass.filename,E.lineNo, getType(ag.e1) + " cant be assigned to "+((AST.feature)node).typeid); 
							//System.out.println("***Error in assign : not valid types :E.type is assigned to node.typeid");
							E.type = "Object";
							return E.type;
						}
					}
					if(subTypeOfClass(getType(ag.e1),((AST.formal)node).typeid)){
						
						/*System.out.println("passed : assgin operation have valid types in both sides");
						*/E.type = getType(ag.e1);
						return E.type;
					}
					else{
						//error
						 reportError(Table.currentClass.filename,E.lineNo, getType(ag.e1) + " cant be assigned to "+((AST.feature)node).typeid); 
                        
					}
		
				}
			reportError(Table.currentClass.filename,E.lineNo,"Type checking failed");
			//System.out.println("failed");
			E.type = "Object";
			return "Object";
		}

		boolean checkParams(AST.method m ,AST.dispatches d){ // Fpr checking the parameters of dispatch instruction and method
			if (d.actuals == null || m.formals == null) return false;
			if (m.formals.size() != (d.actuals.size())) {
				System.out.println("The number of parameters passed doesn't match");
				return false;
			}
			for (int i=0;i<m.formals.size();i++){
				 if (! m.formals.get(i).typeid.equals(getType(d.actuals.get(i)))){
					System.out.println("The parameters type don't match with the signature");
					return false;
				}
			}
			return true;

		}

			
		
}

// Interface of the Nodes in the AST
interface ASTnode {
	public void accept(ASTvisitor visitor);
	public String typeInfo = "no_type";
}
// Expression Node which contains expression 
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
	@Override
	public void accept(ASTvisitor visitor){
		//System.out.println("ExpressionNode visiting");
		visitor.visit(this);

	}
}
// Feature Node which contains features both attributes and methods
class FeatureNode implements ASTnode{
	public AST.attr attribute;
	public AST.feature feat;
	public AST.method method;
	public boolean flag;
	public int visitMcount,visitAcount;
	public int visit;
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
	public void accept(ASTvisitor visitor){  // Accepting the visitor to visit the Feature Node
		visit++;
		visitor.visit(this);
	
		if (feat instanceof AST.method){
			((NodeVisitor)visitor).Table.enterScope();
		
			for (AST.formal fo : method.formals){
				((NodeVisitor)visitor).Table.insert(fo.name,fo);
			}
			((NodeVisitor)visitor).Table.enterScope();
			expMethod.accept(visitor);
			((NodeVisitor)visitor).Table.exitScope();

			if ( visit>1 &&  ! (((NodeVisitor)visitor).getType(method.body).equals(method.typeid)) ){
				System.out.println("The return type of body doesn't match with the specified type in method "+ method.name);
				((NodeVisitor)visitor).errorFlag=true;
			}
			((NodeVisitor)visitor).Table.exitScope();

		}
		else if (feat instanceof AST.attr){
			expAttr.accept(visitor);
		}

	}
}
// Class Node which contains the class in the program
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
	public void accept(ASTvisitor visitor){  // Accepting the visitor to visit the Class Node
		//System.out.println("Accepting class:"+c.name);
		visitor.visit(this);
		
		for (FeatureNode fe : featNodeList){ // Making the visitor to visit each feature nodes
			fe.accept(visitor);

		}
		
	}
	 
}

class ProgramNode implements ASTnode {
	// Program Node 
	AST.program prog;
	ArrayList<CNode> cNodeList;
	public int visitPcount;
	int numMainClass=0;
	boolean hasmainFunc=false;
	AST.class_ mainClass;
	boolean wrongInheritedClass=false;
	boolean flag=false;
	public ProgramNode(AST.program program){
		prog = program;
		cNodeList = new ArrayList<CNode>();
		visitPcount=0;
		for (AST.class_ cl : prog.classes){
			if (cl.parent.equals("Int") || cl.parent.equals("Bool") || cl.parent.equals("String") ){
				flag = true;
				wrongInheritedClass=true;
			}
			if (cl.name.equals("Main")){
				mainClass=cl;
				
				if (++numMainClass > 1) flag=true;
			}
			cNodeList.add(new CNode(cl));
		}
		if (mainClass!=null){
			for (AST.feature f : mainClass.features){
				if (f.name.equals("main")){
					hasmainFunc=true;
				}
			}
		}
		for (CNode c1 : cNodeList){
			for (CNode c2 : cNodeList){
				if (c1 != c2){
					if (c1.c.name.equals(c2.c.name)){
						flag = true;
					}
				}
			}
		}

	}

	@Override
	public void accept(ASTvisitor visitor){  // Accepting the visitor to visit the Program Node
		if (numMainClass ==0){
			System.out.println("There is no main class\nAborting...");
			((NodeVisitor)visitor).errorFlag=true;
			return;			
		}
		// If ther is no main function
		if (!hasmainFunc){
			System.out.println("There is no main function in main class\nAborting...");
			((NodeVisitor)visitor).errorFlag=true;
			return;
		}
		// if there is wrongly inherited class
		if (wrongInheritedClass){
			System.out.println("Class inherits from either Bool,Int,String\nAborting...");
			((NodeVisitor)visitor).errorFlag=true;
			return;
		}
		// if there is more than two classes
		if (flag) {
			System.out.println("There are 2 or more classes with same name\nAborting...");
			((NodeVisitor)visitor).errorFlag=true;
			return;
		}

		boolean cond = visitor.visit(this);  // Checking the inheritance graph
		if (!cond) {
			((NodeVisitor)visitor).errorFlag=true;
			return;
		}
		// First pass
		for (CNode cl : cNodeList){
			cl.accept(visitor);
		}
		// Second Pass
		for (CNode cl : cNodeList){
			((NodeVisitor)visitor).Table.setCurrentClass(cl.c);
			cl.accept(visitor);
		}


	}
	
};




