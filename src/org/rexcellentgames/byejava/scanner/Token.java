package org.rexcellentgames.byejava.scanner;

public class Token {
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

	public String getLexeme(String code) {
		return code.substring(this.start, this.start + this.size);
	}
}