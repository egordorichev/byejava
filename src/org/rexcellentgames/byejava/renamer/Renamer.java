package org.rexcellentgames.byejava.renamer;

import org.rexcellentgames.byejava.ast.Statement;

import java.util.ArrayList;

public class Renamer {
	public Renamer() {

	}

	public void rename(ArrayList<Statement> statements) {
		for (Statement statement : statements) {
			statement.rename();
		}
	}
}