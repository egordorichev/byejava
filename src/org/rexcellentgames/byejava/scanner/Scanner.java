package org.rexcellentgames.byejava.scanner;

import java.util.ArrayList;

public class Scanner {
	private String source;
	private int position;
	private int line;
	private int start;
	private boolean ended;

	public Scanner(String source) {
		setSource(source);
	}

	public void setSource(String source) {
		this.source = source;
		this.position = 0;
		this.start = 0;
		this.ended = false;
		this.line = 1;
	}

	protected Token makeToken(TokenType type) {
		return new Token(type, this.start, this.position - this.start, this.line);
	}

	protected Token error(String error) {
		return new ErrorToken(TokenType.ERROR, this.position, this.position, this.line, String.format("[line %d] %s", this.line, error));
	}

	protected char advance() {
		if (!ended) {
			this.position ++;
		} else {
			return '\0';
		}

		if (this.position >= this.source.length()) {
			this.ended = true;
		}

		return this.source.charAt(this.position - 1);
	}

	protected char peek() {
		return this.ended ? '\0' : this.source.charAt(this.position);
	}

	protected char peekNext() {
		return this.position > this.source.length() ? '\0' : this.source.charAt(this.position + 1);
	}

	protected boolean match(char c) {
		if (this.peek() == c) {
			this.advance();
			return true;
		}

		return false;
	}

	protected void skipWhitespace() {
		while (true) {
			char c = this.peek();

			switch (c) {
				case ' ':
				case '\r':
				case '\t':
					this.advance();
					continue;

				case '\n': {
					this.advance();
					this.line++;
					continue;
				}

				case '/': {
					if (this.peekNext() == '/') {
						while (this.peek() != '\n' && this.peek() != '\0') {
							this.advance();
						}

						continue;
					} else if (this.peekNext() == '*') {
						this.advance();
						this.advance();

						while (!(this.peek() == '*' && this.peekNext() == '/') && this.peek() != '\0') {
							this.advance();
						}

						this.advance();
						this.advance();

						continue;
					}
				}

				default: {
					this.start = this.position;
					return;
				}
			}
		}
	}

	private static boolean isAlpha(char c) {
		return c == '_' || c == '@' || Character.isAlphabetic(c);
	}

	private TokenType getIdentifierType() {
		return Keywords.types.getOrDefault(this.source.substring(this.start, this.position), TokenType.IDENTIFIER);
	}

	private Token decideToken(char c, TokenType a, TokenType b) {
		if (this.match(c)) {
			return this.makeToken(a);
		}
		
		return this.makeToken(b);
	}

	private Token decideToken(char ch, TokenType a, char e, TokenType b, TokenType c) {
		if (this.match(ch)) {
			return this.makeToken(a);
		}

		if (this.match(e)) {
			return this.makeToken(b);
		}

		return this.makeToken(c);
	}

	public Token scanToken() {
		this.skipWhitespace();
		char c = this.advance();

		if (c == '\0') {
			return makeToken(TokenType.EOF);
		}

		if (Character.isDigit(c)) {
			while (Character.isDigit(this.peek())) {
				this.advance();
			}

			if (this.peek() == '.' && Character.isDigit(this.peekNext())) {
				this.advance();

				while (Character.isDigit(this.peek())) {
					this.advance();
				}
			}

			this.match('f');
			return makeToken(TokenType.NUMBER);
		}

		if (isAlpha(c)) {
			while (true) {
				c = this.peek();

				if (isAlpha(c) || Character.isDigit(c)) {
					this.advance();
					continue;
				}

				break;
			}

			return this.makeToken(this.getIdentifierType());
		}

		switch (c) {
			case '(': return this.makeToken(TokenType.LEFT_PAREN);
			case ')': return this.makeToken(TokenType.RIGHT_PAREN);
			case '{': return this.makeToken(TokenType.LEFT_BRACE);
			case '}': return this.makeToken(TokenType.RIGHT_BRACE);
			case '[': return this.makeToken(TokenType.LEFT_BRACKET);
			case ']': return this.makeToken(TokenType.RIGHT_BRACKET);
			case ';': return this.makeToken(TokenType.SEMICOLON);
			case ':': return this.makeToken(TokenType.COLON);
			case '?': return this.makeToken(TokenType.QUESTION);
			case ',': return this.makeToken(TokenType.COMMA);
			case '=': return this.decideToken('=', TokenType.EQUAL_EQUAL, TokenType.EQUAL);
			case '-': return this.decideToken('=', TokenType.MINUS_EQUAL, '-', TokenType.MINUS_MINUS, TokenType.MINUS);
			case '+': return this.decideToken('=', TokenType.PLUS_EQUAL, '+', TokenType.PLUS_PLUS, TokenType.PLUS);
			case '/': return this.decideToken('=', TokenType.SLASH_EQUAL, TokenType.SLASH);
			case '%': return this.decideToken('=', TokenType.PERCENT_EQUAL, TokenType.PERCENT);
			case '*': return this.decideToken('=', TokenType.STAR_EQUAL, TokenType.STAR);
			case '>': return this.decideToken('=', TokenType.GREATER_EQUAL, TokenType.GREATER);
			case '<': return this.decideToken('=', TokenType.LESS_EQUAL, TokenType.LESS);
			case '!': return this.decideToken('=', TokenType.BANG_EQUAL, TokenType.BANG);
			case '&': return this.decideToken('=', TokenType.AMPERSAND_EQUAL, '&', TokenType.AND, TokenType.AMPERSAND);
			case '|': return this.decideToken('=', TokenType.BAR_EQUAL, '|', TokenType.OR, TokenType.BAR);

			case '.': {
				if (this.match('.')) {
					if (this.match('.')) {
						return this.makeToken(TokenType.DOT_DOT_DOT);
					} else {
						return this.error("'.' expected");
					}
				}

				return this.makeToken(TokenType.DOT);
			}

			case '\"': {
				while (true) {
					c = this.advance();

					if (c == '\0') {
						return this.error("Unterminated string");
					}

					if (c == '\"') {
						break;
					}

					if (c == '\n') {
						this.line++;
					}
				}

				return makeToken(TokenType.STRING);
			}

			case '\'': {
				if (this.advance() == '\\') {
					this.advance();
				}

				c = this.advance();

				if (c != '\'') {
					return this.error("' expected");
				}

				return makeToken(TokenType.CHAR);
			}

			default: return error(String.format("Unexpected char '%c'", c));
		}
	}

	public ArrayList<Token> scan() {
		ArrayList<Token> tokens = new ArrayList<>();

		if (ended) {
			return tokens;
		}

		while (!this.ended) {
			tokens.add(this.scanToken());
		}

		tokens.add(this.makeToken(TokenType.EOF));

		return tokens;
	}
}
