package cool;
import java.util.ArrayList;
import java.util.HashMap;
import java.*;


// To Do : Implement String and printf and scanf codes. 

public class IRBuilder extends IRBuilderUtils{
    public IRBuilder(){

    }
    // prints the constructor 
    public String generateCtor(AST.class_ cl){

        Globals.outFile.println(";Constructor for class "+cl.name);
        System.out.println("Generating constructors for class:" + cl.name);

        Globals.currentClassPtrType=getTypeid(cl.name, 0);
        Globals.scopeTable.setCurrentClass(cl);

        StringBuilder SB = new StringBuilder();
        SB.append(
            "define dso_local void @" +getClassMangledName(cl.name)+
            "("+Globals.classNameToStructMap.get(cl.name)+"* %this){" 
        );
        Globals.classToConstructorMap.put(cl.name,"@"+getClassMangledName(cl.name));
        Globals.outFile.println(SB.toString());
        SB.setLength(0);
        Globals.IRB.generateLabel("entry");
        String allocaReg = generateAllocaInst(cl.name,"%this.addr",true);
        generateStoreInst(allocaReg ,cl.name , "%this");
        String ptrReg = generateLoadInst("%this1", cl.name, allocaReg);
        
        ClassNode parentClass= Globals.IG.getClassNode(cl).parent;
        // Calling the constructor of the parent class
        if (parentClass!=null){
            String tempReg = generateBitCastInst(ptrReg, cl.name, parentClass.self.name);
            generateCallCtor("IO", Globals.classToConstructorMap.get(parentClass.self.name),parentClass.self.name,tempReg);
        }
        

        for (AST.feature f : cl.features){
            if (f instanceof AST.attr){
                //String attrPtr="";
                String attrPtr = generateGEPInstForAttr(f.name,cl.name, ptrReg, Globals.attributeToAddrMap.get(cl.name).get((AST.attr)f));
                
                if (((AST.attr)f).value!=null && ! (((AST.attr)f).value instanceof AST.no_expr)){
                    // if expr is not null then   
                    //String reg = getNewVariableName();
                    //generateLoadInst(reg, ((AST.attr)f).typeid ,generateInstruction(((AST.attr)f).value) ) ;
                    String reg = generateInstruction(((AST.attr)f).value);
                    generateStoreInst(attrPtr, ((AST.attr)f).typeid , reg);
                    
                }
                else generateDefaultStore(((AST.attr)f).typeid, attrPtr);
            }
        }
        SB.append(Indent+"ret void\n}");

        Globals.liveAttrPtr.clear();
        Globals.outFile.println(SB.toString());
        return null;

    }

