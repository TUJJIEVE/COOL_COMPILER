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
	public boolean visit(CNode C,FeatureNode F);
	public boolean visit(CNode C,AttributeNode A);
	public boolean visit(CNode C,ExpressionNode E);

}

class Visitor implements ASTvisitor {

	public ScopeTable<AST> Table = new ScopeTable<AST>();
	public InheritanceGraph IG = new InheritanceGraph();	
	
	@Override
	public boolean visit(ProgramNode P){

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


		return true;
	}
	@Override
	public boolean visit(CNode C,FeatureNode F){


		return true;
	}

	@Override
	public boolean visit(CNode C,AttributeNode A){
		return true;
	}

	@Override
	public boolean visit(CNode C,ExpressionNode E){
		return true;
	}
}

interface ASTnode {
	public void accept(ASTvisitor visitor);
}

class ExpressionNode implements ASTnode{
	public ExpressionNode(AST.expression a){

	}

	@Override
	public void accept(ASTvisitor visitor){
		visitor.visit(this);
	}
}

class AttributeNode implements ASTnode{
	public AttributeNode(AST.attr a){

	}

	@Override
	public void accept(ASTvisitor visitor){
		visitor.visit(this);
	}
}

class FeatureNode implements ASTnode{
	AST.feature feature;
	public FeatureNode (AST.feature fe){
		feature = fe;
	}

	@Override
	public void accept(ASTvisitor visitor){
		visitor.visit(this);
	
		if (feature instanceof AST.method){



		}
		else if (feature instanceof AST.attr){

		}

	}
}

class CNode implements ASTnode{

	AST.class_ c;

	public CNode(AST.class_ cl){
		c = cl;
	}

	@Override
	public void accept(ASTvisitor visitor){
		visitor.visit(this);

		for (AST.feature fe : c.features){
			FeatureNode f = new FeatureNode(fe);
			f.accept(visitor);

		}
	}
	 
}

class ProgramNode implements ASTnode {
	AST.program prog;
	public ProgramNode(AST.program program){
		prog = program;
	}

	@Override
	public void accept(ASTvisitor visitor){
		boolean cond = visitor.visit(this);
		if (!cond) return;
		
		for (AST.class_ cl : prog.classes){
			CNode c = new CNode(cl);
			c.accept(visitor);
		}
	}
	
};




