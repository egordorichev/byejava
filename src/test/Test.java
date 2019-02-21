package test;

public class Test extends A implements B, C {
	public String getThing(String thing, int a) {
		return thing + -32;
	}
}