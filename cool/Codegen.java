package cool;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
public class Codegen{
	public Codegen(AST.program program, PrintWriter out){
		//Write Code generator code here
        out.println("; I am a comment in LLVM-IR. Feel free to remove me.");
		Globals g = new Globals(out);
		Visitor visitor = new ConcreteVisitor();
		program.accept(visitor);
	}
}









