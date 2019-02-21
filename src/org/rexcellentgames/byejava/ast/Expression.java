package org.rexcellentgames.byejava.ast;

public class Expression {
	public static class Literal extends Expression {
		public Object value;

		public Literal(Object value) {
			this.value = value;
		}
	}
}