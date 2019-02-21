package org.rexcellentgames.byejava.ast;

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
}
