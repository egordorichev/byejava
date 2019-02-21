package org.rexcellentgames.byejava.scanner;

public class Token {
	public static int lastId;

	public TokenType type;
	public int start;
	public int size;
	public int line;

	public Token(TokenType type, int start, int size, int line) {
		this.type = type;
		this.start = start;
		this.size = size;
		this.line = line;
	}

	public Token() {

	}
}