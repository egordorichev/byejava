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
	}

	public static class Block extends Statement {
		public ArrayList<Statement> statements;

		public Block(ArrayList<Statement> statements) {
			this.statements = statements;
		}
	}

	public static class Method extends Field {
		public String name;
		public Expression init;
		public String type;
		public Modifier modifier;
		public Block block;
		public ArrayList<Argument> arguments;

		public Method(Expression init, String type, String name, Modifier modifier, Block block, ArrayList<Argument> arguments) {
			super(init, type, name, modifier);

			this.block = block;
			this.arguments = arguments;
		}
	}
}