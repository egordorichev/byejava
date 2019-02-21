package org.rexcellentgames.byejava.ast;

import java.util.ArrayList;
import java.util.HashMap;

public class Statement extends Ast {
	public static class Expr extends Statement {
		public Expression expression;

		public Expr(Expression expression) {
			this.expression = expression;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			tabs = this.expression.emit(builder, tabs);
			builder.append(";\n");

			return tabs;
		}
	}

	public static class Package extends Statement {
		public String name;

		public Package(String name) {
			this.name = name;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append("namespace ").append(this.name).append(" {\n");
			return tabs + 1;
		}

		@Override
		public int emitEnd(StringBuilder builder, int tabs) {
			tabs--;
			builder.append("}\n");

			return tabs;
		}
	}

	public static class Class extends Statement {
		public String name;
		public String base;
		public ArrayList<String> implementations;
		public ArrayList<Field> fields;
		public ArrayList<Method> methods;
		public ArrayList<Statement> inner;
		public Modifier modifier;

		public Class(String name, String base, ArrayList<String> implementations, ArrayList<Field> fields, Modifier modifier, ArrayList<Method> methods, ArrayList<Statement> inner) {
			this.name = name;
			this.base = base;
			this.implementations = implementations;
			this.fields = fields;
			this.modifier = modifier;
			this.inner = inner;
			this.methods = methods;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append(this.modifier.access.toString().toLowerCase()).append(' ');

			if (this.modifier.isAbstract) {
				builder.append("abstract ");
			}

			if (this.modifier.isFinal) {
				builder.append("const ");
			} else if (this.modifier.isStatic) {
				builder.append("static ");
			}

			builder.append("class ").append(this.name);

			if (this.base != null || this.implementations != null) {
				builder.append(" : ");

				if (this.base != null) {
					builder.append(this.base);

					if (this.implementations != null) {
						builder.append(", ");
					}
				}

				if (this.implementations != null) {
					for (int i = 0; i < this.implementations.size(); i++) {
						builder.append(this.implementations.get(i));

						if (i < this.implementations.size() - 1) {
							builder.append(", ");
						}
					}
				}
			}

			builder.append(" {\n");
			tabs++;

			if (this.inner == null && this.fields == null && this.methods == null) {
				builder.append('\n');
			} else {
				if (this.inner != null) {
					for (int i = 0; i < this.inner.size(); i++) {
						tabs = this.inner.get(i).emit(builder, tabs);

						if (this.fields != null || this.methods != null || i < this.inner.size() - 1) {
							builder.append('\n');
						}
					}
				}

				if (this.fields != null) {
					for (int i = 0; i < this.fields.size(); i++) {
						tabs = this.fields.get(i).emit(builder, tabs);

						if (this.methods != null || i < this.fields.size() - 1) {
							builder.append('\n');
						}
					}
				}

				if (this.methods != null) {
					for (int i = 0; i < this.methods.size(); i++) {
						tabs = this.methods.get(i).emit(builder, tabs);

						if (i < this.methods.size() - 1) {
							builder.append('\n');
						}
					}
				}
			}

			tabs--;

			indent(builder, tabs);
			builder.append("}\n");

			return tabs;
		}
	}

	public static class Field extends Statement {
		public String name;
		public Expression init;
		public String type;
		public Modifier modifier;

		public Field(Expression init, String type, String name, Modifier modifier) {
			this.init = init;
			this.type = type;
			this.name = name;
			this.modifier = modifier;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);

			builder.append(this.modifier.access.toString().toLowerCase()).append(' ');

			if (this.modifier.isAbstract) {
				builder.append("abstract ");
			}

			if (this.modifier.isFinal) {
				builder.append("const ");
			} else if (this.modifier.isStatic) {
				builder.append("static ");
			}

			builder.append(this.type).append(' ').append(this.name);

			return this.end(builder, tabs);
		}

		protected int end(StringBuilder builder, int tabs) {
			if (this.init != null) {
				builder.append(" = ");
				tabs = this.init.emit(builder, tabs);
			}

			builder.append(";\n");
			return tabs;
		}
	}

	public static class Block extends Statement {
		public ArrayList<Statement> statements;

		public Block(ArrayList<Statement> statements) {
			this.statements = statements;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append("{\n");
			tabs++;

			if (this.statements != null) {
				for (Statement statement : this.statements) {
					tabs = statement.emit(builder, tabs);
				}
			} else {
				builder.append('\n');
			}

			tabs--;
			indent(builder, tabs);
			builder.append("}\n");

			return tabs;
		}
	}

	public static class Method extends Field {
		public Block block;
		public ArrayList<Argument> arguments;

		public Method(Expression init, String type, String name, Modifier modifier, Block block, ArrayList<Argument> arguments) {
			super(init, type, name, modifier);

			this.block = block;
			this.arguments = arguments;
		}

		@Override
		protected int end(StringBuilder builder, int tabs) {
			builder.append('(');

			if (this.arguments != null) {
				for (int i = 0; i < this.arguments.size(); i++) {
					Argument argument = this.arguments.get(i);
					builder.append(argument.type).append(' ').append(argument.name);

					if (i < this.arguments.size() - 1) {
						builder.append(", ");
					}
				}
			}

			builder.append(')');

			if (this.modifier.isAbstract) {
				builder.append(";\n");
			} else {
				builder.append(' ');
				tabs = this.block.emit(builder, tabs);
			}

			return tabs;
		}
	}

	public static class Enum extends Statement {
		public Modifier modifier;
		public String name;
		public ArrayList<String> values;
		public HashMap<String, Expression.Literal> init;

		public Enum(Modifier modifier, String name, ArrayList<String> values, HashMap<String, Expression.Literal> init) {
			this.modifier = modifier;
			this.name = name;
			this.values = values;
			this.init = init;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);

			builder.append("enum ").append(this.name).append(" {\n");
			tabs++;

			if (this.values != null) {
				for (int i = 0; i < this.values.size(); i++) {
					indent(builder, tabs);

					String name = this.values.get(i);
					builder.append(name);

					if (this.init != null && this.init.containsKey(name)) {
						tabs = this.init.get(name).emit(builder, tabs);
					}

					if (i < this.values.size() - 1) {
						builder.append(",\n");
					} else {
						builder.append("\n");
					}
				}
			} else {
				builder.append("\n");
			}

			tabs--;
			indent(builder, tabs);
			builder.append("}\n");

			return tabs;
		}
	}

	public static class Return extends Statement {
		public Expression value;

		public Return(Expression value) {
			this.value = value;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("return");

			if (this.value != null) {
				builder.append(' ');
				tabs = this.value.emit(builder, tabs);
			}

			builder.append(";\n");
			return tabs;
		}
	}
}