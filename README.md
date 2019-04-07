
#                      FOR  cool language semantics 
##                        
###                                                 -cs16btech11029,cs16btech11039
#### =========================================================================
##### contents:
###### 1.Overview.
###### 2.Design Of Semantic Analyzer.
###### 3.Details of Program.
###### 4.explaining testcases.
###### 5.Compiling instructions.
---

#### Overview: 
1. Used vistor design pattern for checking semantics 
2. Created and Checked the inheritance Graph 
3. Made 2 passes to the AST for complete type checking 
#### Design Of Semantic Analyzer.
###### ABOUT VISITOR DESIGN PATTERN :
* Visitor design pattern is one of the behavioral design pattern. It is used when we have to perform an operation on a group of similar kind of Objects. With the help of visitor pattern, we can move the operational logic from the objects to another class.
* There are 2 interfaces in the program one is `ASTnode` and `ASTvistor` .
* The Concrete Visitor class  implements the methods provided by the ASTvisitor.
* There are 3 classes ProgramNode ,CNode , ExpressionNode and FeatureNode which implements the methods provided by the ASTnode class.
* The ASTnode interface has a virtual accept function which accepts the object of ASTvisitor.
* The Program Node class overrides the accepts function. The program node has an ArrayList of CNode objects which hepls in doing the Depth first search of the Classes present in the program.
* The CNode class overrides the accept function. The CNode has ArrayList of Feature Node which helps in doing the Depth first search in the features present in the Class,
* At the start of the Semantic analysis an ASTvisitor Object is created which visits each ASTnode starting from the Program Node. Each Class that implements the ASTnode interface accepts this visitor.
* There are 4 methods with same name as `visit` but it's parameters are different :
```java
 interface ASTvisitor {
      public boolean visit(ProgramNode P);
      public boolean visit(CNode C);
      public boolean visit(FeatureNode F);
      public boolean visit(ExpressionNode E);
  }
  ```
* Each of the visit function is executed depending upon which node is currently being visited.
* The visit function when visiting the CNode inserts the class namespace in the Scope Table .
* The visit function when visiting the ProgramNode checks the inheritance graph and wether there are multiple Main() functions or not. 
* The visit function when visiting the FeatureNode checks wether the features in a class are overriden or not . And it also checks if there is redefinition of a feature in the same class. (These checks happen only in the second pass when the Class namespaces for all the classes in the program is created and stored in the Scope Table.)
* The visit function when visiting the ExpressionNode does the type checking by calling the getType function.
* The type checking is done in the getType function which takes an AST.expression object and depending upon which class it belongs does the necessary type checking 
#### Details of Program.
**0.StartPoint**
* the `ProgramNode`is created , then `visitor` Object of ASTvisitor class is created and program node accepts this visitor using the accept function. 
 
**1.Main class**
* Before building graph itself we will check whether `Main` class and whether `main` function is declared in side `Main` class , and ensure such that there exists only one defination for `main()` in **ProgramNode** constructor . Program exits here if there are multiple definations for `Main` classes printing appropriate error messages. 
* Also if there is any class that inherits from Bool, Int, String then error is printed and semantic is aborted.

**2.Inheritance Graph**
* when AST is given to Semantic analyser it will intially form `Inheritance Graph` , in which root will be `Object` class . Inheritance graph is a Array List of type `ClassNode` . `ClassNode` class has 3 attributes `self` of `AST.class_` class , `parent` of `ClassNode` class , `isVisited` of type  `int` . `self` is the corresponding cool class , `parent` is `ClassNode` for the class that `self` inherited from . if there is no inheritance from any class then parent will be null. `isVisted` is used to find CYCLES in Inheritance graph . Cycles checks are done as we build graph.if the graph is **well=formed** we will proceed to next step of semantic analysis.
* If any cycle is detected then message is printed and semantic is aborted.

