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
		public void addDefaultFuncs(){
			// Globals.scopeTable.insert("concat", new AST.method("concat", new ArrayList<AST.formal>(new AST.formal("s", "String", 0),new AST.formal("s1","String",0)),"String", null, 0));
			// Globals.scopeTable.insert("in_string", new AST.method("in_string",null,"String",null,0));
			// Globals.scopeTable.insert("out_string",new AST.method("out_string",new ArrayList<AST.formal>(new AST.formal("x","String",0)),null,null,0));
			// Globals.scopeTable.insert("out_int", new AST.method("out_int", new ArrayList<AST.formal>(new AST.formal("n", "Int", 0)), null, null, 0));
			// Globals.scopeTable.insert("in_int",new AST.method("in_int", null,"Int", null, 0));
			// Globals.scopeTable.insert("length",new AST.method("length", null, "Int", null, 0));



		}
		@Override
		public void visit(AST.program program){

			Globals.IRB.generatePreReq(program.classes.get(0).filename);   // To generate the module names and data layout etc..
			ArrayList<ClassNode> listOfClasses = new ArrayList<ClassNode>();
			for (AST.class_ cl : program.classes){
				listOfClasses.add(new ClassNode(cl));
			}
			ArrayList<String> stringsFound = new ArrayList<String>();
			
			stringsFound.add("%d");
			stringsFound.add("%s");

			Globals.IG.setGraph(listOfClasses);
			Globals.methodMap.put("in_string","@scanf");
			Globals.methodMap.put("out_int","@printf");
			Globals.methodMap.put("in_int","@scanf");
			Globals.methodMap.put("out_string","@printf");
			Globals.methodMap.put("length","@strlen");
			Globals.methodMap.put("concat","@strcat");
		
			//Globals.methodMap.put("substr","@")
			for (AST.class_ cl : program.classes){
				Globals.scopeTable.insertClass(cl);
				addDefaultFuncs();
				Globals.attributeToAddrMap.put(cl.name,new HashMap<AST.attr,Integer>());
				int index = 0;
				for (AST.feature f : cl.features){
					f.className = cl.name;
					
					if (f instanceof AST.attr){
						
						Globals.scopeTable.insert(f.name, ((AST.attr)f));
						
						if ( ((AST.attr)f).value instanceof AST.string_const){
							AST.string_const s  =(AST.string_const) ((AST.attr)f).value;
							stringsFound.add(s.value);						
						}

						Globals.attributeToAddrMap.get(cl.name).put((AST.attr)f,index);
						index++;
						
					}
					else {
						if (f.name.equals("main")) Globals.mainReturnType = f.typeid;
						Globals.methodMap.put(f.name,"@"+Globals.IRB.getFunctionMangledName(f.name,cl.name));
						
						Globals.scopeTable.insert(f.name, ((AST.method)f));
						
					}
				}
				Globals.scopeTable.printTable(cl);
			}
			Globals.outFile.println("\n");
			generateStructures(listOfClasses);     // For generating the structures of the classes defined.
			Globals.IG.resetGraph();

			Globals.outFile.println("\n");
			for (String s : stringsFound){
				Globals.IRB.generateGlobalString(s);

			}

			Globals.outFile.println("\n");
			generateConstructors(listOfClasses);  // For generating the constructors of the classes defined
			Globals.IG.resetGraph();
			Globals.IRB.generateMainMethod();	

			for (AST.class_ cl : program.classes){
				cl.accept(this);
			}


		}
		@Override
		public void visit(AST.class_ cl){
			System.out.println("visiting class:"+cl.name);
			Globals.scopeTable.setCurrentClass(cl);
			Globals.currentClassPtrType=Globals.IRB.getTypeid(cl.name,0);

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

			System.out.println("Generating method for :"+method.name);

			Globals.currentLocalReg = 0;
			AST.class_ cl = Globals.scopeTable.currentClass;
			StringBuilder SB = new StringBuilder();
			Globals.outFile.println(";Class " + cl.name + " method " + method.name);
			SB.append("define dso_local " + Globals.IRB.getTypeid(method.typeid,0) + " @" +Globals.IRB.getFunctionMangledName(method.name,cl.name)+" (");

			SB.append(Globals.IRB.getTypeid(cl.name,0)).append(" %this, ");

			Globals.scopeTable.enterScope();
			
			for (AST.formal fo : method.formals){
				SB.append(Globals.IRB.getTypeid(fo.typeid,0) +" %"+fo.name);
				Globals.scopeTable.insert(fo.name,fo);
				SB.append(", ");
			}
			SB.deleteCharAt(SB.length()-1);
			SB.deleteCharAt(SB.length()-1);
			SB.append(") {");
			Globals.outFile.println(SB.toString());
			Globals.IRB.generateLabel("entry");
			Globals.IRB.generateAllocaInst(method.className, "%this.addr",true);
			Globals.IRB.generateStoreInst("%this.addr", method.className, "%this");
			Globals.IRB.generateLoadInst("%this1", method.className, "%this.addr");
			
			for (AST.formal fo : method.formals){
				Globals.IRB.generateAllocaInst(fo.typeid,"%"+fo.name+".addr",true);
				Globals.IRB.generateStoreInst("%"+fo.name+".addr" ,fo.typeid,"%"+fo.name);				
			}

			String returnAddr=method.body.accept(this);
			
			Globals.scopeTable.exitScope();
			
			if (!(method.body.type.equals(method.typeid))){
				// Have to do bitcast operation
				returnAddr=Globals.IRB.generateBitCastInst(returnAddr, method.body.type, method.typeid);
			}
			Globals.IRB.generateReturnInst(method.typeid, returnAddr);
			Globals.outFile.println("}");
			
			Globals.liveAttrPtr.clear();



		}
		@Override
		public void visit(AST.formal formals){
			

		}
		@Override
		public String visit(AST.expression expr){
			System.out.println("visiting expression");
			return Globals.IRB.generateInstruction(expr);

		}


}