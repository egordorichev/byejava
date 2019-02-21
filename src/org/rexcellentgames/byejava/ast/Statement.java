package org.rexcellentgames.byejava.ast;

import java.util.ArrayList;

public class Statement {
	public static class Expr extends Statement {
		public Expression expression;

		public Expr(Expression expression) {
			this.expression = expression;
		}
	}

	public static class Package extends Statement {
		public String name;

		public Package(String name) {
			this.name = name;
		}
	}

	public static class Class extends Statement {
		public String name;
		public String base;
		public ArrayList<String> implementations;
		public ArrayList<Field> fields;
		public Modifier modifier;

		public Class(String name, String base, ArrayList<String> implementations, ArrayList<Field> fields, Modifier modifier) {
			this.name = name;
			this.base = base;
			this.implementations = implementations;
			this.fields = fields;
			this.modifier = modifier;
		}
	}

	public static class Field extends Statement {
		public Expression init;
		public String type;
		public Modifier modifier;

		public Field(Expression init, String type, Modifier modifier) {
			this.init = init;
			this.type = type;
			this.modifier = modifier;
		}
	}
}