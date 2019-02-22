package org.rexcellentgames.byejava.ast;

import org.rexcellentgames.byejava.scanner.TokenType;

import java.util.ArrayList;

public class Expression extends Ast {
	public static class Literal extends Expression {
		public Object value;

		public Literal(Object value) {
			this.value = value;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append(value);
			return tabs;
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

	public static class Deunary extends Expression {
		public Expression left;
		public TokenType operator;

		public Deunary(TokenType operator, Expression left) {
			this.operator = operator;
			this.left = left;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			tabs = this.left.emit(builder, tabs);

			switch (this.operator) {
				case MINUS_MINUS: builder.append("--"); break;
				case PLUS_PLUS: builder.append("++"); break;
			}

			return tabs;
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
				case EQUAL_EQUAL: builder.append(" == "); break;
				case BANG_EQUAL: builder.append(" != "); break;
				case LESS_EQUAL: builder.append(" <= "); break;
				case GREATER_EQUAL: builder.append(" >= "); break;
				case GREATER: builder.append(" > "); break;
				case LESS: builder.append(" < "); break;
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
			builder.append('.').append(this.field).append(" = ");
			tabs = this.to.emit(builder, tabs);
			return tabs;
		}
	}

	public static class Assign extends Expression {
		public Expression to;
		public Expression value;

		public Assign(Expression to, Expression value) {
			this.to = to;
			this.value = value;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			tabs = this.to.emit(builder, tabs);
			builder.append(" = ");

			return this.value.emit(builder, tabs);
		}
	}

	public static class Call extends Expression {
		public Expression callee;
		public ArrayList<Expression> arguments;

		public Call(Expression callee, ArrayList<Expression> arguments) {
			this.callee = callee;
			this.arguments = arguments;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			tabs = this.callee.emit(builder, tabs);
			builder.append('(');

			if (this.arguments != null) {
				for (int i = 0; i < this.arguments.size(); i++) {
					this.arguments.get(i).emit(builder, tabs);

					if (i < this.arguments.size() - 1) {
						builder.append(", ");
					}
				}
			}

			builder.append(')');
			return tabs;
		}
	}

	public static class Grouping extends Expression {
		public Expression expression;

		public Grouping(Expression expression) {
			this.expression = expression;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append('(');
			tabs = this.expression.emit(builder, tabs);
			builder.append(')');

			return tabs;
		}
	}

	public static class Index extends Expression {
		public Expression from;
		public Expression index;

		public Index(Expression from, Expression index) {
			this.from = from;
			this.index = index;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			this.from.emit(builder, tabs);
			builder.append('[');
			this.index.emit(builder, tabs);
			builder.append(']');

			return tabs;
		}
	}
}