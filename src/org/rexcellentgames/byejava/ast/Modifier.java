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

	public int compareTo(Modifier modifier) {
		if (this.isStatic && !modifier.isStatic) {
			return -1;
		}

		if (this.isFinal && !modifier.isFinal) {
			return -1;
		}

		if (this.access == modifier.access) {
			return 0;
		}

		if (this.access == Access.PUBLIC) {
			return -1;
		}

		if (this.access == Access.PROTECTED) {
			return modifier.access == Access.PUBLIC ? 1 : -1;
		}

		return 1;
	}
}