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
		public ArrayList<Generetic> generetics;
		public boolean construct;
		public boolean array;

		public Variable(String name, ArrayList<Generetic> generetics, boolean construct) {
			this.name = name;
			this.generetics = generetics;
			this.construct = construct;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			if (this.construct) {
				builder.append("new ");
			}

			builder.append(this.name);

			if (this.generetics != null) {
				builder.append('<');

				for (int i = 0; i < this.generetics.size(); i++) {
					builder.append(this.generetics.get(i).name);

					if (i < this.generetics.size() - 1) {
						builder.append(", ");
					}
				}

				builder.append('>');
			}

			if (this.array) {
				builder.append("[]");
			}

			return tabs;
		}

		@Override
		public void rename() {
			this.name = this.checkType(this.name);
			this.checkTypes(this.generetics);
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

		@Override
		public void rename() {
			this.right.rename();
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

		@Override
		public void rename() {
			this.left.rename();
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
				case AND: builder.append(" && "); break;
				case OR: builder.append(" || "); break;
				case MINUS: builder.append(" - "); break;
				case PLUS: builder.append(" + "); break;
				case SLASH: builder.append(" / "); break;
				case STAR: builder.append(" * "); break;
				case PERCENT_EQUAL: builder.append(" % "); break;
				case MINUS_EQUAL: builder.append(" -= "); break;
				case PLUS_EQUAL: builder.append(" += "); break;
				case SLASH_EQUAL: builder.append(" /= "); break;
				case STAR_EQUAL: builder.append(" *= "); break;
				case PERCENT: builder.append(" % "); break;
				case EQUAL_EQUAL: builder.append(" == "); break;
				case BANG_EQUAL: builder.append(" != "); break;
				case LESS_EQUAL: builder.append(" <= "); break;
				case GREATER_EQUAL: builder.append(" >= "); break;
				case GREATER: builder.append(" > "); break;
				case LESS: builder.append(" < "); break;
				case INSTANCEOF: builder.append(" is "); break;
			}

			return this.right.emit(builder, tabs);
		}

		@Override
		public void rename() {
			this.right.rename();
			this.left.rename();
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

		@Override
		public void rename() {
			this.from.rename();
			this.field = this.updateName(this.field);
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

		@Override
		public void rename() {
			this.to.rename();
			this.value.rename();
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

		@Override
		public void rename() {
			this.callee.rename();
			this.rename(this.arguments);
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

		@Override
		public void rename() {
			this.expression.rename();
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

		@Override
		public void rename() {
			this.from.rename();
			this.index.rename();
		}
	}

	public static class This extends Expression {
		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append("this");
			return tabs;
		}
	}

	public static class Super extends Expression {
		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append("base");
			return tabs;
		}
	}

	public static class If extends Expression {
		public Expression condition;
		public Expression ifBranch;
		public Expression elseBranch;

		public If(Expression condition, Expression ifBranch, Expression elseBranch) {
			this.condition = condition;
			this.ifBranch = ifBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			this.condition.emit(builder, tabs);
			builder.append(" ? ");
			this.ifBranch.emit(builder, tabs);
			builder.append(" : ");
			this.elseBranch.emit(builder, tabs);

			return tabs;
		}

		@Override
		public void rename() {
			this.condition.rename();
			this.ifBranch.rename();
			this.elseBranch.rename();
		}
	}

	public static class Cast extends Expression {
		public String type;
		public Expression expression;

		public Cast(String type, Expression expression) {
			this.type = type;
			this.expression = expression;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append('(').append(this.type).append(") ");
			return this.expression.emit(builder, tabs);
		}

		@Override
		public void rename() {
			this.type = this.checkType(this.type);
			this.expression.rename();
		}
	}

	public static class Array extends Expression {
		public ArrayList<Expression> values;

		public Array(ArrayList<Expression> values) {
			this.values = values;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append('{');

			if (this.values != null) {
				builder.append(' ');

				for (int i = 0; i < this.values.size(); i++) {
					this.values.get(i).emit(builder, 0);

					if (i < this.values.size() - 1) {
						builder.append(", ");
					} else {
						builder.append(' ');
					}
				}
			}

			builder.append('}');

			return tabs;
		}

		@Override
		public void rename() {
			this.rename(this.values);
		}
	}
}