**3.For Each Nodes in AST**
* Using **`Vistor pattern`** described above , each Node of **AST** is visited .
* For each Expression node getType function is called which does the entire type checking and also returns the expression Type.
* For each feature node various checks is performed like formal parameter validation,
overriden methods etc..
* For each class node class scope is created in the scope table and inbuilt functions are inserted in the topmost scope.
* For each Program node it's inheritance graph is constructed and validated.

**4.Scope cum Symbol Table**
* The scope table is a `HashMap` with `ClassScope` object as key and `Arraylist` of Hash map as value. **HashMap`<`*ClassScope*,*ArrayList*`<`HashMap`<`String,AST.ASTNode`>>>`**
* ClassScope is a Class which has `AST.class_` , `scope` members.
```java
      class ClassScope{
           public AST.class_ cl;
           public int scope;
   
           public ClassScope(AST.class_ c){
              this.cl = c;
              this.scope = 0;
          }
      }
```
* For each class there is a `ArrayList` maintained . In each index of  arraylist a `HashMap` is maintained. The indices of the `Arraylist` represent the scope of the program in a particular class. The `HashMap` has name of `features` as key and its `ASTnode` as value .
* All the inbuilt functions , features of classes are stored in the outermost scope with scope value 0. 
* `insertClass` funtion is used to insert class in Scopetable . this will create a new object adds to existing hashTable.
* `serchTable` function is used to search in Hashtable.iterates over the Hashvalues to match key if key is matched it returns value otherwise it returns null.
* `enterScope` function for entering in a new scope .
* `exitScope` function used for exiting Scope.
* `lookUpClassSpace` function used for finding identifier in the class Scope .
* `lookUpLocal` function used to identify Local Objects.
* `lookUpGlobal` functions used to identify Global Objects.



