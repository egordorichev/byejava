package org.rexcellentgames.byejava.ast;

import java.util.HashMap;

public class TypeRegistry {
	public static HashMap<String, Type> types = new HashMap<>();
	public static HashMap<String, Integer> reserved = new HashMap<>();

	static {
		add(new Type("boolean", "bool"));
		add(new Type("String", "string"));
		add(new Type("HashMap", "Dictionary").setModule("java.util.HashMap", "system.collections.generic.Dictionary"));
		add(new Type("ArrayList", "List").setModule("java.util.ArrayList", "system.collections.generic.Dictionary"));

		String[] res = { "int", "char", "byte", "short", "float", "double" };

		for (String string : res) {
			reserved.put(string, 0);
		}
	}

	private static void add(Type type) {
		types.put(type.originalName, type);
	}
}