package cool;
import java.util.ArrayList;
import java.util.HashMap;
import java.*;
public class IRBuilder {

    static final String Int_32 = "i32";
    static final String Int_64 = "i64";
    static final String Character = "i8";
    static final String Bool = "i8";
    static final String string = "i8*";
    static final String Indent = "  ";



    static HashMap<String,Integer> labelMap = new HashMap<String,Integer>();

    static int loopNameMaker = 0 ;
    static int labelNameMaker = 0 ;
    
    static String getNewVariableName(){
        String temp = "%" + Integer.toString(Globals.currentLocalReg) ;
        Globals.currentLocalReg++ ;
        return temp ;
    }
    static String getNewLoopName(){
        String temp = "%" +Integer.toString(loopNameMaker) ;
        Globals.currentLocalReg++ ;
        return temp ;
    }
    static String getNewlabelName(){
        String temp = "%" + Integer.toString(labelNameMaker) ;
        Globals.currentLocalReg++ ;
        return temp ;
    }

    String result = "empty";

    IRBuilder(){


    }
    public void generatePreReq(String filename){
        StringBuilder SB = new StringBuilder();
        SB.append("; ModuleID = "+filename +"\n");
        SB.append("source_filename = " + "\"" +filename +"\"\n" );
        SB.append("target datalayout = e-m:e-i64:64-f80:128-n8:16:32:64-S128\n");
        SB.append("target triple = x86_64");
        Globals.outFile.println(SB.toString());
        // Also write default functions 
    }

    // public String getStringType(int size){
    //     // Use the size parameter to declare the array of char
    // }
    public String getTypeid(String type,int size){
        switch(type){
            case "Bool" : return Bool;
            case "Int"  : return Int_32;
            case "String" : return string;
            case "IO" : return "void";
        }
        // If non primitive types then 
        return Globals.classNameToStructMap.get(type)+"*";
    }
    public String getAlignment(String type,int size){
        switch(type){
            case "Int" : return "4";
            case "Bool" : return "1";
        }
        // Compute alignment for other non primitive type
        return null;
    }
    public String generateInstruction(AST.expression e){

        if (e instanceof AST.plus){
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
            StringBuilder SB = new StringBuilder();
                String value = getNewlabelName() ;
                String exprvalue = generateInstruction(((AST.neg)e).e1) ;

                SB.append(value).append(" = xor ").append(Bool).append(" ").append(exprvalue).append(" , -1 ") ; //adding xor value with 1 gives complement  
                
                //here -1 is not in Bool type so problem might arise 
                
                //doubt wheher to convert again to i8 or not //
                return value ;
        }
        else if (e instanceof AST.cond) { //change the value as pridicate :

            StringBuilder SB = new StringBuilder() ;
            String predicate = generateInstruction(((AST.cond)e).predicate) ; //gives predicate value
            
            String value = getNewVariableName();
            

            //adding comparison instruction//
            SB.append(Indent+value).append(" = icmp eq ").append(predicate).append(" , 1") ; //dont know whether to keep 1 or true here  
            
            String ifbody = getNewlabelName() ;
            String elsebody = getNewlabelName() ;
            String afterbranch = getNewlabelName() ;

            //adding br instruction in this block//
            SB.append(Indent+"br i1 ").append(value).append(" , label ").append(ifbody).append(" , label ").append(elsebody) ;

            //create label instruction of ifblock ;//
            SB.append("label ").append(ifbody).append(" :\n") ;
            Globals.outFile.println(SB.toString());
            SB.setLength(0);
            String ifbodyexp = generateInstruction(((AST.cond)e).ifbody) ;
            SB.append(Indent+"br label ").append(afterbranch);
            
            //create label for elseblock//
            SB.append("label ").append(elsebody).append(" :\n") ;
            Globals.outFile.println(SB.toString());
            SB.setLength(0);
            String elsebodyexap = generateInstruction(((AST.cond)e).elsebody) ;
            SB.append(Indent+"br label ").append(afterbranch);

            //adding after branch//
            //
            SB.append("label ").append(afterbranch).append(" :");
            Globals.outFile.println(SB.toString());
            return value ;
        }

        else if (e instanceof AST.loop){
            StringBuilder SB = new StringBuilder() ;

            String loopNumber  =  getNewLoopName();

            SB.append("label ").append("forcond"+loopNumber).append(" :\n");
            Globals.outFile.println(SB.toString());
            SB.setLength(0);
            String value = generateInstruction(((AST.loop)e).predicate) ;
            String pvalue = getTruncInst(value,getTypeid(value,0),"i1"); //how to get typeid //
            SB.append(Indent+"br i1 ").append(pvalue).append(" , label ").append("forbody"+loopNumber).append(" , label ").append("forend"+loopNumber) ; //conditional branch
            SB.append("label ").append("forbody"+loopNumber).append(" : \n");
            Globals.outFile.println(SB.toString());
            SB.setLength(0);
            value = generateInstruction(((AST.loop)e).body);
            SB.append(Indent+"br label ").append("forcond"+loopNumber).append("\n");
            SB.append("label ").append("forend"+loopNumber).append(" :");

            Globals.outFile.println(SB.toString());

            return value ;
        }
        else if (e instanceof AST.assign){
            
                //lets say value is pointer assigned to  lefthand side of the assignment in  a<- expr ;
                //
                
                //  StringBuilder SB = new StringBuilder() ;
                //  AST.class_ currClass = Globals.scopeTable.currentClass;   
                //  AST.attr attrib =(AST.attr) Globals.scopeTable.lookUpLocal(currClass.name+((AST.assign)e).name);
                //  String reg = generateGEPInstForAttr(type, Globals.attributeToAddrMap.get(currClass.name)., index) 
                 
                
                //  SB.append(Indent+"store ").append(inT).append(" ").append(a.amount).append(" , ").append(inT).append("* ").append(value).append("\t;storing value\n") ;
                   
                   


        }
        else if (e instanceof AST.new_){
            StringBuilder SB = new StringBuilder() ;
            String addressToStore = getNewVariableName();
            generateAllocaInst(((AST.new_)e).typeid , addressToStore) ; 
           // addressToStore ; //contains address of Object ;
           
            String value = getNewVariableName() ; 
            generateLoadInst(value , ((AST.new_)e).typeid , addressToStore) ;
            return value ;
        }

        else if (e instanceof AST.object){
            AST.ASTNode node = Globals.scopeTable.lookUpGlobal(((AST.object)e).name,AST.attr);
            if (node == null){

            }
            else{
                if (! Globals.isPrimitiveType(((AST.attr)node).typeid)){
                    if ()
                }
            }

            
        }
        else if (e instanceof AST.int_const){ //returns the loaded variable//
            StringBuilder SB = new StringBuilder() ;

            String value = getNewVariableName() ;
            generateAllocaInst("Int",value);
            SB.append(Indent+"store ").append(Int_32).append(" ").append(Integer.toString(((AST.int_const)e).value)).append(" , ").append(Int_32).append("* ").append(value).append("\t;storing value") ;
            Globals.outFile.println(SB.toString());


            return value ;


        }
        else if (e instanceof AST.string_const){
            StringBuilder SB = new StringBuilder() ;

            String value = getNewVariableName() ;
            generateAllocaInst("string",value);
            SB.append(Indent+"store ").append(string).append(" ").append(((AST.string_const)e).value).append(" , ").append(Int_32).append("* ").append(value).append("\t;storing value") ;
            Globals.outFile.println(SB.toString());
            

            return value ;
  
        }
        else if (e instanceof AST.bool_const){
            StringBuilder SB = new StringBuilder() ;
            String value = getNewVariableName() ;
            generateAllocaInst("Bool",value);
            String sol = (((AST.bool_const)e).value == true ) ?  "1" : "0" ;
            SB.append(Indent+"store ").append(Bool).append(" ").append(sol).append(" , ").append(Bool).append("* ").append(value).append("\t;storing value") ;
            Globals.outFile.println(SB.toString());
            
            return value ;

        }
        else if (e instanceof AST.let){
            // no need to implement
        }
        else if (e instanceof AST.static_dispatch){


        }
        else if (e instanceof AST.dispatch){
            // No need to implement
        }
        return null;
    }    

