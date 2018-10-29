package cool;
import cool.ClassNode;
import java.util.ArrayList;
import java.util.HashMap;
// Inheritance graph class which makes a graph from the classes present in the program 
// Contains methods to check for cycles in the graph
public class InheritanceGraph{

	ArrayList<ClassNode> listOfClasses;
	
	public void setGraph(ArrayList<ClassNode> listOfClasses){
		this.listOfClasses = listOfClasses;

		for(int i=0;i<listOfClasses.size();i++){
			ClassNode temp = listOfClasses.get(i);
			if (temp.self.parent.equals("Object")) {
				temp.parent=null;
				continue;	
			}
			for (int j=0;j<listOfClasses.size();j++){
				if (temp.self.parent.equals(listOfClasses.get(j).self.name)){
					temp.parent = listOfClasses.get(j);
				}
			}

		}

	}
	public void resetGraph(){
		for (ClassNode CN : listOfClasses){
			CN.isVisited=false;
		}
	}

	public AST.class_ getTopNode(){
		for (ClassNode CN : listOfClasses){
			if (CN.parent==null){
				return CN.self;
			}
		}
		return null;
	}
	// Function for checking if the inheritance graph is correct or not if not then return false else true
	public InheritanceGraph(){
	

	}
// Method to check if the inheritance graph contains cycles returns true if contains else false
	public AST.class_ getLCA(AST.class_ c1, AST.class_ c2){
		return null;
	}

	public AST.class_ getJoin(AST.class_ c1,AST.class_ c2){
		return null;

	}

// Function to get the classNode in the inheritance graph
	public ClassNode getClassNode(AST.class_ c){
		for (ClassNode cn : listOfClasses){
			if (cn.self.name.equals(c.name)){
				return cn;
			}
		}
		return null;
	}
}


