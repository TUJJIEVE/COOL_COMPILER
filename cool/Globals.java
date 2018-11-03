package cool;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.PrintWriter;
public class Globals {

	public static ScopeTable<AST.ASTNode> scopeTable = new ScopeTable<AST.ASTNode>();

	public static int currentLocalReg=0; 

	public static int currentGlobalReg=0;

	public static InheritanceGraph IG = new InheritanceGraph();

	public static IRBuilder IRB = new IRBuilder();

	public static PrintWriter outFile;
	Globals(PrintWriter out){
		outFile = out;
	}
	public static HashMap<String,String> methodMap = new HashMap<String,String>();
	public static HashMap<String,String> classToConstructorMap = new HashMap<String,String>();

	public static HashMap<AST.class_,String> classToStructMap = new HashMap<AST.class_,String>();
	public static HashMap<String,String> classNameToStructMap = new HashMap<String,String>();
	public static HashMap<String,HashMap<AST.attr,Integer>> attributeToAddrMap = new HashMap<String,HashMap<AST.attr,Integer>>();
	public static HashMap<String,Boolean> liveAttrPtr = new HashMap<String,Boolean>();
	public static String currentClassPtr="%this1";
	public static String currentClassPtrType;
	public static String mainReturnType;
	public static boolean isPrintfUsed = false;
	public static boolean isIndent=false;
	public static StringBuilder SB = new StringBuilder();
	public static HashMap<String,String> globalStrings  = new HashMap<String,String>();
	public static Integer numGlobalStrings= new Integer(0);
	public static boolean isPrimitiveType(String type){
		return type.equals("Int") || type.equals("String") || type.equals("Bool") || type.equals("IO") || type.equals("Object") ;

	}
	

}