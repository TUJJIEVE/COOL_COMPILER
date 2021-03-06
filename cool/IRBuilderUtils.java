package cool;

import java.util.*;

import cool.AST;
import cool.Globals;

import java.io.PrintWriter;

public class IRBuilderUtils {

    static final String Int_32 = "i32";
    static final String Bool = "i8";
    static final String string = "i8*";
    static final String Indent = "  ";

    static HashMap<String,Integer> labelMap = new HashMap<String,Integer>();
    static int loopNameMaker = 0 ;
    static int labelNameMaker = 0 ;
    static Integer callNameMaker = 0 ;
    IRBuilderUtils(){


    }    

    static String getNewVariableName(){
        return  "%" + Integer.toString(Globals.currentLocalReg++) ;
    }

    static String getNewLoopName(){
        if(loopNameMaker!=0) return Integer.toString(loopNameMaker++) ;
        else return "";
    }

    static String getNewlabelName(){
        if (labelNameMaker!=0) return Integer.toString(labelNameMaker++) ;
        else return "" ;
    }



    public void generatePreReq(String filename){
        StringBuilder SB = new StringBuilder();
        SB.append("; ModuleID = "+filename +"\n");
        SB.append("source_filename = " + "\"" +filename +"\"\n\n" );
        //SB.append("target datalayout = e-m:e-i64:64-f80:128-n8:16:32:64-S128\n");
        //SB.append("target triple = x86_64");
        SB.append("declare dso_local i32 @printf(i8*, ...)\n\n");
        SB.append("declare dso_local i32 @scanf(i8*, ...)\n\n");
        SB.append("declare dso_local void @exit(i32)\n\n");
        SB.append("declare dso_local i64 @strlen(i8*)\n\n");
        SB.append("declare dso_local i8* @strcat(i8*, i8*)\n\n");
        SB.append("declare dso_local noalias i8* @malloc(i64)\n\n");
        SB.append("declare dso_local i8* @strncpy(i8*, i8*, i64)\n");

        Globals.outFile.println(SB.toString());
        // Also write default functions 
        
    }


