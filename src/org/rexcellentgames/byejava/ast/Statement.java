package org.rexcellentgames.byejava.ast;

import jdk.nashorn.internal.ir.Block;

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
			builder.append("}");

			return tabs;
		}
	}

	public static class Class extends Statement {
		public String name;
		public String base;
		public ArrayList<String> implementations;
		public ArrayList<Field> fields;
		public ArrayList<Method> methods;
		public ArrayList<Block> init;
		public ArrayList<Statement> inner;
		public Modifier modifier;
		public ArrayList<Generetic> generetics;

		public Class(String name, String base, ArrayList<String> implementations, ArrayList<Field> fields, Modifier modifier, ArrayList<Method> methods, ArrayList<Statement> inner, ArrayList<Block> init, ArrayList<Generetic> generetics) {
			this.name = name;
			this.base = base;
			this.implementations = implementations;
			this.fields = fields;
			this.modifier = modifier;
			this.inner = inner;
			this.methods = methods;
			this.init = init;
			this.generetics = generetics;
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

			if (this.generetics != null) {
				builder.append('<');

				for (int i = 0; i < this.generetics.size(); i++) {
					builder.append(this.generetics.get(i).name);

					if (i < this.generetics.size() - 1) {
						builder.append(", ");
					}
				}

				builder.append("> ");
			}

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

			if (this.generetics != null) {
				boolean had = false;

				for (int i = 0; i < this.generetics.size(); i++) {
					Generetic generetic = this.generetics.get(i);

					if (generetic.extend == null) {
						continue;
					}

					if (had) {
						builder.append(' ');
					}

					had = true;
					builder.append("where ").append(generetic.name).append(" : ").append(generetic.extend);
				}
			}


			builder.append(" {\n");
			tabs++;

			if (this.init != null) {
				indent(builder, tabs);
				builder.append("protected void _Init() {\n");
				tabs++;

				for (int i = 0; i < this.init.size(); i++) {
					indent(builder, tabs);
					this.init.get(i).emit(builder, tabs);

					if (i < this.init.size() - 1) {
						builder.append("\n");
					}
				}

				tabs--;
				indent(builder, tabs);
				builder.append("}\n\n");
			}

			if (this.inner == null && this.fields == null && this.methods == null) {
				builder.append('\n');
			} else {
				if (this.inner != null) {
					for (int i = 0; i < this.inner.size(); i++) {
						Statement statement = this.inner.get(i);

						if (statement instanceof Block) {
							indent(builder, tabs);
							builder.append("static ").append(this.name).append("() ");
						}

						tabs = statement.emit(builder, tabs);

						if (this.fields != null || this.methods != null || i < this.inner.size() - 1) {
							builder.append('\n');
						}
					}
				}

				if (this.fields != null) {
					for (int i = 0; i < this.fields.size(); i++) {
						tabs = this.fields.get(i).emit(builder, tabs);

						/*if (this.methods != null || i < this.fields.size() - 1) {
							builder.append('\n');
						}*/
					}
				}

				boolean calledInit = false;

				if (this.methods != null) {
					for (int i = 0; i < this.methods.size(); i++) {
						Method method = this.methods.get(i);

						if (this.init != null && method.name.equals(this.name)) {
							method.block.callInit = true;
							calledInit = true;
						}

						tabs = method.emit(builder, tabs);

						if (i < this.methods.size() - 1) {
							builder.append('\n');
						}
					}
				}

				if (!calledInit && this.init != null) {
					builder.append('\n');
					indent(builder, tabs);
					builder.append("public ").append(this.name).append("() {\n");

					for (int i = 0; i < this.init.size(); i++) {
						indent(builder, tabs + 1);
						this.init.get(i).emit(builder, tabs + 1);

						if (i < this.init.size() - 1) {
							builder.append('\n');
						}
					}

					indent(builder, tabs);
					builder.append("}\n");
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
		public ArrayList<Generetic> generetics;

		public Field(Expression init, String type, String name, Modifier modifier, ArrayList<Generetic> generetics) {
			this.init = init;
			this.type = type;
			this.name = name;
			this.modifier = modifier;
			this.generetics = generetics;
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

			this.insert(builder, tabs);

			if (this.type != null) {
				builder.append(this.type);

				if (this.generetics != null && !(this instanceof Method)) {
					builder.append('<');

					for (int i = 0; i < this.generetics.size(); i++) {
						builder.append(this.generetics.get(i).name);

						if (i < this.generetics.size() - 1) {
							builder.append(", ");
						}
					}

					builder.append('>');
				}

				builder.append(' ');
			}

			builder.append(this.name);

			return this.end(builder, tabs);
		}

		protected void insert(StringBuilder builder, int tabs) {

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
		public boolean callInit;

		public Block(ArrayList<Statement> statements) {
			this.statements = statements;
		}

		private static boolean valid(Statement expression) {
			return expression instanceof Expr || expression instanceof Var;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append("{\n");
			tabs++;

			if (this.callInit) {
				indent(builder, tabs);
				builder.append("_Init();");

				if (this.statements != null) {
					builder.append('\n');
				}
			}

			if (this.statements != null) {
				for (int i = 0; i < this.statements.size(); i++) {
					Statement statement = this.statements.get(i);

					if (i > 0 && !(valid(this.statements.get(i - 1)) && valid(statement))) {
						builder.append('\n');
					}

					if (statement instanceof Block) {
						indent(builder, tabs);
					}

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
		public boolean override;

		public Method(Expression init, String type, String name, Modifier modifier, Block block, ArrayList<Argument> arguments, ArrayList<Generetic> generetics) {
			super(init, type, name, modifier, generetics);

			this.block = block;
			this.arguments = arguments;
		}

		@Override
		protected void insert(StringBuilder builder, int tabs) {
			if (this.override) {
				builder.append("override ");
			}
		}

		@Override
		protected int end(StringBuilder builder, int tabs) {
			if (this.generetics != null) {
				builder.append('<');

				for (int i = 0; i < this.generetics.size(); i++) {
					builder.append(this.generetics.get(i).name);

					if (i < this.generetics.size() - 1) {
						builder.append(", ");
					}
				}

				builder.append("> ");
			}

			builder.append('(');

			if (this.arguments != null) {
				for (int i = 0; i < this.arguments.size(); i++) {
					Argument argument = this.arguments.get(i);

					if (argument.varg) {
						builder.append("params ");
					}

					builder.append(argument.type);

					if (argument.varg) {
						builder.append("[]");
					}

					builder.append(' ').append(argument.name);

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

	public static class If extends Statement {
		public Expression ifCondition;
		public Statement ifBranch;
		public ArrayList<Expression> ifElseConditions;
		public ArrayList<Statement> ifElseBranches;
		public Statement elseBranch;

		public If(Expression ifCondition, Statement ifBranch, ArrayList<Expression> ifElseConditions, ArrayList<Statement> ifElseBranches, Statement elseBranch) {
			this.ifCondition = ifCondition;
			this.ifBranch = ifBranch;
			this.ifElseConditions = ifElseConditions;
			this.ifElseBranches = ifElseBranches;
			this.elseBranch = elseBranch;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("if ");
			tabs = this.ifCondition.emit(builder, tabs);
			builder.append(' ');
			this.ifBranch.emit(builder, this.ifBranch instanceof Expr ? 0 : tabs);

			if (!(this.ifBranch instanceof Block)) {
				indent(builder, tabs);
			} else {
				builder.deleteCharAt(builder.length() - 1);
				builder.append(' ');
			}

			if (this.ifElseConditions != null) {
				for (int i = 0; i < this.ifElseConditions.size(); i++) {
					builder.append("else if ");
					tabs = this.ifElseConditions.get(i).emit(builder, tabs);
					builder.append(' ');
					Statement branch = this.ifElseBranches.get(i);
					branch.emit(builder, branch instanceof Expr ? 0 : tabs);

					if (!(branch instanceof Block)) {
						indent(builder, tabs);
					} else {
						builder.deleteCharAt(builder.length() - 1);
						builder.append(' ');
					}
				}
			}

			if (this.elseBranch != null) {
				builder.append("else ");
				this.elseBranch.emit(builder, this.elseBranch instanceof Expr ? 0 : tabs);
			}

			return tabs;
		}
	}

	public static class For extends Statement {
		public Statement init;
		public Expression condition;
		public Expression increment;
		public Statement body;

		public For(Statement init, Expression condition, Expression increment, Statement body) {
			this.init = init;
			this.condition = condition;
			this.increment = increment;
			this.body = body;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("for (");

			if (this.init != null) {
				this.init.emit(builder, 0);

				if (this.init instanceof Var) {
					builder.deleteCharAt(builder.length() - 1);
					builder.deleteCharAt(builder.length() - 1);
				}
			}

			if (this.condition != null) {
				builder.append("; ");
				condition.emit(builder, tabs);
			} else {
				builder.append(';');
			}

			if (this.increment != null) {
				builder.append("; ");
				increment.emit(builder, tabs);
			} else {
				builder.append(';');
			}

			builder.append(") ");
			this.body.emit(builder, this.body instanceof Expr ? 0 : tabs);

			return tabs;
		}
	}

	public static class Var extends Statement {
		public Argument self;
		public Expression init;

		public Var(Argument self, Expression init) {
			this.self = self;
			this.init = init;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append(this.self.type);

			if (this.self.generetics != null) {
				builder.append('<');

				for (int i = 0; i < this.self.generetics.size(); i++) {
					builder.append(this.self.generetics.get(i).name);

					if (i < this.self.generetics.size() - 1) {
						builder.append(", ");
					}
				}

				builder.append('>');
			}

			if (this.self.array) {
				builder.append("[]");
			}

			builder.append(' ').append(this.self.name);

			if (this.init != null) {
				builder.append(" = ");
				this.init.emit(builder, tabs);
			}

			builder.append(";\n");
			return tabs;
		}
	}

	public static class Foreach extends Statement {
		public Statement init;
		public Expression in;
		public Statement body;

		public Foreach(Statement init, Expression in, Statement body) {
			this.init = init;
			this.in = in;
			this.body = body;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("foreach (");

			this.init.emit(builder, 0);

			builder.deleteCharAt(builder.length() - 1);
			builder.deleteCharAt(builder.length() - 1);
			builder.append(" in ");

			this.in.emit(builder, tabs);

			builder.append(") ");
			this.body.emit(builder, this.body instanceof Expr ? 0 : tabs);

			return tabs;
		}
	}

	public static class While extends Statement {
		public Expression condition;
		public Statement body;
		public boolean flipped;

		public While(Expression condition, Statement body, boolean flipped) {
			this.condition = condition;
			this.body = body;
			this.flipped = flipped;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);

			if (this.flipped) {
				builder.append("do ");
				this.body.emit(builder, tabs);

				if (this.body instanceof Block) {
					builder.deleteCharAt(builder.length() - 1);
				}

				builder.append(" while (");
				this.condition.emit(builder, 0);
				builder.append(");\n");
			} else {
				builder.append("while (");
				this.condition.emit(builder, 0);
				builder.append(") ");
				this.body.emit(builder, tabs);
			}

			return tabs;
		}
	}

	public static class Break extends Statement {
		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("break;\n");

			return tabs;
		}
	}

	public static class Continue extends Statement {
		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("continue;\n");

			return tabs;
		}
	}

	public static class Import extends Statement {
		public String module;

		public Import(String module) {
			int dot = module.lastIndexOf('.');
			this.module = dot == -1 ? module : module.substring(0, dot);
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append("using ").append(this.module).append(";\n");
			return tabs;
		}
	}

	public static class SwitchBranch {
		public Block block;
		public ArrayList<Expression> cases;
	}

	public static class Switch extends Statement {
		public ArrayList<SwitchBranch> branches;
		public Expression what;

		public Switch(ArrayList<SwitchBranch> branches, Expression what) {
			this.branches = branches;
			this.what = what;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("switch (");
			this.what.emit(builder, tabs);
			builder.append(") {\n");
			tabs++;

			if (this.branches != null) {
				for (int i = 0; i < this.branches.size(); i++) {
					SwitchBranch branch = this.branches.get(i);

					for (int j = 0; j < branch.cases.size(); j++) {
						Expression expression = branch.cases.get(j);

						indent(builder, tabs);

						if (expression == null) {
							builder.append("default:");
						} else {
							builder.append("case ");
							expression.emit(builder, 0);
							builder.append(": ");
						}

						if (j < branch.cases.size() - 1) {
							builder.append('\n');
						}
					}

					branch.block.emit(builder, tabs);

					if (i < this.branches.size() - 1) {
						builder.append('\n');
					}
				}
			}

			tabs--;
			indent(builder, tabs);
			builder.append("}\n");

			return tabs;
		}
	}
}