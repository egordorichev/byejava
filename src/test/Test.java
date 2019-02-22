package test;

import java.util.ArrayList;

public class Test<T extends A, C extends D> {
	public void test() {
		ArrayList<T, C> list = new ArrayList<>();
		//test<T, C>();
		// todo: type casts
		// todo: new
	}
}