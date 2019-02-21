package org.rexcellentgames.byejava.emitter;

import org.rexcellentgames.byejava.ast.Statement;

import java.util.ArrayList;

public class Emitter {
	private ArrayList<Statement> ast;
	private StringBuilder builder;

	public Emitter(ArrayList<Statement> ast) {
		this.setAst(ast);
	}

	public void setAst(ArrayList<Statement> ast) {
		this.ast = ast;
	}

	public String emit() {
		this.builder = new StringBuilder();
		int tabs = 0;
		Statement packageStatement = null;

		for (Statement statement : this.ast) {
			if (statement instanceof Statement.Package) {
				tabs = statement.emit(builder, tabs);
				packageStatement = statement;
			}
		}

		for (Statement statement : this.ast) {
			if (statement != packageStatement) {
				tabs = statement.emit(this.builder, tabs);
			}
		}

		if (packageStatement != null) {
			tabs = packageStatement.emitEnd(this.builder, tabs);
		}

		return this.builder.toString();
	}
}