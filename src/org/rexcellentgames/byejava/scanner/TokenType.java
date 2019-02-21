package org.rexcellentgames.byejava.scanner;

public enum TokenType {
	// Values
	IDENTIFIER, STRING, NUMBER,
	// Single-character tokens.
	LEFT_PAREN, RIGHT_PAREN,
	LEFT_BRACE, RIGHT_BRACE,
	COMMA, DOT,
	DOT_DOT_DOT,
	COLON, SEMICOLON,
	QUESTION,
	// One or two character tokens.
	BANG, BANG_EQUAL,
	SLASH, SLASH_EQUAL,
	PERCENT, PERCENT_EQUAL,
	STAR, STAR_EQUAL,
	MINUS, MINUS_EQUAL,
	PLUS, PLUS_EQUAL,
	EQUAL, EQUAL_EQUAL,
	GREATER, GREATER_EQUAL,
	LESS, LESS_EQUAL,
	AMPERSAND, AMPERSAND_EQUAL,
	AND, // &&
	BAR, BAR_EQUAL,
	OR, // ||
	// Keywords
	CLASS, NEW,
	IMPLEMENTS, EXTENDS,
	ENUM, NULL,
	RETURN, SUPER, THIS,
	TRUE, FALSE,
	WHILE, DO, FOR,
	BREAK, CONTINUE,
	IF, ELSE,
	SWITCH, DEFAULT,
	CASE, INSTANCEOF,
	PUBLIC, PROTECTED,
	PRIVATE, STATIC,
	FINAL, CONST,
	PACKAGE, IMPORT,
	TRY, THROWS,
	THROW, CATCH,
	FINALLY, ABSTRACT,
	// Special
	EOF, ERROR;

	public int id;

	TokenType() {
		// Can't store a static field in a enum :/
		this.id = Token.lastId++;
	}
}