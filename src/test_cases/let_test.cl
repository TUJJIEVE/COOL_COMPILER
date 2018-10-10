class Too {
	bb:Int;
	hello():Int{
		{
			bb;
		}
	};
};
class Foo inherits Too{
	bb : Int;
	hello():Int{
		{
			bb;
		}
	};
};
class Main inherits Int{
	a : Foo ;
	b : Too ;
	c : IO ;

	main():Int{
		{
		
			  a@Foo.hello();
			  b@Foo.hello();
			  a@Foo.abort();
			  b@Foo.type_name();
			  a.copy();
			  b.copy();
			  c.copy();



		}
	};

};


