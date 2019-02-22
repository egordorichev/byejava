package test;

public class Test {
	static {
		a = 32;
	}

	{
		c = 32;
	}

	{
		d = 32;
	}

	public Test() {
		// This is a comment
	}

	public Test(int a) {
		this.a = a;

		/*
		 * This is also a comment
		 */
		thing.a = 32;
	}
}