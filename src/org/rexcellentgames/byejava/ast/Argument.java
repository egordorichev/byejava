package org.rexcellentgames.byejava.ast;

public class Argument {
	public String name;
	public String type;
	public boolean varg;

	public Argument(String name, String type, boolean varg) {
		this.name = name;
		this.type = type;
		this.varg = varg;
	}

	public Argument() {

	}
}
