class Foo {
    f (a : Int): Int {{
        new IO.out_string("this is Foo");
        a;
    }};
    s():String{
        "i am a string"
    };
};
class Too inherits Foo {
    f(a : Int):Int{{
        new IO.out_string("This is Too");
        a;
    }};
};
class Main {
    a : Too ;
    main() : IO{{
        a.s();
        new IO.out_string("hello");
        new IO;    
   }};
};
