package cool;
import java.util.ArrayList;
import java.util.HashMap;
public class ConcreteVisitor implements Visitor {

		public void generateStructParent(ClassNode CN){
			if (CN.isVisited==true) return;
			CN.isVisited=true;

			if (CN.parent!=null){
				generateStructParent(CN.parent);
			}
			Globals.outFile.println(";The struct for "+CN.self.name);
			Globals.IRB.generateStruct(CN.self);  // IRBuilder Shoudl generate Strucut for the class cl and return string

		}

		public void generateStructures(ArrayList<ClassNode> listOfClasses){
			
			Globals.outFile.println(";Structures of the classes");

			for (ClassNode CN : listOfClasses){
				if (CN.isVisited==true) continue;
				generateStructParent(CN);
				
				
			}
			

		}

		public void generateCtorsParent(ClassNode CN){
			if(CN.isVisited) return;
			CN.isVisited=true;
			if (CN.parent!=null){
				generateCtorsParent(CN.parent);

			}
			Globals.IRB.generateCtor(CN.self);
		}

		public void generateConstructors(ArrayList<ClassNode> listCl){
			
			for (ClassNode CN : listCl){
				if (CN.isVisited){
					continue;
				}
				generateCtorsParent(CN);
			}

			
		}
		@Override
		public void visit(AST.program program){

			Globals.IRB.generatePreReq(program.classes.get(0).filename);   // To generate the module names and data layout etc..

			ArrayList<ClassNode> listOfClasses = new ArrayList<ClassNode>();
			for (AST.class_ cl : program.classes){
				listOfClasses.add(new ClassNode(cl));
			}
			Globals.IG.setGraph(listOfClasses);
			for (AST.class_ cl : program.classes){
				Globals.scopeTable.insertClass(cl);
				Globals.attributeToAddrMap.put(cl.name,new HashMap<AST.attr,Integer>());
				int index = 0;
				for (AST.feature f : cl.features){
					if (f instanceof AST.attr){
						Globals.scopeTable.insert(cl.name+f.name, ((AST.attr)f));
						Globals.attributeToAddrMap.get(cl.name).put((AST.attr)f,index);
						index++;
						
					}
					else {
						Globals.scopeTable.insert(cl.name+f.name, ((AST.method)f));
						
					}
				}
			}
			generateStructures(listOfClasses);     // For generating the structures of the classes defined.
			Globals.IG.resetGraph();
			generateConstructors(listOfClasses);  // For generating the constructors of the classes defined
			Globals.IG.resetGraph();

			for (AST.class_ cl : program.classes){
				cl.accept(this);
			}


		}
		@Override
		public void visit(AST.class_ cl){
			Globals.scopeTable.setCurrentClass(cl);
			

			for (AST.feature f : cl.features){
				if (f instanceof AST.method){
					((AST.method)f).accept(this);
				}
			}

		}
		@Override
		public void visit(AST.attr attribute){
			
		}

		@Override
		public void visit(AST.method method){

			Globals.currentLocalReg = 0;
			AST.class_ cl = Globals.scopeTable.currentClass;

			Globals.outFile.println(";Class " + cl.name + " method " + method.name);
			StringBuilder SB = new StringBuilder();
			SB.append("define dso_local " + Globals.IRB.getTypeid(method.typeid,0) + " @" +Globals.IRB.getFunctionMangledName(method.name,cl.name)+" (");
			SB.append(Globals.IRB.getTypeid(cl.name,0)).append(" %this, ");
			
			for (AST.formal fo : method.formals){
				SB.append(Globals.IRB.getTypeid(fo.typeid,0) +" %"+fo.name);
				Globals.scopeTable.insert(cl.name+method.name+fo.name,fo);
				SB.append(", ");
			}
			SB.deleteCharAt(SB.length()-1);
			SB.deleteCharAt(SB.length()-1);
			SB.append(") {");
			Globals.outFile.println(SB.toString());
			Globals.IRB.generateLabel("entry");
			Globals.scopeTable.enterScope();
			for (AST.formal fo : method.formals){
				Globals.IRB.generateAllocaInst(fo.typeid,fo.name+".addr");
				Globals.IRB.generateStoreInst("%"+fo.name+".addr" ,fo.typeid,"%"+fo.name);				
			}

			String returnAddr=method.body.accept(this);
			
			Globals.scopeTable.exitScope();
			
			if (!(method.body.type.equals(method.typeid))){
				// Have to do bitcast operation
				returnAddr=Globals.IRB.generateBitCastInst(returnAddr, method.body.type, method.typeid);
			}
			Globals.IRB.generateReturnInst(method.typeid, returnAddr);

			




		}
		@Override
		public void visit(AST.formal formals){
			

		}
		@Override
		public String visit(AST.expression expr){

			String finalReg = new String();
			if (expr instanceof AST.block){
				Globals.scopeTable.enterScope();
				for (AST.expression e : ((AST.block)expr).l1){
					finalReg=Globals.IRB.generateInstruction(e);
				}
				Globals.scopeTable.exitScope();
			}
			else{
				finalReg=Globals.IRB.generateInstruction(expr);
			}
			return finalReg;

		}


}