**5.Type Checking**
* *`TypeChecking`* : type checks have been done according to  [**section`12`**](https://theory.stanford.edu/~aiken/software/cool/cool-manual.pdf)   in cool mannual . When type checking results in ***errors*** then its *StaticType* its type is assigned as `Object` Type and further Typechecking takes place. these checks are mostly done by calling **`getType()`** function . 

* *****Details in Typechecking*****
    * `constants` type should be `Int` or `String` or `Bool` .
    * `identifiers` type should be predefined , we can get them accessing             *`ScopeTable`* . if an identifier is not found it will print error.
    * `assignment` **id<-expr**  : the only condition for assign is that Type of expr should be subtype for type of id .
    * `Subtype:`let say Class A inherits B and B inherits C then :
      * A is `subtype` of A
      * A is `subtype` of B 
      * A is `subtype` of C 
    *  we assign static type of `new Type` as static type of `expr` if the expr Type is valid i.e if that Class is present otherwise we assign 'new Type' to `Object`.
    * in other expressions such as `isvoid`,`loop` their static type are fixed like `isvoid` type is always **Bool** , `loop` type is always **Object** .
    * in **Arithmatic,comparison,~** operations we need all operands  should be of type **Int**
    * in `let` expr the type checking will be done between let.value and let.typeid and Static type of let will return the type of let expression body.
    * in `case` expression we will check is there a possibility that type doesn't match with the provided types then a `warning` is issued , as it is calculated in runtime , and type of case expression will be **join** of all types of branch expressions . i.e least parent class for all the branches present in case .
    * in case of `dispatch` expressions :
        * `<id>(e1,e2....en)` : search for <id> method in this and its parent classes if found then its valid expression otherwise its an **error**
        * `<id>(e1,e2...en)` **~** `self.<id>(e1,e2....en)` 
        * `expr.<id>(e1,e2...en)`:search for <id> method in **typeOf**(<id>) and its parents class if found then its valid expression otherwise its an **error** .
        * `expr@Type.<id>(e1,e2,...en)` : typeOf(expr) should be subtype for `Type` and `id` should be present in Type class , other wise its an **error**

*

#### Explaining testcases.
* there are 5 test cases that check almost all of the semantics except semantics corresponding to *SELF_TYPE* .Some of the Sematic checks were 
    * forming correct inheritance graph 
        * **error** if cycle is found
    * no of Main classes and main() functions 
        * only One `Main` class and it should contain `main` method .**error** otherwise.
    * calling proper functions i.e `Dispatches` all kinds of function calls are checked in one of the test cases like 
       * self@IO.out_string("bla bla ..."); => should not give **error**
       * self@Int.out_string("........."); => gives **error**
    * gives **error** when used undeclared identifiers are used 
    * gives **error** when inherited from `Bool`,`Int`,`String` . 
    etc..
         
          
        
           
#### Compiling Instructions.
Open `semantic` named folder in  :
``` 
       make
```
Open `semantic/src/java` :
```
        ./semantic <filename>.cl
```


#                       FOR cool language CodeGeneration
##                        
###                                                 -cs16btech11029,cs16btech11039
#### ==========================================================================================================
##### contents:
###### 1.Overview.
###### 2.Design Of CodeGenerator.
###### 3.Details in CodeGeneration.
###### 4.explaining testcases.
###### 5.Compiling instructions.
---
#### 1.Overview . 
    1. used VisitorDesign Pattern for codegenereration 
    2. parsed AST and generated LLVM IR in a Bottom UP approach . 
    3 . Runtime checks were done for "Divide by Zero" , "Static Dispatch on void"

#### 2.Design of Codegenerator
###### ABOUT VISITOR DESIGN PATTERN :
* **Visitor design pattern** is one of the behavioral design pattern. It is used when we have to perform an operation on a group of similar kind of Objects. With the help of visitor pattern, we can move the operational logic from the objects to another class.
* There are 2 interface  in the Design one is `CoolParserVisitor` and `Visitor`. where `CoolParserVisitor` is provided by **default** other is added.
* The `ConcreteVisitor` class implements the methods provided by the `Visitor`
* There are 6 methods with same name as `visit` in **Visitor**class but it's parameters are different :
```java
interface Visitor{
	public void visit(AST.program program);
	public void visit(AST.class_ cl);
	public void visit(AST.attr attribute);
	public void visit(AST.method method);
	public void visit(AST.formal params);
	public String visit(AST.expression exp);
}
  ```
 * `IRBuildersUtils` class contains methods that are utilised in `IRBuilder` class
 * `IRBuilder` generates Instructions according to expression type .
 *  `Globals` contains all the global variables used while creating instructions i.e hashmaps , Strings , ClassNames etc 
 *  `ConcreteVisitor` class takes care about creating LLVM IR by visiting the program, class and method node. First the visitor visits the program node which sets up all the default strings , functions needed , it also sets up the inheritance graph and fills the scope table .It then creates the struct of the classes and the constructors of the classes . It creates it such that the parent class's struct is created first . 
 *  It then for each class in the program it runs `visit(class)` which for each method visits(method).
 *  In the visit (method) method the alloca and the store instruction for the parameters passed is generated and then it generates the instructions for the body by visiting the body.
 *  when a AST.expr is found `visit(expr)` generates IR using `IRBuilder.generateInstruction` . This function recursively creates the needed instructions by using the helper functions present in it's parent class i.e `IRBuilderUtils`.
 *  all Strings type objects in cool files are stored in global i8* pointers. The strings are obtained in the semantic analysis phase.
 *  All the classes in cool inherit from Object class. So for each class in the cool program the struct will be like :
 ``` 
    %class.<class_name> = type { %class.Object, i8* }
    The first param is the Class Object and second param is a string which 
    stores the class name.
```
 * This helps in printing the type_name of the object.
 * All the strings that are collected by the semantic analyzer are declared as global constants at the top of the .ll file
 * The constructor of Object class and IO class is there in every .ll file
 * For division by zero checking, a function named checkDivZero is there in every .ll file for checking the error.
 * For checking the static dispatch on void if-else branching is used.
 * The semantic.java class has been modified
 * The scopeTable.java class has been modified

### NOTE : PLEASE USE THE SEMANTIC ANALYZER THAT IS MADE BY US . ALSO PLEASE USE ALL THE FILES THAT WE HAVE GIVEN.

#### 3.Details in CodeGeneration 
###### **GOAL :  code generation from cool Annoted AST to llvm IR**
**1.primitive**
a language has same power(computational) as C++,Java.. if it has there 3 elements .
```
    Memory 
    Branching instructions 
    Loops / recursions 
```
Intially ,we will Look into Memory  aspect of LLVM IR and Cool
* MEMORY
    ***Cool*** handles memory using new  keyword which allocates that memory and it allows to use variables too . Cool doesn’t distingush between TYPE and a Class i.e each Class is a Type in cool Language . we can declare variables of user defined types or inbuilt classes such as “Int , String , Bool “ .
    ***LLVM***:We have to see how a dataType is defined in LLVM , and convert COOL type to corresponding w in LLVM a dataType is either a pointer to some dataType  , iN (N belongs to natural numbers) and a struct (user defined datatype)
    classes are converted to corresponding struct 
    Eg : 
    This class in `cool` 
    ``` 
    Class A {
    s  : String <- “this” ;
    i  : Int <- 3 ;
    c : Bool <- true ;
    Fooo : Int {
    3 
    }
    };
    ```  
    Is converted to  `LLVM IR` as
    ```
    ;The struct for A
    %class.A = type { %class.Object, i8*, i8*, i32, i8 }
    ```
    ***Here the first 2 attributes of all the Structs will be same 1st attribute is `%class.Object` and 2nd attribute is `i8`\* which points to memory address that stores actual class name , 3rd attribute , 4th attribute , 5 attribute are pointers to respective members in cool Class declaration***
    Cool doesnt contain arrays or pointers or char or structs as we see in other object oriented launguages such as c++ or java etc . But we Cool has strings  as continous memory allocation To handle String we have used the same ways as clang used to store c++ strings  i.e storing a i8 pointer and allocating a continous memory to it using alloca instruction in llvm .
    `LLVM IR` doesnt provide `Bool` as an seperate datatype we have to use i8 as default for Bool . i.e, when its true i8 stores 1 and for false we have taken 0 .Because as LLVM doesnot provide a direct complement instruction on integers but provides a xor operation which can be used to find complement when value xor,ed with  -1 .
    **member functions of all classes are global and those will not be present in struct**
    
* BRANCHING
    LLVM IR basically is modulue , which is a set of BasicBlocks .
    When there is Branching instruction that means we have to shift to a BasicBlock with out executing other BasicBlock(Instructions)
    LLVM IR consists 2 ways of branching 
    **Conditional branching** 
    **Unconditional branching**
    But cool provides only Conditional Branching like
    ```
    If exp1 then exp2 else exp3 fi
    ```
    This is converted to a Conditional Braching instruction in LLVM and use unconditional branch instruction to directly jump to next instruction after if else block: 

    ```
    ...
    %cmp = //i1 value that stores 1 or 0 corresponding to true or false based on exp1
    br i1 %cmp , label bb1  , label bb2 
    bb1 :
        //contains exp1
        br label ifend
    bb2:
        //contains exp2
        br label ifend
    label ifend :
    ...
    ```
    
* LOOPS/RECURSION :
    LLVM provides recursion as just other Languges provides i.e we can call an llvm ir function with in same function.
    In case of Loops ,`Cool` Provides only one way to keep loop i.e
    ```
    while expr loop expr1 pool 
    ```
    I.e when expr is true then expr1 should run else it should not run ,IN `LLVM` its converted as following 
    ```
    label condBlock : 
        %val = //conerting expr and storing its value
        %cmp = icmp i1 %val , 1 
        br  i1 %cmp bodyBlock , exitBlock 
    label bodyBlock :
        …
        //converting expr1 
        br label condBlock 
    label exitBlock : 
    …
    //has converted expressions after loop in cool
    ```
**2 . How we handled Other expressions of Cool in LLVM**
   * ***ID<-expr***
       ```
        %val = expr value 
        ;finding the pointer of that variable i.e ID 
        %ptr = getelementptr … .
        ;Type can be found by using hashtable
        store Type  %val , %ptr
       ```
   * ***function calls***
        only static dispatches are handled 
        **expr@type.func(params)**
        ```
            ;if func is a default function provided by llvm use it directly 
            ;eg : length in substr
            ;other wise , generate that functions and using llvm 'call' inst
            %val = generateInstructions(expr)
            mangledname = getMangledname(func,type)
            call mangledname(%val);
        ```
    * ***Block expressions*** 
        **{ [expr;]+ }**
        ```
            ;generate for each expr in block
        ```
    * ***new TYPE*** 
        ```
            type = getTypeid(TYPE)
            %val = alloca type 
            ; if TYPE is primitive(i.e Int , Bool , String) 
            ; then %loadval =  load %val
            ; else %val 
        ```
    * ***isvoid expr***
        ```
            ;if expr.type is primitive return false i.e , 0
            ;else
            %val = generateinstuctions for expr
            %ptr = getelementptr ..
            mtype = gettypeid(type,0)
            %ret = icmp ne mtype %ptr , null 
        ```
    * ***Arthimatic and comparision expressions in Cool***
        ```
            ;For both arithamatic and comparision LLVM has instructions
            ;eg : in cool lets say i have an expr 2 + x where x is a variable
            ;we convert as
            %x12 = load from %x
            %summ = add i32 2 , %a
        ```
    * ***~ expr***
        belongs to AST.neg class => Boolean complement
        ```
            %val = generate instructions for expr
            %rett = xor i8 %val , 1 
        ```
    * ***not expr***
        belongs to AST.comp class => 1's complement
        ```
            %val = generate instructions for expr
            %rett = xor i32 %val , -1 
        ```
    * ***Bool_const***
        ```
        ; if the value is true then we return "1"
        ; else return "0"
        ; these value as printed in IR 
        ```
    * ***Int_const***
        ```
        ; find the value of Int and return it as String 
        ```
    * ***String_const***
        ```
        
        ```
    * ***ID***
        ```
        
        ```
**3.How Functions are generated**
* The formal parameters of the function are first allocated statck space by using the `alloca` instruction of the LLVM IR and then the formal parameters are stored in this location using `store` instruction.
* These formal parameters are then loaded using `load` instruction into a local register.
* After this process the `generateInstruction` method in the `IRBuilder` class is called to generate instructions for the body of the function.
* The `generateInstruction` method returns the register into which the returned value is stored.
* This returned value type is checked with the return type of the function . If it doesn't match then it does BitCast Operation by calling `genereateBitCastInst`. It is done since the returned value by the generateInstruction is a subtype of the return type of the function (this is checked in the semantic phase).
* Then it returns the register.

**4.How default functions are handled**
* The `in_int, in_str` methods are handled using the `scanf` call . The instruction generated is same as that generated by Clang on compiling .cpp files.
* The `out_int , out_str` methods are handled using the `printf` call. The instruction generated is same as that generated by Clang on compiling .cpp files.
* The `length` method is handled using `strlen` call .
* The `concat` method is handled using `strcat` call.
* The `abort` method is handled using `exit` call.
* The `type_name` method is handled by just printing the class name which is present as global string.
* The `substr` by using `strncpy` function.

#### 4.explaining testcases 
*   arthimatic_binary.cl :
    ```
        test verifies :
            all arthimatic operations  
            < , <= , = operators
            assignment expression of primitive types
            Block expression 
            main method ,  Main Class 
    ```
* staticDispatch_inheritance.cl
    ```
        test verifies :
            static Dispatch
            inheritance
            assignment of non primitive types
            assignment of child classes object to Parent class Object
    ```
* nestedIf_loops.cl
    ```
        test verifies : 
            nested if else conditions on various expressions 
            checks the formation of blocks in Loops and conditions
    ```
#### 5.Compiling Instructions.
Open `codegen` named folder in  :
``` 
       make
```
Open `codegen/src/java` :
```
        ./codegen <filename>.cl
```


