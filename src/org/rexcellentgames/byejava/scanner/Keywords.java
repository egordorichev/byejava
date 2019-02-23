package org.rexcellentgames.byejava.scanner;

import java.util.HashMap;

public class Keywords {
	public static HashMap<String, TokenType> types = new HashMap<>();
	public static HashMap<String, Integer> reserved = new HashMap<>();

	static {
		for (TokenType type : TokenType.values()) {
			// Check if this is a keyword
			if (type.id >= TokenType.CLASS.id && type.id <= TokenType.ABSTRACT.id) {
				types.put(type.toString().toLowerCase(), type);
			}

			types.put("@Override", TokenType.OVERRIDE);
		}

		String[] res = { "int", "char", "boolean", "double" };

		for (String string : res) {
			reserved.put(string, 0);
		}
	}
}