package org.rexcellentgames.byejava.ast;

public class Expression extends Ast {
	public static class Literal extends Expression {
		public Object value;

		public Literal(Object value) {
			this.value = value;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append(stringify(value));
			return tabs;
		}

		private String stringify(Object object) {
			if (object == null) {
				return "null";
			}

			if (object instanceof Double) {
				String text = object.toString();

				if (text.endsWith(".0")) {
					text = text.substring(0, text.length() - 2);
				}

				return text;
			}

			return object.toString();
		}
	}
}