    public String generateAllocaInst( String typeid,String addressToStore){
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+addressToStore + " = alloca " + getTypeid(typeid,0) + ", align " + getAlignment(typeid,0));
        Globals.outFile.println(SB.toString());
        // String last = (num == 0) ? ", "+type + " "+Integer.toString(num) : " ";
        // last = last + ( " ; yields "+type + " * ; pointer\n" );
        return addressToStore;
        
    }
    
    public String generateStruct(AST.class_ cl){

        StringBuilder SB = new StringBuilder();
        String structName = "%class."+cl.name;
        Globals.classNameToStructMap.put(cl.name,structName);
        SB.append(structName + " = " + "type { ");
        boolean anyFeatures = false;
        for (AST.feature f : cl.features){
            if (f instanceof AST.attr){
                SB.append( getTypeid(f.typeid,0));
                SB.append(", ");
                anyFeatures = true;
            }
        }
        if (anyFeatures){
            SB.deleteCharAt(SB.length()-1);
            SB.deleteCharAt(SB.length()-1);
        }
        else{
            SB.append("i8");
        }
        SB.append(" }");
        Globals.outFile.println(SB.toString());
        return structName;

    }
    // returns the mangled class name
    public String getClassMangledName(String className){
        
        StringBuilder SB = new StringBuilder();
        SB.append("_ZN"+className.length()+className);
        
        return SB.toString();
        
    }
    // Returns the mangled function name
    public String getFunctionMangledName(String functionName,String className){
        StringBuilder SB = new StringBuilder();
        SB.append(
            "_ZN"+className.length()+className+functionName.length()+functionName
        );
        return SB.toString();
    }
    // prints the constructor 
    public String generateCtor(AST.class_ cl){
        
        StringBuilder SB = new StringBuilder();
        SB.append(
            "define dso_local void @" +getClassMangledName(cl.name)+
            "("+Globals.classNameToStructMap.get(cl.name)+"* %this){" 
        );
        Globals.classToConstructorMap.put(cl.name,"@"+getClassMangledName(cl.name));
        Globals.outFile.println(SB.toString());
        SB.setLength(0);
        Globals.IRB.generateLabel("entry");
        String allocaReg = generateAllocaInst(cl.name,"%this.addr");
        generateStoreInst(allocaReg ,cl.name , "%this");
        String ptrReg = generateLoadInst("%this1", cl.name, allocaReg);
        
        ClassNode parentClass= Globals.IG.getClassNode(cl).parent;
        // Calling the constructor of the parent class
        if (parentClass!=null){
            String tempReg = generateBitCastInst(ptrReg, cl.name, parentClass.self.name);
            generateCallInst("IO", Globals.classToConstructorMap.get(parentClass.self.name),parentClass.self.name,tempReg);
        }
        

        for (AST.feature f : cl.features){
            if (f instanceof AST.attr){
                //String attrPtr="";
                String attrPtr = generateGEPInstForAttr(cl.name, ptrReg, Globals.attributeToAddrMap.get(cl.name).get((AST.attr)f));
                
                if (((AST.attr)f).value!=null && ! (((AST.attr)f).value instanceof AST.no_expr)){
                    // if expr is not null then   
                    String reg = getNewVariableName();
                    generateLoadInst(reg, ((AST.attr)f).typeid ,generateInstruction(((AST.attr)f).value) ) ;
                    generateStoreInst(attrPtr, ((AST.attr)f).typeid , reg);
                    
                }
                else generateDefaultStore(((AST.attr)f).typeid, attrPtr);
            }
        }
        SB.append(Indent+"ret void\n}");


        //SB.appen
        
        Globals.outFile.println(SB.toString());
        return null;

    }
    public String generateBitCastInst(String value, String from , String to){
        String reg = getNewVariableName();
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+reg).append(" = ").append("bitcast ").append(getTypeid(from, 0));
        SB.append(" ").append(value).append(" to ").append(getTypeid(to, 0));
        Globals.outFile.println(SB.toString());
        return reg;
    }
    public String generateCallInst(String returnType,String func,String type,String params){
        Globals.outFile.println(Indent+"call "+getTypeid(returnType, 0)+" "+func+" ("+getTypeid(type, 0)+" "+params+")");
        return null;
    }

    public String generateDefaultStore(String type,String address){
        if (getTypeid(type,0).equals("i32") || getTypeid(type, 0).equals("i64")){
            generateStoreInst(address, type, "0");
        }
        else if (getTypeid(type, 0).equals("i8")){
            generateStoreInst(address, type, "0");
        }
        else if (getTypeid(type, 0).equals("i8*")){
            // Look for how to give default value to string
        }

        return address;
    }

    public String getTruncInst(String value1 , String type1 , String type2 ) { //converts value one from type1 to type2 ;
        StringBuilder SB = new StringBuilder();
        String    value = getNewVariableName() ;
        SB.append(value + " = trunc "+type1 + " " + value1 + " to " + type2);
        return value ;

    }

    public String getBinaryInstruction(AST.expression e){

        return null;
    }

    public String generateLoadInst(String value , String type , String address){
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+value + " = load " + getTypeid(type,0) + " , " + getTypeid(type,0) +"* " + address + " \t;yields " + type + ":val");
        Globals.outFile.println(SB.toString());
        return value;
         
    }

    public String generateStoreInst(String address , String type , String valueTostore){
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+"store " + getTypeid(type,0) + " " + valueTostore + " , " + getTypeid(type,0)  + "* " + address + "\t ; store instruction if type " + type);
        Globals.outFile.println(SB.toString());
        return address;

    }


    public String generateGEPInstForAttr(String type,String accessAddr,Integer index){
        String value = getNewVariableName();
        StringBuilder SB = new StringBuilder();

        SB.append(Indent).append(value).append(" = ").append("getelementptr inbounds ");
        SB.append(getTypeid(type,0)).append(", ").append(getTypeid(type,0)).append("* ").append(accessAddr).append(", ");
        SB.append("i32 0,").append("i32 ").append(index.toString());

        Globals.outFile.println(SB.toString());

        return value;
    }
    public String generateBranchInst(){

        return null;
    }
    public String generatePHINode(){

        return null;
    }
    public String generateLabel(String labelName){
        String newLabel = labelName;
        if (labelMap.containsKey(labelName)){
            newLabel = labelName+labelNameMaker;
            labelNameMaker++;
            Globals.outFile.println(newLabel+":");
        }   
        else {
            labelMap.put(labelName,1);
            Globals.outFile.println(labelName+":"); 
        }
        return newLabel;
    
    }

    public String generateReturnInst(String type,String addr){
        Globals.outFile.println(Indent+"ret "+getTypeid(type, 0)+" "+addr);
        return null;
    }
    public String generateBasicBlock(){

        return null;
    }   



}



















