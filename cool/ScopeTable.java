package cool;
import java.util.*;

public class ScopeTable<T> {
	// Class to store the scope of the current class
	class ClassScope{
		public AST.class_ cl;
		public int scope;
		
		public ClassScope(AST.class_ c){
			this.cl = c;
			this.scope = 0;
		}
	}
	// This is the scope cum symbol table
	public HashMap< ClassScope , ArrayList<HashMap<String,T>> >  maps=new HashMap<ClassScope,ArrayList<HashMap<String, T>>>();
	public AST.class_ currentClass;
	public AST.class_ parClass;
	//public ArrayList<T> returnedNodes;
	public ScopeTable(){
	
		parClass = null;
		currentClass = null;
	}



	void setCurrentClass(AST.class_ c){
		//System.out.println("Changing curr class from:"+currentClass.name+" to:"+c.name);
		currentClass=c; // This is the current class
	}
	void insertClass(AST.class_ c){
		//System.out.println("Inserting new class namespace");
		maps.put(new ClassScope(c),new ArrayList<HashMap<String,T>>(15));
		currentClass = c;
		ClassScope cscope = searchTable(currentClass);
		ArrayList<HashMap<String,T>> table = maps.get(cscope);
		table.add(new HashMap<String,T>());
	

	}
	// Method to serach in the table for getting the ClassScope object
	ClassScope searchTable(AST.class_ c){
		for (HashMap.Entry<ClassScope,ArrayList<HashMap<String,T>>> m : maps.entrySet()){
			ClassScope cscope = m.getKey();
			if (cscope.cl.equals(c)){
				return cscope;
			}

		}
		return null;

	}
	// Function to print the table
	void printTable (AST.class_ c){
		ClassScope cs = searchTable(c);
		for (HashMap.Entry<String,T> h : maps.get(cs).get(0).entrySet()){
			System.out.println(h.getKey());
		}
		
	}
	// Method to look up in the parent Class
	T lookUpParent(String name,Class<?> cls){
		AST.class_ temp = currentClass;
		ClassNode parentNode = Globals.IG.getClassNode(currentClass).parent;
		while (parentNode!=null){
			setCurrentClass(parentNode.self);
			T node = lookUpGlobal(name,cls);
			if (node!=null) {
				parClass = parentNode.self;
				setCurrentClass(temp);
				return node;
			}
			else parentNode = parentNode.parent;
		}
		setCurrentClass(temp);
		return null;
	}
	// Function to insert the ASTNode 
	void insert(String s, T t){
		ClassScope cscope = searchTable(currentClass);
		ArrayList<HashMap<String,T>> table = maps.get(cscope);

		if (table.get(cscope.scope) == null){
			//System.out.println("The HashMap is empty in the index:"+cscope.scope);
			table.add(new HashMap<String,T>());

		}
//		System.out.println("The HashMap is empty in the index:"+cscope.scope);

		table.get(cscope.scope).put(s,t);
		//System.out.println("The stored:" + s);
	}
	// Function to enter the scope
	void enterScope(){
		//System.out.println("Entering Scope");
		ClassScope cscope = searchTable(currentClass);
		cscope.scope++;
		maps.get(cscope).add(new HashMap<String,T>());
		//System.out.println("Entered");

	}
	// Function to exit the scope
	void exitScope(){
		//System.out.println("Exiting Scope");
		ClassScope cscope = searchTable(currentClass);
		if (cscope.scope >0 ){
			maps.get(cscope).remove(cscope.scope);
			cscope.scope--;
		}
		//System.out.println("Exited");

	}
	// Method to look in the class namespace	
	T lookUpClassSpace(String t){
		//System.out.println("Looking in the class namespace of " + currentClass.name);
		ClassScope cscope = searchTable(currentClass);
		if (maps.get(cscope).size() == 0){
			//System.out.println("The scope table of class " + currentClass.name + " is empty");
			//System.out.println("Nothing found in Class namespace");
			return null;

		}else{
			//returnedNodes.add(maps.get(cscope).get(0).get(t)); 
			return maps.get(cscope).get(0).get(t);
		}
	}
	// Method to look in the local scope
	T lookUpLocal(String t){
		//System.out.println("Looking in the local scope");
		ClassScope cscope = searchTable(currentClass);
		//returnedNodes.add(maps.get(cscope).get(cscope.scope).get(t));
		return maps.get(cscope).get(cscope.scope).get(t);
	}
	// Method to look in the global scope and get object of desired class
	T lookUpGlobal(String t,Class<?> cls){
		//System.out.println("Looking in the global scope");
		ClassScope cscope = searchTable(currentClass);
		for ( int i = cscope.scope; i>=0 ; i--){
			if (maps.get(cscope).get(i).containsKey(t)){
					T node = maps.get(cscope).get(i).get(t);
					if (cls == node.getClass()){
						return maps.get(cscope).get(i).get(t);
					}					
				}
			}
		return null;

		}
}



