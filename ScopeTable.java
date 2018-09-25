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
		maps.put(new ClassScope(c),new ArrayList<HashMap<String,T>>());
		currentClass = c;
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

	void insert(String s, T t){
		ClassScope cscope = searchTable(currentClass);
		ArrayList<HashMap<String,T>> table = maps.get(cscope);
		table.get(cscope.scope).put(s,t);
	}

	void enterScope(){
		ClassScope cscope = searchTable(currentClass);
		cscope.scope++;
		maps.get(cscope).add(new HashMap<String,T>());
	}
	void exitScope(){
		ClassScope cscope = searchTable(currentClass);
		if (cscope.scope >0 ){
			maps.get(cscope).remove(cscope.scope);
			cscope.scope--;
		}
	}	
	T lookUpClassSpace(String t){
		ClassScope cscope = searchTable(currentClass);
		return maps.get(cscope).get(0).get(t);
	}

	T lookUpLocal(String t){
		ClassScope cscope = searchTable(currentClass);
		return maps.get(cscope).get(cscope.scope).get(t);
	}
	T lookUpGlobal(String t){
		ClassScope cscope = searchTable(currentClass);
		for ( int i = cscope.scope; i>=0 ; i--){
			
			if (maps.get(cscope).get(i).containsKey(t))
				return maps.get(cscope).get(i).get(t);
		}
		return null;
	}
}
