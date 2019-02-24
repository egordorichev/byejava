package org.rexcellentgames.byejava.ast;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Statement extends Expression {
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

		@Override
		public void rename() {
			this.expression.rename();
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
		public ArrayList<Generetic> baseGeneretic;
		public ArrayList<String> implementations;
		public ArrayList<Field> fields;
		public ArrayList<Method> methods;
		public ArrayList<Block> init;
		public ArrayList<Statement> inner;
		public Modifier modifier;
		public ArrayList<Generetic> generetics;
		public Expression.Call call;

		public Class(String name, String base, ArrayList<Generetic> baseGeneretic, ArrayList<String> implementations, ArrayList<Field> fields, Modifier modifier, ArrayList<Method> methods, ArrayList<Statement> inner, ArrayList<Block> init, ArrayList<Generetic> generetics, Expression.Call call) {
			this.name = name;
			this.base = base;
			this.baseGeneretic = baseGeneretic;
			this.implementations = implementations;
			this.fields = fields;
			this.modifier = modifier;
			this.inner = inner;
			this.methods = methods;
			this.init = init;
			this.generetics = generetics;
			this.call = call;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			if (this.call == null) {
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

						if (this.baseGeneretic != null) {
							builder.append('<');

							for (int i = 0; i < this.baseGeneretic.size(); i++) {
								builder.append(this.baseGeneretic.get(i).name);

								if (i < this.baseGeneretic.size() - 1) {
									builder.append(", ");
								}
							}

							builder.append("> ");
						}

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
			} else {
				this.call.emit(builder, tabs);
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

				Comparator<Field> comparator = new Comparator<Field>() {
					@Override
					public int compare(Field s1, Field s2) {
						return s1.modifier.compareTo(s2.modifier);
					}
				};

				if (this.fields != null) {
					Collections.sort(this.fields, comparator);

					for (int i = 0; i < this.fields.size(); i++) {
						tabs = this.fields.get(i).emit(builder, tabs);
					}

					if (this.methods != null) {
						builder.append('\n');
					}
				}

				boolean calledInit = false;

				if (this.methods != null) {
					Collections.sort(this.methods, comparator);

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
					indent(builder, tabs + 1);
					builder.append("_Init();\n");
					indent(builder, tabs);
					builder.append("}\n");
				}
			}

			tabs--;

			indent(builder, tabs);
			builder.append(this.call == null ? "}\n" : "}");

			return tabs;
		}

		@Override
		public void rename() {
			this.name = this.checkType(this.name);

			if (this.base != null) {
				this.base = this.checkType(this.base);

				if (this.baseGeneretic != null) {
					this.checkTypes(this.baseGeneretic);
				}
			}

			if (this.implementations != null) {
				for (int i = 0; i < this.implementations.size(); i++) {
					this.implementations.set(i, this.checkType(this.implementations.get(i)));
				}
			}

			if (this.generetics != null) {
				this.checkTypes(this.generetics);
			}

			if (this.methods != null) {
				for (Method methods : this.methods) {
					methods.rename();
				}
			}

			if (this.fields != null) {
				for (Field field : this.fields) {
					field.rename();
				}
			}

			if (this.inner != null) {
				for (Statement inner : this.inner) {
					inner.rename();
				}
			}

			if (this.init != null) {
				for (Block block : this.init) {
					block.rename();
				}
			}

			if (this.call != null) {
				this.call.rename();
			}
		}
	}

	public static class Field extends Statement {
		public String name;
		public Expression init;
		public String type;
		public Modifier modifier;
		public ArrayList<Generetic> generetics;
		public boolean array;

		public Field(Expression init, String type, String name, Modifier modifier, ArrayList<Generetic> generetics,boolean array) {
			this.init = init;
			this.type = type;
			this.name = name;
			this.modifier = modifier;
			this.generetics = generetics;
			this.array = array;
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

			if (this.array) {
				builder.deleteCharAt(builder.length() - 1);
				builder.append("[] ");
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

		@Override
		public void rename() {
			this.name = this.updateName(this.name);
			this.type = this.checkType(this.type);
			this.checkTypes(this.generetics);

			if (this.init != null) {
				this.init.rename();
			}
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

		@Override
		public void rename() {
			this.renameStatements(this.statements);
		}
	}

	public static class Method extends Field {
		public Block block;
		public ArrayList<Argument> arguments;
		public boolean override;

		public Method(Expression init, String type, String name, Modifier modifier, Block block, ArrayList<Argument> arguments, ArrayList<Generetic> generetics) {
			super(init, type, name, modifier, generetics, false);

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

		@Override
		public void rename() {
			super.rename();
			this.block.rename();

			if (this.arguments != null) {
				for (Argument argument : this.arguments) {
					this.rename(argument);
				}
			}
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

		@Override
		public void rename() {
			this.name = this.updateName(this.name);

			for (int i = 0; i < this.values.size(); i++) {
				this.values.set(i, this.updateName(this.values.get(i)));
			}
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

		@Override
		public void rename() {
			if (this.value != null) {
				this.value.rename();
			}
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
			builder.append("if (");
			tabs = this.ifCondition.emit(builder, tabs);
			builder.append(") ");
			this.ifBranch.emit(builder, this.ifBranch instanceof Block ? tabs : 0);

			if (this.ifBranch instanceof Block) {
				builder.deleteCharAt(builder.length() - 1);
				builder.append(' ');
			}

			if (this.ifElseConditions != null) {
				for (int i = 0; i < this.ifElseConditions.size(); i++) {
					builder.append("else if (");
					tabs = this.ifElseConditions.get(i).emit(builder, tabs);
					builder.append(") ");
					Statement branch = this.ifElseBranches.get(i);
					branch.emit(builder, branch instanceof Block ? tabs : 0);

					if (branch instanceof Block) {
						builder.deleteCharAt(builder.length() - 1);
						builder.append(' ');
					}
				}
			}

			if (this.elseBranch != null) {
				builder.append("else ");
				this.elseBranch.emit(builder, this.elseBranch instanceof Block ? tabs : 0);
			}

			builder.append('\n');

			return tabs;
		}

		@Override
		public void rename() {
			this.ifBranch.rename();
			this.ifCondition.rename();

			if (this.ifElseConditions != null) {
				this.rename(this.ifElseConditions);
				this.renameStatements(this.ifElseBranches);
			}

			if (this.elseBranch != null) {
				this.elseBranch.rename();
			}
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

		@Override
		public void rename() {
			if (this.init != null) {
				this.init.rename();
			}

			if (this.condition != null) {
				this.condition.rename();
			}

			if (this.increment != null) {
				this.increment.rename();
			}

			this.body.rename();
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

		@Override
		public void rename() {
			this.rename(this.self);

			if (this.init != null) {
				this.init.rename();
			}
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

		@Override
		public void rename() {
			this.init.rename();
			this.in.rename();
			this.body.rename();
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

		@Override
		public void rename() {
			this.condition.rename();
			this.body.rename();
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
		public String realModule;

		public Import(String module) {
			this.setModule(module);
		}

		public void setModule(String module) {
			this.module = module;
			this.realModule = this.getModule();
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			builder.append("using ").append(this.realModule).append(";\n");
			return tabs;
		}

		public String getModule() {
			int dot = module.lastIndexOf('.');
			return dot == -1 ? module : module.substring(0, dot);
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

		@Override
		public void rename() {
			this.what.rename();

			if (this.branches != null) {
				for (SwitchBranch branch : this.branches) {
					this.rename(branch.cases);
					branch.block.rename();
				}
			}
		}
	}

	public static class TryException {
		public ArrayList<String> types;
		public String name;
	}

	public static class Try extends Statement {
		public Statement tryBranch;
		public ArrayList<TryException> tryVars;
		public ArrayList<Statement> tryBranches;
		public Statement finallyBranch;

		public Try(Statement tryBranch, ArrayList<TryException> tryVars, ArrayList<Statement> tryBranches, Statement finallyBranch) {
			this.tryBranch = tryBranch;
			this.tryVars = tryVars;
			this.tryBranches = tryBranches;
			this.finallyBranch = finallyBranch;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("try ");
			this.tryBranch.emit(builder, tabs);

			if (this.tryBranches != null) {
				for (int i = 0; i < this.tryBranches.size(); i++) {
					builder.deleteCharAt(builder.length() - 1);
					builder.append(" catch (");
					TryException var = this.tryVars.get(i);

					for (int j = 0; j < var.types.size(); j++) {
						builder.append(var.types.get(j));

						if (j < var.types.size() - 1) {
							builder.append(" | ");
						}
					}

					builder.append(' ');
					builder.append(var.name);

					builder.deleteCharAt(builder.length() - 1);
					builder.deleteCharAt(builder.length() - 1);
					builder.append(") ");
					this.tryBranches.get(i).emit(builder,tabs);
				}
			}

			if (this.finallyBranch != null) {
				builder.deleteCharAt(builder.length() - 1);
				builder.append(" finally ");
				this.finallyBranch.emit(builder, tabs);
			}

			return tabs;
		}

		@Override
		public void rename() {
			this.tryBranch.rename();

			if (this.tryVars != null) {
				this.renameStatements(this.tryBranches);

				for (TryException exception : this.tryVars) {
					exception.name = this.updateName(exception.name);

					for (int i = 0; i < exception.types.size(); i++) {
						exception.types.set(i, this.updateName(exception.types.get(i)));
					}
				}
			}
		}
	}

	public static class Throw extends Statement {
		public Expression var;

		public Throw(Expression var) {
			this.var = var;
		}

		@Override
		public int emit(StringBuilder builder, int tabs) {
			indent(builder, tabs);
			builder.append("throw ");
			this.var.emit(builder, 0);
			builder.append(";\n");

			return tabs;
		}

		@Override
		public void rename() {
			this.var.rename();
		}
	}
}