    public String generateInstruction(AST.expression e){

        if (e instanceof AST.plus){

            System.out.println("Generating Instructions for plus");
            StringBuilder SB = new StringBuilder();
            String value = getNewVariableName() ;
            SB.append(Indent+value);
            SB.append(" = "); //
            String ee1 = generateInstruction(((AST.plus)e).e1) ; //ee1 type should be interger
            String ee2 = generateInstruction(((AST.plus)e).e2) ; //ee2 type should be interger
            
            SB.append("add " + Int_32 + " " + ee1 + " , "+ ee2);
            
            Globals.outFile.println(SB.toString());
            return value;
        }

        else if (e instanceof AST.sub){

            System.out.println("Generating Instructions for sub");
            StringBuilder SB = new StringBuilder();
            String value = getNewVariableName() ;
            SB.append(Indent+value).append(" = ");
            
            String ee1 = generateInstruction(((AST.sub)e).e1) ; //ee1 type should be interger
            String ee2 = generateInstruction(((AST.sub)e).e2) ; //ee2 type should be interger

            SB.append("sub " + Int_32 + " " + ee1 + " , "+ ee2 );
            Globals.outFile.println(SB.toString());
            return value;
            
        }

        else if (e instanceof AST.mul){

            System.out.println("Generating Instructions for mul");
            StringBuilder SB = new StringBuilder();
            String value = getNewVariableName() ;
            SB.append(Indent+value).append(" = ");
            
            String ee1 = generateInstruction(((AST.mul)e).e1) ; //ee1 type should be interger
            String ee2 = generateInstruction(((AST.mul)e).e2) ; //ee2 type should be interger

            SB.append(Indent+"mul " + Int_32 + " " + ee1 + " , "+ ee2);
            Globals.outFile.println(SB.toString());
            return value;

        }
        else if (e instanceof AST.divide){

            System.out.println("Generating Instructions for divide");
            StringBuilder SB = new StringBuilder();
            String value = getNewVariableName() ;
            SB.append(Indent+value).append(" = ");
            String ee1 = generateInstruction(((AST.divide)e).e1) ; //ee1 type should be interger
            String ee2 = generateInstruction(((AST.divide)e).e2) ; //ee2 type should be interger

            SB.append("udiv " + Int_32 + " " + ee1 + " , "+ ee2 );
            Globals.outFile.println(SB.toString());
            return value;

        }

        else if (e instanceof AST.lt){

            System.out.println("Generating Instructions for lt");
            StringBuilder SB = new StringBuilder() ;

            String value = getNewVariableName() ;
            String ee1 = generateInstruction(((AST.lt)e).e1) ; //ee1 type should be interger
            String ee2 = generateInstruction(((AST.lt)e).e2) ; //ee2 type should be interger

            SB.append(Indent+value).append(" = ");
            SB.append("icmp ult "+ ee1 + " , "+ ee2 );
            Globals.outFile.println(SB.toString());
            return value;

        }
        else if (e instanceof AST.leq){

            System.out.println("Generating Instructions for leq");
            StringBuilder SB = new StringBuilder() ;

            String value = getNewVariableName() ;
            String ee1 = generateInstruction(((AST.leq)e).e1) ; //ee1 type should be interger
            String ee2 = generateInstruction(((AST.leq)e).e2) ; //ee2 type should be interger

            SB.append(Indent+value).append(" = ");
            SB.append("icmp ule " + ee1 + " , "+ ee2);
            Globals.outFile.println(SB.toString());
            return value;

        }
        else if (e instanceof AST.eq){

            System.out.println("Generating Instructions for eq");
            StringBuilder SB = new StringBuilder() ;
            String value = getNewVariableName() ;
            String ee1 = generateInstruction(((AST.eq)e).e1) ; //ee1 type should be interger
            String ee2 = generateInstruction(((AST.eq)e).e2) ; //ee2 type should be interger
            SB.append(Indent+value).append(" = ");
            SB.append("icmp eq " + ee1 + " , "+ ee2  );
            Globals.outFile.println(SB.toString());
            return value;

        }
        else if (e instanceof AST.comp){

            System.out.println("Generating Instructions for comp");
            StringBuilder SB = new StringBuilder() ;
            String value = getNewVariableName() ;
            String ee1 = generateInstruction(((AST.comp)e).e1) ; //ee1 type should be interger
            ee1 = ee1.contains(" ") ? ee1.split(" ")[0] : ee1 ;
            
            SB.append(Indent+value).append(" = ");
            SB.append("xor "+Int_32 + ee1 + " , -1 " ); //finding ones complement
            Globals.outFile.println(SB.toString());
            return value;
        }
        else if (e instanceof AST.neg){

            System.out.println("Generating Instructions for neg");
            StringBuilder SB = new StringBuilder();
                String value = getNewlabelName() ;
                String exprvalue = generateInstruction(((AST.neg)e).e1) ;

                SB.append(value).append(" = xor ").append(Bool).append(" ").append(exprvalue).append(" , -1 ") ; //adding xor value with 1 gives complement  
               
               return value ;
        }
        else if (e instanceof AST.cond) { //change the value as pridicate :

            System.out.println("Generating Instructions for if");
            StringBuilder SB = new StringBuilder() ;
            String predicate = generateInstruction(((AST.cond)e).predicate) ; //gives predicate value
            String ifbody = "if.then"+getNewlabelName() ;
            String elsebody = "if.else"+getNewlabelName() ;
            String afterbranch = "if.end"+getNewlabelName() ;
            // To do : see if bitcast instruction is needed or not
            //adding br instruction in this block//
            SB.append(Indent+"br i1 ").append(predicate).append(", label %").append(ifbody).append(", label %").append(elsebody) ;

            //create label instruction of ifblock ;//
            SB.append("\n\n");
            Globals.liveAttrPtr.clear();
            SB.append(ifbody).append(":") ;
            Globals.outFile.println(SB.toString());
            SB.setLength(0);
            
            generateInstruction(((AST.cond)e).ifbody) ;
            
            SB.append(Indent+"br label %").append(afterbranch);
            SB.append("\n\n");
            Globals.liveAttrPtr.clear();
            SB.append(elsebody).append(":") ;
            Globals.outFile.println(SB.toString());
            SB.setLength(0);
            
            generateInstruction(((AST.cond)e).elsebody) ;
            
            SB.append(Indent+"br label %").append(afterbranch);
            SB.append("\n\n");
            Globals.liveAttrPtr.clear();
            SB.append(afterbranch).append(":");
            Globals.outFile.println(SB.toString());
            
            return predicate;  // see if returned value to be used anywhere or not
        }

        else if (e instanceof AST.block){

            String finalReg="";
            Globals.scopeTable.enterScope();
            for (AST.expression exp: ((AST.block)e).l1){
                finalReg = generateInstruction(exp);
            }
            Globals.scopeTable.exitScope();
            return finalReg;
        }

        else if (e instanceof AST.loop){
            
            System.out.println("Generating Instructions for loop");
            StringBuilder SB = new StringBuilder() ;
            Globals.liveAttrPtr.clear();
            String loopNumber  =  getNewLoopName();
            Globals.outFile.println("");
            SB.append("while.cond"+loopNumber).append(":");
            Globals.outFile.println(SB.toString());
            SB.setLength(0);
            String value = generateInstruction(((AST.loop)e).predicate) ;
            if(! ((AST.loop)e).predicate.type.equals("Bool")) value = getTruncInst(value,getTypeid(value,0),"i1"); //how to get typeid //
            SB.append(Indent+"br i1 ").append(value).append(" , label ").append("while.body"+loopNumber).append(" , label ").append("while.end"+loopNumber) ; //conditional branch
            SB.append("\n\n");
            Globals.liveAttrPtr.clear();
            SB.append("while.body"+loopNumber).append(":");
            Globals.outFile.println(SB.toString());
            SB.setLength(0);

            value = generateInstruction(((AST.loop)e).body);

            SB.append(Indent+"br label ").append("while.cond"+loopNumber).append("\n\n");
            Globals.liveAttrPtr.clear();
            SB.append("while.end"+loopNumber).append(":");

            Globals.outFile.println(SB.toString());

            return value ;
        }
        else if (e instanceof AST.assign){

            System.out.println("Generating Instructions for assign exp");
                 
                 AST.ASTNode id= Globals.scopeTable.lookUpGlobal(((AST.assign)e).name, AST.formal.class);   
                 String finalReg = generateInstruction(((AST.assign)e).e1);
                 String interReg = "";
                 if (id!=null){
                     generateStoreInst("%"+((AST.formal)id).name+".addr", ((AST.formal)id).typeid, finalReg );                    
                    return "";
                }
                 if (id==null) id = Globals.scopeTable.lookUpGlobal(((AST.assign)e).name,AST.attr.class);
                 
                 if (id==null){
                   id = (AST.attr) Globals.scopeTable.lookUpParent(((AST.assign)e).name,AST.attr.class);
                   interReg = generateBitCastInst("%this1", Globals.scopeTable.currentClass.name,Globals.scopeTable.parClass.name);        
                   if (Globals.liveAttrPtr.get(((AST.assign)e).name)==null) interReg = generateGEPInstForAttr(((AST.assign)e).name,Globals.scopeTable.parClass.name, interReg, Globals.attributeToAddrMap.get(Globals.scopeTable.parClass.name).get(((AST.attr)id))); 
                   generateStoreInst(interReg,((AST.attr)id).typeid,finalReg);
                    return "";
                }
                else{
                    interReg ="%"+((AST.assign)e).name;
                    if (Globals.liveAttrPtr.get(((AST.assign)e).name) == null){
                        System.out.println(Globals.scopeTable.currentClass.name);
                        System.out.println(((AST.attr)id).name);
                        System.out.println(Globals.attributeToAddrMap.get(Globals.scopeTable.currentClass.name).get(((AST.attr)id)));
                        interReg = generateGEPInstForAttr(((AST.assign)e).name,Globals.scopeTable.currentClass.name , "%this1" , Globals.attributeToAddrMap.get(Globals.scopeTable.currentClass.name).get(((AST.attr)id)));                     
                    } 
                    generateStoreInst(interReg,((AST.attr)id).typeid, finalReg);

                }
            return "";        
        }

        else if (e instanceof AST.new_){

            System.out.println("Generating Instructions for new exp");
            StringBuilder SB = new StringBuilder() ;
            String addressToStore = getNewVariableName();
            generateAllocaInst(((AST.new_)e).typeid , addressToStore,false) ; 
            if (Globals.isPrimitiveType(((AST.new_)e).typeid)){
                //String value = getNewVariableName() ; 
               // return  generateLoadInst(value , ((AST.new_)e).typeid , addressToStore) ;
                return addressToStore;
            }
            else {
                generateCallCtor("IO",Globals.classToConstructorMap.get(((AST.new_)e).typeid),((AST.new_)e).typeid,addressToStore);
                return addressToStore;
            }
            // addressToStore ; //contains address of Object ;
           
            //return value ;
        }

        else if (e instanceof AST.object){

            System.out.println("Generating Instructions for Object:"+((AST.object)e).name + " type :"+((AST.object)e).type);
            String objName = ((AST.object)e).name;
            if (objName.equals("self")) {
                objName = Globals.scopeTable.currentClass.name;
                return "%this1";
            }
            AST.formal formal =(AST.formal) Globals.scopeTable.lookUpGlobal(objName,AST.formal.class);
            if (formal !=null) return generateInstForFormalObj(formal);
            else{
                AST.attr obj = (AST.attr) Globals.scopeTable.lookUpGlobal(objName,AST.attr.class);
                if (obj==null){
                    obj = (AST.attr) Globals.scopeTable.lookUpParent(objName, AST.attr.class);
                    if (obj!=null) return generateFromParent(obj);
                    else System.out.println("Error ** object not available");
                }
                if (obj!=null){
                    return generateInstForAttrObj(obj);
                }
            }
            //return null;
                 
        }
        else if (e instanceof AST.int_const){ //returns the loaded variable//
            System.out.println("Generating Instructions for int");
            Integer val = ((AST.int_const)e).value;

            return val.toString();


        }
        else if (e instanceof AST.string_const){
            System.out.println("Generating Instructions for string");
            StringBuilder SB = new StringBuilder();
            String string = ((AST.string_const)e).value;
            Integer length  = string.length();
            
            SB.append("getelementptr inbounds (").append("[").append(length.toString()).append(" x i8]");
            SB.append(", ").append("[").append(length.toString()).append(" x i8]").append("* ").append(Globals.globalStrings.get(string));
            SB.append(", ").append("i32 0, i32 0)");

            return SB.toString();

            
  
        }
        else if (e instanceof AST.bool_const){
            System.out.println("Generating Instructions for bool");
            String value = (((AST.bool_const)e).value == true) ? "1" : "0";
            return value ;

        }

        else if (e instanceof AST.static_dispatch){
            System.out.println("Generating static dispatch exp");
            // To do : See when to add the caller expression register to the arguments list
            ArrayList<String> arguments = new ArrayList<String>();
            ArrayList<String> types = new ArrayList<String>();
            //arguments.add("%this1");
            String callerType = ((AST.static_dispatch)e).typeid;
            String callerReg = generateInstruction(((AST.static_dispatch)e).caller);
            // if (!Globals.isPrimitiveType(callerType)){
            //     if (callerType.equals(Globals.scopeTable.currentClass.name)){
            //         arguments.add("%this1");
            //         types.add(callerType);
            //     }
            //     else{
            //         arguments.add(generateBitCastInst("%this1",Globals.scopeTable.currentClass.name,callerType));
            //         types.add(callerType);
            //     }                
            // }
            arguments.add(callerReg);
            types.add(((AST.static_dispatch)e).typeid);
            int index = 0;
            AST.method md =(AST.method) Globals.scopeTable.lookUpGlobal(((AST.static_dispatch)e).name,AST.method.class);
            if (md==null) {
                System.out.println("paremt");
                md = (AST.method) Globals.scopeTable.lookUpParent(((AST.static_dispatch)e).name, AST.method.class);
            }
            for ( AST.expression exp : ((AST.static_dispatch)e).actuals){
                String reg = generateInstruction(exp);
                if (! md.formals.get(index).typeid.equals(exp.type) ){
                    reg = generateBitCastInst(reg,md.formals.get(index).typeid,exp.type);                        
                }
                arguments.add(reg);

                types.add(md.formals.get(index).typeid);
                index++;
            }
            
            String methodName = Globals.methodMap.get(((AST.static_dispatch)e).name);
            System.out.println(methodName);
            String returnType = ((AST.static_dispatch)e).typeid;
            String returnReg = "%call";

            if (callNameMaker > 0 ) returnReg+=callNameMaker.toString();
            if (getTypeid(returnType,0).equals("void") && !methodName.equals("@printf") && !methodName.equals("@scanf")) returnReg = ""; 
            
            callNameMaker++;

            if ( ((AST.static_dispatch)e).name.equals("out_string") || ((AST.static_dispatch)e).name.equals("out_int") ){
                /*
                @.str.1 = private unnamed_addr constant [3 x i8] c"%f\00", align 1
                @.str.2 = private unnamed_addr constant [3 x i8] c"%d\00", align 1
                %call3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.1, i32 0, i32 0), i32 %2)
                %call4 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.2, i32 0, i32 0), i32* %v)

                printf takes arguments same is scanf so out_string in_string out_int and in_int 
                */
                if (((AST.static_dispatch)e).name.equals("out_int")) return generatePrintfCall(arguments.get(1),returnReg,"Int");
                else return generatePrintfCall(arguments.get(1),returnReg,"String");
            
            }
            else if (((AST.static_dispatch)e).name.equals("in_string") || ((AST.static_dispatch)e).name.equals("in_int") ){
                if (((AST.static_dispatch)e).name.equals("out_int")) return  generateScanfCall(arguments.get(1),returnReg,"Int");
                return generateScanfCall(arguments.get(1),returnReg,"String");
            }
            else if (((AST.static_dispatch)e).name.equals("length")){
                return generateStrLenCall(arguments.get(0),returnReg);
            }
            else if (((AST.static_dispatch)e).name.equals("concat")){
                return generateStrConcatCall(arguments.get(0),arguments.get(1),returnReg);
            }
            else if (((AST.static_dispatch)e).name.equals("substr")){
                return generateSubStrCall(arguments.get(1),arguments.get(2),returnReg);
            
            }
            else if (((AST.static_dispatch)e).name.equals("abort")){
                return generateExitCall();
                //return "";
            }
            else if (((AST.static_dispatch)e).name.equals("type_name")){
                
            }
            else generateCallInst(returnType,methodName,types,arguments,returnReg);
            
            
            
            return returnReg;

        }
        else if (e instanceof AST.let){
            // no need to implement
            return null;
        }
        else if (e instanceof AST.dispatch){
            // No need to implement
            return null;
        }
        else if (e instanceof AST.typcase){
            return null;
        }
        return null;
    }    





}



















