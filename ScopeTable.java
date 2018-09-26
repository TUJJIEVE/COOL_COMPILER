package cool;
import java.util.*;
public class ScopeTable<T> {

	class ClassScope{
		public AST.class_ cl;
		public int scope;
		
		public ClassScope(AST.class_ c){
			this.cl = c;
			this.scope = 0;
		}
	}
	
	private HashMap< ClassScope , ArrayList<HashMap<String,T>> >  maps=new HashMap<ClassScope,ArrayList<HashMap<String, T>>>();
	public AST.class_ currentClass;

	public ScopeTable(){
				
	}
	void insertClass(AST.class_ c){
		System.out.println("Inserting new class namespace");
		maps.put(new ClassScope(c),new ArrayList<HashMap<String,T>>(15));
		currentClass = c;
		ClassScope cscope = searchTable(currentClass);
		ArrayList<HashMap<String,T>> table = maps.get(cscope);
		table.add(new HashMap<String,T>());
	}
	ClassScope searchTable(AST.class_ c){
		for (HashMap.Entry<ClassScope,ArrayList<HashMap<String,T>>> m : maps.entrySet()){
			ClassScope cscope = m.getKey();
			if (cscope.cl.equals(c)){
				return cscope;
			}

		}
		return null;

	}
	void printTable (AST.class_ c){
		ClassScope cs = searchTable(c);
		for (HashMap.Entry<String,T> h : maps.get(cs).get(0).entrySet()){
			System.out.println(h.getKey());
		}
		
	}
	void insert(String s, T t){
		ClassScope cscope = searchTable(currentClass);
		ArrayList<HashMap<String,T>> table = maps.get(cscope);

		if (table.get(cscope.scope) == null){
			System.out.println("The HashMap is empty in the index:"+cscope.scope);
			table.add(new HashMap<String,T>());

		}
//		System.out.println("The HashMap is empty in the index:"+cscope.scope);

		table.get(cscope.scope).put(s,t);
		System.out.println("The stored:" + s);
	}

	void enterScope(){
		System.out.println("Entering Scope");
		ClassScope cscope = searchTable(currentClass);
		cscope.scope++;
		maps.get(cscope).add(new HashMap<String,T>());
		System.out.println("Entered");

	}
	void exitScope(){
		System.out.println("Exiting Scope");
		ClassScope cscope = searchTable(currentClass);
		if (cscope.scope >0 ){
			maps.get(cscope).remove(cscope.scope);
			cscope.scope--;
		}
		System.out.println("Exited");

	}	
	T lookUpClassSpace(String t){
		System.out.println("Looking in the class namespace");
		ClassScope cscope = searchTable(currentClass);
		if (maps.get(cscope).size() == 0){
			System.out.println("The scope table of class " + currentClass.name + " is empty");
			//System.out.println("Nothing found in Class namespace");
			return null;

		}else{
			return maps.get(cscope).get(0).get(t);
		}
	}

	T lookUpLocal(String t){
		System.out.println("Looking in the local scope");
		ClassScope cscope = searchTable(currentClass);
		return maps.get(cscope).get(cscope.scope).get(t);
	}
	T lookUpGlobal(String t){
		System.out.println("Looking in the global scope");
		ClassScope cscope = searchTable(currentClass);
		for ( int i = cscope.scope; i>=0 ; i--){
			
			if (maps.get(cscope).get(i).containsKey(t))
				return maps.get(cscope).get(i).get(t);
		}
		return null;
	}
}
