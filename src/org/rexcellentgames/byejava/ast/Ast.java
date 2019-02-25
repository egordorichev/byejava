package org.rexcellentgames.byejava.ast;

import org.rexcellentgames.byejava.renamer.Renamer;

import java.util.ArrayList;

public class Ast {
	public int emit(StringBuilder builder, int tabs) {
		return tabs;
	}

	public int emitEnd(StringBuilder builder, int tabs) {
		return tabs;
	}

	public void indent(StringBuilder builder, int tabs) {
		for (int i = 0; i < tabs; i++) {
			builder.append('\t');
		}
	}

	public void rename() {

	}

	private static String toCamelCase(String str) {
		if (str.length() == 0) {
			return str;
		}

		if (str.length() == 1) {
			return str.substring(0, 1).toUpperCase();
		}

		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	protected String checkType(String type) {
		if (type == null) {
			return null;
		}

		if (TypeRegistry.reserved.containsKey(type)) {
			return type;
		}

		Type tp = TypeRegistry.types.getOrDefault(type, null);

		if (tp != null) {
			if (tp.module != null) {
				for (Statement statement : Renamer.ast) {
					if (statement instanceof Statement.Import) {
						Statement.Import imp = ((Statement.Import) statement);

						if (imp.module.equals(tp.originalModule)) {
							imp.setModule(tp.module);
						}
					}
				}
			}

			return tp.name;
		}

		return toCamelCase(type);
	}

	protected static void emitGeneric(StringBuilder builder, ArrayList<Generetic> generetics) {
		builder.append('<');

		for (int i = 0; i < generetics.size(); i++) {
			Generetic generetic = generetics.get(i);
			builder.append(generetic.name);

			if (generetic.generetics != null) {
				emitGeneric(builder, generetic.generetics);
			}

			if (i < generetics.size() - 1) {
				builder.append(", ");
			}
		}

		builder.append('>');
	}

	protected String updateName(String name) {
		if (name == null) {
			return null;
		}

		return toCamelCase(name);
	}

	protected void checkTypes(ArrayList<Generetic> generetics) {
		if (generetics == null) {
			return;
		}

		for (Generetic generetic : generetics) {
			generetic.name = this.checkType(generetic.name);
		}
	}

	protected void rename(ArrayList<Expression> expressions) {
		if (expressions == null) {
			return;
		}

		for (Expression expression : expressions) {
			if (expression != null) {
				expression.rename();
			}
		}
	}

	protected void renameStatements(ArrayList<Statement> statements) {
		if (statements == null) {
			return;
		}

		for (Statement statement : statements) {
			statement.rename();
		}
	}

	protected void rename(Argument argument) {
		argument.name = this.updateName(argument.name);
		argument.type = this.checkType(argument.type);

		this.checkTypes(argument.generetics);
	}
}
