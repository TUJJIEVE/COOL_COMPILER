
#                                            cool language semantics 
##                                             CS3423: Assignment 2
###                                                                        -cs16btech11029,cs16btech11039
#### ============================================================================================================================
##### contents:
###### 1.Overview.
###### 2.Design Of Semantic Analyzer.
###### 3.Details of Program.
###### 4.explaining testcases.
###### 5.Compiling instructions.
---

#### Overview: 
1. used vistor design pattern for checking semantics 
2. 
3.
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
* the `program ` node is created , then `visitor` Object is created and program node accepts this visitor using the accept function. 
 
**1.Main class**
* Before building graph itself we will check whether `Main` class and whether `main` function is declared in side `Main` class , and ensure such that there exists only one defination for `main()` in **ProgramNode** constructor . Program exits here if there are multiple definations for `Main` classes printing appropriate error messages. 

**2.Inheritance Graph**
* when AST is given to Semantic analyser it will intially form `Inheritance Graph` , in which root will be `Object` class . Inheritance graph is a Array List of type `ClassNode` . `ClassNode` class has 3 attributes `self` of `AST.class_` class , `parent` of `ClassNode` class , `isVisited` of type  `int` . `self` is the corresponding cool class , `parent` is `ClassNode` for the class that `self` inherited from . if there is no inheritance from any class then parent will be null. `isVisted` is used to find CYCLES in Inheritance graph . Cycles checks are done as we build graph.if the graph is **well=formed** we will proceed to next step of semantic analysis.

**3.For Each Nodes in AST**
* Using **`Vistor pattern`** described above , each Node of **AST** is visited .

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

