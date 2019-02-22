package test;

public class Test {
	public void test() {
		switch (10) {
			case 1:
			case 2: {
				test();
				break;
			}

			default:
			case 32: testa();
		}
	}
}