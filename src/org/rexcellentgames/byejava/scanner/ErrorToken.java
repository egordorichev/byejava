package org.rexcellentgames.byejava.scanner;

public class ErrorToken extends Token {
	public String error;

	public ErrorToken(TokenType type, int start, int size, int line, String error) {
		super(type, start, size, line);
		this.error = error;
	}
}