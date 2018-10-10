class Main {
	hello : Int;
	hello1 : Int;
	hi : String;
	fanna : FOO;
	how : Bool;
	kal : Baa ;
	main():IO {
		{
		new IO.out_string("Hello world!\n");
		}
	};
	maye():Int{
		{
			hello;
		}
	};
	mayor():Int{
		{
			let x : Int <- 2 + 3 in x ;
		}
	};
};

class FOO inherits Main {
	main():IO {
		new IO.out_string("Hello world!\n")
	};
	hello : Bee;
	maye():Int{
		{
			hello;
		}
	};
};





