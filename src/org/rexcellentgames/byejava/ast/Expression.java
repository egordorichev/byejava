package org.rexcellentgames.byejava.ast;

import org.rexcellentgames.byejava.scanner.TokenType;

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

	public static class Variable extends Expression {
		public String name;

		public Variable(String name) {
			this.name = name;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append(this.name);
			return tabs;
		}
	}

	public static class Unary extends Expression {
		public TokenType operator;
		public Expression right;

		public Unary(TokenType operator, Expression right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			switch (this.operator) {
				case MINUS: builder.append('-'); break;
				case BANG: builder.append('!'); break;
			}

			return this.right.emit(builder, tabs);
		}
	}

	public static class Binary extends Expression {
		public TokenType operator;
		public Expression left;
		public Expression right;

		public Binary(TokenType operator, Expression left, Expression right) {
			this.operator = operator;
			this.left = left;
			this.right = right;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			tabs = this.left.emit(builder, tabs);

			switch (this.operator) {
				case MINUS: builder.append(" - "); break;
				case PLUS: builder.append(" + "); break;
				case SLASH: builder.append(" / "); break;
				case STAR: builder.append(" * "); break;
				case PERCENT: builder.append(" % "); break;
			}

			return this.right.emit(builder, tabs);
		}
	}

	public static class Get extends Expression {
		public Expression from;
		public String field;

		public Get(Expression from, String field) {
			this.from = from;
			this.field = field;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			tabs = this.from.emit(builder, tabs);
			builder.append('.').append(this.field);

			return tabs;
		}
	}

	public static class Set extends Expression {
		public Expression to;
		public Expression from;
		public String field;

		public Set(Expression from, Expression to, String field) {
			this.from = from;
			this.to = to;
			this.field = field;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			tabs = this.from.emit(builder, tabs);
			builder.append('.');
			tabs = this.to.emit(builder, tabs);
			builder.append(" = ").append(this.field);

			return tabs;
		}
	}

	public static class Assign extends Expression {
		public Expression to;
		public String field;

		public Assign(Expression to, String field) {
			this.to = to;
			this.field = field;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			tabs = this.to.emit(builder, tabs);
			builder.append(" = ").append(this.field);

			return tabs;
		}
	}
}