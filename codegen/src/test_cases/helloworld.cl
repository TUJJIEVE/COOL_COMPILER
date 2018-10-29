class Main {
	a:Int <- 12;
	b:Int <- 13;
	c:Int <- a+b ;
	hello(x:Int):Int{
		{
			
			2+x;
		}	
	};
	main():IO {
		new IO.out_string("Hello world!\n")
	};
};

class Hi inherits Main{
	d:Int <- 14;
	e:Main;
};
