package test;

import java.util.ArrayList;

public class Test<T extends A> {
	public <C> C test() {
		ArrayList<T, C> list = new ArrayList<>();
		test<T, C>();
		// todo: type casts
	}
}