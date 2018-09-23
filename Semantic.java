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
	public class ClassNode{
		public AST.class_ self;
		public ClassNode parent;
		public int isVisited;

		public ClassNode(AST.class_ c){
			this.self = c;
			this.parent = null;
			this.isVisited=0;
		}
	}

	public Semantic(AST.program program){
		//Write Semantic analyzer code here
		ArrayList<ClassNode> listOfClasses = new ArrayList<ClassNode>();

		for (AST.class_ cl : program.classes){   // For creating the inheritance graph
			listOfClasses.add(new ClassNode(cl));
		}

		if (!checkInheritanceGraph(listOfClasses)){

			System.out.println("The inheritance graph contains Cycle");
		}

		
	}


// Function for checking if the inheritance graph is correct or not if not then return false else true
	public boolean checkInheritanceGraph(ArrayList<ClassNode> listOfClasses){

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
				System.out.println("No matching subclass found for class " + temp.self.name);
				return false;
			}
		}	

		for (ClassNode cn : listOfClasses){
			boolean isCyclic = checkCycles(cn);
			if (isCyclic){
				System.out.println("The Inheritance graph is cyclic");
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



interface ASTvisitor{
	public boolean visit(ProgramNode v);
	public void visit(ClassNode v);
	public void visit(FeatureNode v);
	public void visit(AttributeNode v);
	public void visit(ExpressionNode v);

}

class Visitor implements ASTVisitor extends Semantic{
	@Override
	public boolean visit(ProgramNode v){
		ArrayList<ClassNode> listOfClasses = new ArrayList<ClassNode>();

		for (AST.class_ cl : program.classes){   // For creating the inheritance graph
			listOfClasses.add(new ClassNode(cl));
		}

		if (!checkInheritanceGraph(listOfClasses)){

			System.out.println("The inheritance graph contains Cycle");
			return false;
		}
		return true;
	}
}

interface ASTnode {
	public void accept(ASTvisitor visitor);
}


class ProgramNode implements ASTnode {
	AST.program prog;
	ProgramNode(AST.program program){
		prog = program;
	}

	@Override
	public void accept(ASTvisitor visitor){
		boolean cond = visitor.visit(this);
		if (!cond) return;
		for (AST.class_ cl : program.classes){
			ClassNode c = new ClassNode(cl);
			c.accept(visitor);
		}
	}
	
}

class ClassNode implements ASTnode{
	AST.class_ class;
	ClassNode(AST.class_ cl){
		class = cl;
	}

	@Override
	public void accept(ASTVisitor visitor){
		visitor.visit(this);

		for (AST.feature fe : class.features){
			FeatureNode f = new FeatureNode(fe);
			f.accept(visitor);

		}
	}
	 
}

class FeatureNode implements ASTnode{
	AST.feature feature;
	FeatureNode (AST.feature fe){
		feature = fe;
	}

	@Override
	public void accept(ASTVisitor visitor){
		visitor.visit(this);
	
		if (feature instanceof AST.method){



		}
		else if (feature instanceof AST.attr){

		}

	}
}
