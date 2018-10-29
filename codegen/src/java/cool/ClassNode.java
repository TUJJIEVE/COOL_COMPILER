package cool;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassNode{
	// Class used in the inheritance graph stores the parent of the class
	public AST.class_ self;
	public ClassNode parent;
	public boolean isVisited=false;
	public ClassNode(){
		this.self = null;
	}
	public ClassNode(AST.class_ c){
		this.self = c;
		this.parent = null;
		this.isVisited=false;
	}
}