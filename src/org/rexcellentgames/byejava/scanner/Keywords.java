package org.rexcellentgames.byejava.scanner;

import java.util.HashMap;

public class Keywords {
	public static HashMap<String, TokenType> types = new HashMap<>();

	static {
		for (TokenType type : TokenType.values()) {
			// Check if this is a keyword
			if (type.id >= TokenType.CLASS.id && type.id <= TokenType.ABSTRACT.id) {
				types.put(type.toString().toLowerCase(), type);
			}
		}
	}
}