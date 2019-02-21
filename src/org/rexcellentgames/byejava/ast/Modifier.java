package org.rexcellentgames.byejava.ast;

public class Modifier {
	public Access access = Access.PUBLIC;
	public boolean isFinal;
	public boolean isStatic;
	public boolean isAbstract;

	public Modifier(Access access, boolean isFinal, boolean isStatic, boolean isAbstract) {
		this.access = access;
		this.isFinal = isFinal;
		this.isStatic = isStatic;
		this.isAbstract = isAbstract;
	}

	public Modifier() {

	}
}