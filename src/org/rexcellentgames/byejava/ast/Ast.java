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

	private static String toCamelCase(String s) {
		String[] parts = s.split(" ");
		StringBuilder camelCaseString = new StringBuilder();

		for (String part : parts) {
			if (part != null && part.trim().length() > 0) {
				camelCaseString.append(toProperCase(part));
			} else {
				camelCaseString.append(part).append(" ");
			}
		}

		return camelCaseString.toString();
	}

	private static String toProperCase(String s) {
		String temp = s.trim();
		String spaces = "";

		if (temp.length() != s.length()) {
			int startCharIndex = s.charAt(temp.indexOf(0));
			spaces = s.substring(0, startCharIndex);
		}

		temp = temp.substring(0, 1).toUpperCase() +
			spaces + temp.substring(1).toLowerCase();

		return temp;

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
			expression.rename();
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
