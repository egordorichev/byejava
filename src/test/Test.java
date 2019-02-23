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
		ArrayList<Statement.Import> imports = new ArrayList<>();

		for (Statement statement : this.ast) {
			if (statement instanceof Statement.Package) {
				packageStatement = statement;
			} else if (statement instanceof Statement.Import) {
				Statement.Import im = (Statement.Import) statement;
				boolean found = false;

				for (Statement.Import i : imports) {
					if (i.module.equals(im.module)) {
						found = true;
						break;
					}
				}

				if (!found) {
					imports.add(im);
					im.emit(builder, tabs);
				}
			}
		}

		if (imports.size() > 0) {
			builder.append('\n');
		}

		if (packageStatement != null) {
			tabs = packageStatement.emit(builder, tabs);
		}

		for (Statement statement : this.ast) {
			if (statement != packageStatement && !(statement instanceof Statement.Import)) {
				tabs = statement.emit(this.builder, tabs);
			}
		}

		if (packageStatement != null) {
			tabs = packageStatement.emitEnd(this.builder, tabs);
		}

		return this.builder.toString();
	}
}