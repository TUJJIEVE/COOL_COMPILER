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
			 ArrayList<AST.formal> f1 = new ArrayList<AST.formal>();
			 AST.formal fo1 = new AST.formal("s","String",0);
			 f1.add(fo1);
			
			 Globals.scopeTable.insert("concat", new AST.method("concat", f1,"String", null, 0));
			 
			 ArrayList<AST.formal> f2 = new ArrayList<AST.formal>();
			 AST.formal f21 = new AST.formal(null,null,0);
			 
			 f2.add(f21);
			
				 
			 Globals.scopeTable.insert("in_string", new AST.method("in_string",f2,"String",null,0));
			 ArrayList<AST.formal> f3 = new ArrayList<AST.formal>();
			 AST.formal f31 = new AST.formal("s","String",0);
			 
			 f3.add(f31);
	
	
			 Globals.scopeTable.insert("out_string",new AST.method("out_string",f3,"String",null,0));
			 ArrayList<AST.formal> f4 = new ArrayList<AST.formal>();
			 AST.formal f41 = new AST.formal("s","Int",0);
			 f4.add(f41);
			 
			 Globals.scopeTable.insert("out_int", new AST.method("out_int",f4,"Int", null, 0));
			 ArrayList<AST.formal> f5 = new ArrayList<AST.formal>();
			 AST.formal f51 = new AST.formal(null,null,0);
			 f5.add(f51);
		
			 Globals.scopeTable.insert("in_int",new AST.method("in_int",f5,"Int", null, 0));
			 ArrayList<AST.formal> f6 = new ArrayList<AST.formal>();
			 AST.formal f61 = new AST.formal(null,null,0);
			 f6.add(f61);
	
	
			 Globals.scopeTable.insert("length",new AST.method("length", f6, "Int", null, 0));

			 ArrayList<AST.formal> f7 = new ArrayList<AST.formal>();
			 AST.formal f71 = new AST.formal("i","Int",0);
			 AST.formal f72 = new AST.formal("j","Int",0);
			 f7.add(f71);
			 f7.add(f72);
			 Globals.scopeTable.insert("substr",new AST.method("substr", f7, "String", null, 0));

			 ArrayList<AST.formal> f8 = new ArrayList<AST.formal>();
			 AST.formal f81 = new AST.formal(null,null,0);
			 
			 f8.add(f81);
			 
			Globals.scopeTable.insert("type_name",new AST.method("type_name",f8,"String",null,0));



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
			stringsFound.add("Error : Division by zero");
			Globals.IG.setGraph(listOfClasses);
			Globals.methodMap.put("in_string","@scanf");
			Globals.methodMap.put("out_int","@printf");
			Globals.methodMap.put("in_int","@scanf");
			Globals.methodMap.put("out_string","@printf");
			Globals.methodMap.put("length","@strlen");
			Globals.methodMap.put("concat","@strcat");
			Globals.methodMap.put("abort","@exit");
			Globals.methodMap.put("malloc","@malloc");
			Globals.methodMap.put("strncpy","@strncpy");
			Globals.methodMap.put("substr","@"+Globals.IRB.getClassMangledName("substr"));
			Globals.methodReturnMap.put("@strcat","String");
			Globals.methodReturnMap.put("@strlen","Int");
			Globals.methodReturnMap.put("@malloc","String");
			Globals.methodReturnMap.put("@printf","String");
			Globals.methodReturnMap.put("@scanf","String");
			Globals.methodReturnMap.put("@exit","void");
			Globals.methodReturnMap.put("@strncpy","String");
			Globals.methodReturnMap.put("@"+Globals.IRB.getClassMangledName("substr"),"String");
			Globals.methodMap.put("type_name","@"+Globals.IRB.getClassMangledName("type_name"));
			Globals.methodReturnMap.put("@"+Globals.IRB.getClassMangledName("type_name"),"String");

			//Globals.methodMap.put("substr","@")
			for (AST.class_ cl : program.classes){
				Globals.scopeTable.insertClass(cl);
				addDefaultFuncs();
				Globals.attributeToAddrMap.put(cl.name,new HashMap<AST.attr,Integer>());
				int index = 2;
				if(Globals.IG.getClassNode(cl).parent!=null) index++;
				stringsFound.add(cl.name);
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
						Globals.methodReturnMap.put("@"+Globals.IRB.getFunctionMangledName(f.name,cl.name),f.typeid);
						Globals.scopeTable.insert(f.name, ((AST.method)f));
						
					}
				}
				Globals.scopeTable.printTable(cl);
			}
			Globals.outFile.println("\n");
			Globals.IRB.generatePreStructs();
			generateStructures(listOfClasses);     // For generating the structures of the classes defined.
			Globals.IG.resetGraph();

			Globals.outFile.println("\n");
			for (String s : stringsFound){
				Globals.IRB.generateGlobalString(s);

			}
			Globals.IRB.generatePreConstructs();
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
			Globals.IRB.labelNameMaker=0;
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