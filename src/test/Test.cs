namespace test {
	public class Test : A, B, C {
		public String getThing(String thing, int a) {
			return thing + -32 / a;
		}

		public void test() {
			getThing("hello", 4);
			test.a = 32;
			b = 19;
		}
	}
}

