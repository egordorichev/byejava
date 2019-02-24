package org.rexcellentgames.byejava.ast;

public class Type {
	public String originalName;
	public String name;
	public String originalModule;
	public String module;

	public Type(String originalName, String name) {
		this.originalName = originalName;
		this.name = name;
	}

	public Type setModule(String module, String newModule) {
		this.originalModule = module;
		this.module = newModule;

		return this;
	}
}