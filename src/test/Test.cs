using java.util;

namespace test {
	public class Test<T> where T : A {
		public C test<C> () {
			ArrayList<T, C> list = new ArrayList<>();

			test<T, C>();
		}
	}
}
