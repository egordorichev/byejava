package org.rexcellentgames.byejava.ast;

public class Modifier {
	public Access access = Access.PUBLIC;
	public boolean isFinal;
	public boolean isStatic;

	public Modifier(Access access, boolean isFinal, boolean isStatic) {
		this.access = access;
		this.isFinal = isFinal;
		this.isStatic = isStatic;
	}

	public Modifier() {

	}
}