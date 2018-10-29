package cool;
import java.util.ArrayList;
import java.util.HashMap;
interface Visitor{

	public void visit(AST.program program);
	public void visit(AST.class_ cl);
	public void visit(AST.attr attribute);
	public void visit(AST.method method);
	public void visit(AST.formal params);
	public String visit(AST.expression exp);


}