    public String getTypeid(String type,int size){
        switch(type){
            case "Bool" : return Bool;
            case "Int"  : return Int_32;
            case "String" : return string;
            case "IO" : return Globals.classNameToStructMap.get(type)+"*";
            case "Object" : return Globals.classNameToStructMap.get(type)+"*";
            case "void" : return "void";
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

    public String generateAllocaInst( String typeid,String addressToStore,Boolean isPtr){
        System.out.println("Generating alloca Instructions");
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+addressToStore + " = alloca " + getTypeid(typeid,0));
        if (!isPtr && !Globals.isPrimitiveType(typeid)) SB.deleteCharAt(SB.length()-1); 
        //SB.append(", align " + getAlignment(typeid,0));
        Globals.outFile.println(SB.toString());
        // String last = (num == 0) ? ", "+type + " "+Integer.toString(num) : " ";
        // last = last + ( " ; yields "+type + " * ; pointer\n" );
        return addressToStore;
        
    }

    public String generateInstForFormalObj(AST.formal f){

        if (Globals.isPrimitiveType(f.typeid)) return generateLoadInst(getNewVariableName(), f.typeid, "%"+f.name+".addr");
        return "%"+f.name+".addr";
    }

    public String generateFromParent(AST.attr node){

        System.out.println("Generate Inst from parent");
        String GEPReg = "%"+node.name;

        if (checkPtrExistance(node.name)) {
            String bitcastReg = generateBitCastInst(Globals.currentClassPtr, Globals.scopeTable.currentClass.name, node.className);
            GEPReg = generateGEPInstForAttr(node.name, node.className, bitcastReg, Globals.attributeToAddrMap.get(node.className).get(node));
        }
        
        if(Globals.isPrimitiveType(node.typeid)) return generateLoadInst(getNewVariableName(), node.typeid, GEPReg);
        return GEPReg;
    }

    public String generateInstForAttrObj(AST.attr node){

        System.out.println("Generate Instructino for attr obj");
        String loadReg = new String();
        System.out.println(node.typeid);
        if (!Globals.isPrimitiveType(node.typeid)){

            if (! Globals.currentClassPtrType.equals(node.typeid)){
                String GEPReg = "%"+node.name;
                if (checkPtrExistance(node.name)) {
                  //String bitcastReg = generateBitCastInst(Globals.currentClassPtr, Globals.scopeTable.currentClass.name, node.typeid);
                    GEPReg = generateGEPInstForAttr(node.name,node.className,Globals.currentClassPtr,Globals.attributeToAddrMap.get(node.className).get(node));
                }
             
                return GEPReg;
            }
            else{
                String GEPReg = "%"+node.name;
                if (checkPtrExistance(node.name)){
                    GEPReg = generateGEPInstForAttr(node.name,node.className,Globals.currentClassPtr,Globals.attributeToAddrMap.get(node.className).get(node));
                }
                return GEPReg;
            }
        }
        else{
            String reg = "%"+node.name;
            if (checkPtrExistance(node.name)) reg = generateGEPInstForAttr(node.name, node.className, "%this1",Globals.attributeToAddrMap.get(node.className).get(node));
            loadReg = generateLoadInst(getNewVariableName(), node.typeid, reg);
            return loadReg;
        }
        
    }

    public String generateStruct(AST.class_ cl){

        System.out.println("Generating structs for class:"+cl.name);
        StringBuilder SB = new StringBuilder();
        String structName = "%class."+cl.name;
        Globals.classNameToStructMap.put(cl.name,structName);
        SB.append(structName + " = " + "type { %class.Object, i8*, ");
        boolean anyFeatures = false;

        ClassNode parentClass = Globals.IG.getClassNode(cl).parent;
        if (parentClass!=null){
            SB.append(getTypeid(parentClass.self.name, 0));
            SB.deleteCharAt(SB.length()-1);  // To remove "*" at the end of the struct name
            SB.append(", ");
            anyFeatures = true;
        }

        for (AST.feature f : cl.features){
            if (f instanceof AST.attr){
                String type = getTypeid(f.typeid,0);
                SB.append(type);
                if (!Globals.isPrimitiveType(f.typeid)) SB.deleteCharAt(SB.length()-1);
                SB.append(", ");
                anyFeatures = true;
            }
        }
        SB.deleteCharAt(SB.length()-1);
        SB.deleteCharAt(SB.length()-1);

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

    public String generateBitCastInst(String value, String from , String to){

        System.out.println("Generating bitcast Instructions");
        String reg = getNewVariableName();
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+reg).append(" = ").append("bitcast ").append(getTypeid(from, 0));
        SB.append(" ").append(value).append(" to ").append(getTypeid(to, 0));
        Globals.outFile.println(SB.toString());
        return reg;
    }
    public String generateCallCtor(String returnType,String func,String type,String params){
     
        System.out.println("Generating call Ctor");
        Globals.outFile.println(Indent+"call "+getTypeid(returnType, 0)+" "+func+" ("+getTypeid(type, 0)+" "+params+")");
        return null;
    }
    public String generateCallInst(String returnType,String func,ArrayList<String> listTypes,ArrayList<String> listParams,String returnReg){
        // To do : see if to store the call instruction in a variable or not
        System.out.println("Generating Call Inst");

        StringBuilder SB = new StringBuilder();
        SB.append(Indent);
        if (! returnReg.equals("") ) SB.append(returnReg).append(" = ");
        SB.append("call ").append(getTypeid(returnType,0)).append(" ").append(func).append(" (");
        
        for (int i=0;i<listTypes.size();i++){
            SB.append(getTypeid(listTypes.get(i),0));
            SB.append(" ");
            SB.append(listParams.get(i));
            SB.append(", ");
        }
        SB.deleteCharAt(SB.length()-1);
        SB.deleteCharAt(SB.length()-1);
        SB.append(")");
        
        Globals.outFile.println(SB.toString());
        return "";
    }
    public String generateDefaultStore(String type,String address){
      
        System.out.println("Generating def store Instructions");
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
       
        System.out.println("Generating trunc Instructions");
        StringBuilder SB = new StringBuilder();
        String    value = getNewVariableName() ;
        SB.append(value + " = trunc "+type1 + " " + value1 + " to " + type2);
        return value ;

    }

    public String getBinaryInstruction(AST.expression e){

        return null;
    }

    public String generateLoadInst(String value , String type , String address){
      
        System.out.println("Generating load Instructions");
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+value + " = load " + getTypeid(type,0) + " , " + getTypeid(type,0) +"* " + address );
        Globals.outFile.println(SB.toString());
        return value;
         
    }

