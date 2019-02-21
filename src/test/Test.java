package test;

public class Test extends A implements B, C {
	public String getThing(String thing, int a) {
		return thing + -32 / a;
	}

	public void test() {
		getThing("hello", 4);
		test.a = 32;
		b = 19;

		if (true) {
			thing();
		} else if (false) {
			dudeWhatsUp();
		} else {
			mathIsWrong();
		}
	}
}