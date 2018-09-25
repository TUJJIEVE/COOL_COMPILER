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
		C.visitCcount++;
		Table.insertClass(C.c);

		return true;
	}
	
	@Override
	public boolean visit(FeatureNode F){
		if (F.feat instanceof AST.method){
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

				Table.insert(F.method.name,F.method);

				
			}
			else{	



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

				Table.insert(F.attribute.name,F.attribute);

			}
			else {



			}
		
		}




		return true;
	}


	@Override
	public boolean visit(ExpressionNode E){
		E.visitEcount++;
		if (E.expr instanceof AST.block){
			if (E.visitEcount<2) { 
				Table.enterScope(); 
				return true;
			}
			else {
				// Set the type of the expression node as the type of the last expression in 
				// the expList. 
			}
	
		}
		else if (E.expr instanceof AST.assign){
			if (E.visitEcount < 2) {
				//Table.lookupClassSpace(E.ex);
			}
		}



		return true;
	}

	public String getType(ExpressionNode E){

		if (E.expr instanceof AST.int_const){
			return "int";
		}
		else if  (E.expr instanceof AST.string_const){
			return "string";
		}
		else if (E.expr instanceof AST.bool_const){
			return "bool";
		}
		else if (E.expr instanceof AST.object){

		}
	return "Object";

	}
}



interface ASTnode {
	public void accept(ASTvisitor visitor);
	public String typeInfo = "no_type";
}

class ExpressionNode implements ASTnode{

	AST.expression expr;
	AST.block blockexpr;
	ArrayList<ExpressionNode> expList;
	String typeInfo;

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
		visitor.visit(this);

		for (ExpressionNode e : expList){
			e.accept(visitor);
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
			expMethod.accept(visitor);


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
		}
	}
	
};