    public String generateStoreInst(String address , String type , String valueTostore){
      
        System.out.println("Generating store Instructions");
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+"store " + getTypeid(type,0) + " " + valueTostore + ", " + getTypeid(type,0)  + "* " + address);
        Globals.outFile.println(SB.toString());
        return address;

    }

    public Boolean checkPtrExistance(String name){
        System.out.println("Checking gep");
        return true;
    }


    public String generateGEPInstForAttr(String name,String type,String accessAddr,Integer index){
      
        System.out.println("Generating gep for class attr Instructions");
        String value = "%"+name;
        StringBuilder SB = new StringBuilder();
       // if (Globals.space > Globals.liveLocalAttrPtr.size()-1) Globals.liveLocalAttrPtr.add(new HashMap<String,Boolean>());
        //if (Globals.liveLocalAttrPtr.get(Globals.space)==null) Globals.liveLocalAttrPtr.add(new HashMap<String,Boolean>());
       // Globals.liveLocalAttrPtr.get(Globals.space).put(name,true);
        if (Globals.liveAttrPtr.get(name)==null) Globals.liveAttrPtr.put(name,1);
        else {value+=Globals.liveAttrPtr.get(name);
        Integer temp = Globals.liveAttrPtr.get(name);
        Globals.liveAttrPtr.remove(name);
        Globals.liveAttrPtr.put(name,temp+1);}
        SB.append(Indent).append(value).append(" = ").append("getelementptr inbounds ");
        SB.append(getTypeid(type,0));
        if (!Globals.isPrimitiveType(type)) SB.deleteCharAt(SB.length()-1);
        SB.append(", ").append(getTypeid(type,0)).append(" ").append(accessAddr).append(", ");
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
      
        System.out.println("Generating return Instructions");
        Globals.outFile.println(Indent+"ret "+getTypeid(type, 0)+" "+addr);
        return null;
    }
    public String generateBasicBlock(){

        return null;
    }
    
    public String generateGlobalString(String s){
        
        String stringReg = "@.str";
        if (Globals.numGlobalStrings>0){
            stringReg+="."+ Globals.numGlobalStrings.toString();
        } 
        Globals.globalStrings.put(s, stringReg);
        Integer sizeOfString = s.length()+1;

        StringBuilder SB = new StringBuilder();
        SB.append(stringReg).append(" = ").append("private unnamed_addr constant");
        SB.append(" [").append(sizeOfString.toString()).append(" x i8]").append(" c");
        SB.append("\"").append(s).append("\\00\"");

        Globals.numGlobalStrings++;
        Globals.outFile.println(SB.toString());

        return stringReg;
    }

    public String generateGEPforString(String addr){
        return "";
    }

    public String generatePrintfCall(String toPrint,String returnReg,String type){
//        %call4 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.2, i32 0, i32 0), i32* %v)
//%call3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.1, i32 0, i32 0), i32 %2)
          
          StringBuilder SB = new StringBuilder();
          SB.append(Indent).append(returnReg).append(" = ").append(" call i32 (i8*, ...) @printf(i8* getelementptr inbounds (");
         
          if (type.equals("Int")) SB.append("[3 x i8], [3 x i8]* ").append(Globals.globalStrings.get("%d"));
          else if(type.equals("String")) SB.append("[3 x i8], [3 x i8]* ").append(Globals.globalStrings.get("%s"));
          else {
            SB.append("["+(toPrint.length()+1) +" x i8], ["+ (toPrint.length()+1) +" x i8]* ").append(Globals.globalStrings.get(toPrint));
          }
          SB.append(", i32 0, i32 0),");
 
          if (type.equals("Int")) SB.append(" i32 ").append(toPrint);
          else if (type.equals("String")) SB.append(" i8* ").append(toPrint);
          else {
              SB.deleteCharAt(SB.length()-1);
          }
          SB.append(")");
          Globals.outFile.println(SB.toString());
          return returnReg;

    }
    public String generateScanfCall(String returnReg,String type){
        System.out.println("generating scanf call");
        StringBuilder SB = new StringBuilder();
        String reg = getNewVariableName();
        reg = generateAllocaInst(type, reg, false);
        SB.append(Indent).append(returnReg).append(" = ").append(" call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* ");
 
        if (type.equals("Int")) SB.append(Globals.globalStrings.get("%d"));
        else SB.append(Globals.globalStrings.get("%s"));
 
        SB.append(", i32 0, i32 0),");
 
        if (type.equals("Int")) SB.append(" i32* ");
        else SB.append(" i8** ");
 
        SB.append(reg).append(")");
        Globals.outFile.println(SB.toString());
    
        if (Globals.isPrimitiveType(type)) return generateLoadInst(getNewVariableName(), type, reg);
        else return reg;

        
    }
    public String generateStrLenCall(String toFind,String returnReg){
        String truncReg = getNewVariableName();
        Globals.outFile.println(Indent+returnReg+" = call i64 @strlen(i8* "+toFind+")");
        Globals.outFile.println(Indent+truncReg+" = trunc i64 "+returnReg+" to i32");
        return truncReg;
    }

    public String generateStrConcatCall(String s1,String s2,String returnReg){
        Globals.outFile.println(Indent+returnReg+" = call i8* @strcat(i8* "+s1+", i8* "+s2+")");
        return returnReg;
    } 
    public String generateSubStrCall(String s ,String i,String len,String returnReg){
        StringBuilder SB = new StringBuilder();
        SB.append(Indent+returnReg).append(" = call i8* @"+getClassMangledName("substr")+"(i8* "+s+", i32 "+i+", i32 "+len+")");
        Globals.outFile.println(SB.toString());
        return returnReg;
    }
    public String generatetypeNameCall(String ptr,String clsName,String returnReg){
        StringBuilder SB = new StringBuilder();
        String reg = generateBitCastInst(ptr, clsName, "Object");
        SB.append(Indent+returnReg).append(" = call i8* @"+getClassMangledName("type_name")+" (%class.Object* "+reg+")");
        Globals.outFile.println(SB.toString());
        return returnReg;
    }
    
    
    public String generateExitCall(){
        // Also have to generate error message
        Globals.outFile.println(Indent+"call void @exit(i32 0)");
        return null;
    }
    public String typeNameCall(String tp){
        return null;
    }
    public void generateMainMethod(){

        Globals.currentLocalReg=0;
        StringBuilder SB = new StringBuilder();
        SB.append("\n; Main Function\n");
        SB.append("define dso_local i32 @main() {");
        SB.append("\nentry:\n");
        //generateLabel("entry");
        SB.append(Indent+"%main = alloca %class.Main\n");
        SB.append(Indent+"call void @"+getClassMangledName("Main")+"(%class.Main* %main)\n");
        if (Globals.mainReturnType.equals("Int")){
            SB.append(Indent+"%retval = call i32 @"+getFunctionMangledName("main", "Main")+"(%class.Main* %main)\n");
            SB.append(Indent+"ret i32 %retval");
        }
        else{
            SB.append(Indent+"%retval = call ").append(getTypeid(Globals.mainReturnType, 0));
            SB.append(" @"+getFunctionMangledName("main", "Main")+"(%class.Main* %main)\n");
            SB.append(Indent+"ret i32 0");
            
        }
        SB.append("\n}");
        Globals.outFile.println(SB.toString());

    }
    public void generatePreStructs(){
        StringBuilder SB = new StringBuilder();
        Globals.classNameToStructMap.put("IO","%class.IO");
        Globals.classNameToStructMap.put("Object","%class.Object");
        SB.append("%class.IO = type { i8 }\n");
        SB.append("%class.Object = type { i8* }");
        Globals.outFile.println(SB.toString());
    }

    public void generatePreConstructs(){
        StringBuilder SB = new StringBuilder();
        Globals.classToConstructorMap.put("IO","@"+getClassMangledName("IO"));
        Globals.classToConstructorMap.put("Object", "@"+getClassMangledName("Object"));
       Globals.currentLocalReg=0;
        SB.append("define dso_local void @"+getClassMangledName("IO")+"(%class.IO* %this) {\n");
        SB.append("entry:\n");
        SB.append(Indent+"%this.addr = alloca %class.IO*\n");
        SB.append(Indent+"store %class.IO* %this, %class.IO** %this.addr\n");
        SB.append(Indent+"%this1 = load %class.IO* , %class.IO** %this.addr\n");
        SB.append(Indent+"ret void\n}");
        
        Globals.currentLocalReg=0;
        SB.append("\n\n");
        SB.append("define dso_local void @"+getClassMangledName("Object")+"(%class.Object* %this, i8* %str) {\n");
        SB.append("entry:\n");
        SB.append(Indent+"%this.addr = alloca %class.Object*\n");
        SB.append(Indent+"store %class.Object* %this, %class.Object** %this.addr\n");
        SB.append(Indent+"%this1 = load %class.Object* , %class.Object** %this.addr\n");
        SB.append(Indent+"%str.addr = alloca i8*\n");
        SB.append(Indent+"store i8* %str, i8** %str.addr\n");
        SB.append(Indent+"%0 = load i8*, i8** %str.addr\n");
        SB.append(Indent+"%name = getelementptr inbounds %class.Object, %class.Object* %this1, i32 0, i32 0\n");
        SB.append(Indent+"store i8* %0, i8** %name\n");
    
        SB.append(Indent+"ret void\n}");

        SB.append("\n\n;Method substr\n");    
        String mName = getClassMangledName("substr");
    
        Globals.currentLocalReg=0;
        SB.append("define i8* @").append(mName).append("(i8* %str , i32 %begind ,i32 %endd) {\nentry:\n");
        String newvar = getNewVariableName();
        SB.append(Indent+newvar).append(" = zext i32 ").append("%endd").append(" to i64\n");
        String mallocvar = getNewVariableName();
        SB.append(Indent+mallocvar).append(" = call noalias i8* @malloc(i64 ").append(newvar).append(")\n");
        String getvar = getNewVariableName();
        SB.append(Indent+getvar).append(" = getelementptr inbounds i8, i8* %str, i32 %begind \n") ;
        String callcopyvar = getNewVariableName();
//        String type = getTypeid("String") ;
        SB.append(Indent+callcopyvar).append(" = call i8* @strncpy(i8* ").append(mallocvar+", i8* "+getvar+", i64 "+newvar).append(")\n"); 
        SB.append(Indent+"ret i8* "+mallocvar);
        SB.append("\n}\n");
        Globals.currentLocalReg=0;
        SB.append("\n\n;Method type_name\n");
        mName = getClassMangledName("type_name");
        String Objtype = "%class.Object";//
        SB.append("define i8* @").append(mName).append("("+Objtype +"* %selff) {\n");
        SB.append("entry:\n");
        String one = getNewVariableName();
        SB.append(Indent+one+" = getelementptr inbounds %class.Object, %class.Object* %selff, i32 0, i32 0\n");
        String two = getNewVariableName();
        SB.append(Indent + two + " = load i8*, i8** ").append(one).append("\n");
        SB.append(Indent+"ret i8* "+two+"\n}\n");
        Globals.currentLocalReg=0;
        Globals.outFile.println(SB.toString());
 
    }

  

}
