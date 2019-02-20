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
	}

	protected Token makeToken(TokenType type) {
		System.out.println("Make token '" + this.source.substring(this.start, this.position) + "'");
		return new Token(type, this.start, this.position - this.start, this.line);
	}

	protected Token error(String error) {
		return new ErrorToken(TokenType.ERROR, this.position, this.position, this.line, error);
	}

	protected char advance() {
		this.position ++;

		if (this.position >= this.source.length()) {
			this.ended = true;
		}

		return this.peek();
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

				default: {
					this.start = this.position;
					return;
				}
			}
		}
	}

	private static boolean isAlpha(char c) {
		return c == '_' || Character.isAlphabetic(c);
	}

	private TokenType getIdentifierType() {
		return Keywords.types.getOrDefault(this.source.substring(this.start, this.position), TokenType.IDENTIFIER);
	}

	public Token scanToken() {
		this.skipWhitespace();

		if (ended) {
			return makeToken(TokenType.EOF);
		}

		char c = this.peek();

		if (Character.isDigit(c)) {
			this.advance();

			while (Character.isDigit(this.peek())) {
				this.advance();
			}

			if (this.peek() == '.' && Character.isDigit(this.peekNext())) {
				this.advance();

				while (Character.isDigit(this.peek())) {
					this.advance();
				}
			}

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

			return makeToken(this.getIdentifierType());
		}

		switch (c) {
			case '\"': {
				while (true) {
					c = this.advance();

					if (c == '\0') {
						return this.error("Unterminated string");
					}

					if (c == '\"') {
						this.advance();
						break;
					}

					if (c == '\n') {
						this.line++;
					}
				}

				return makeToken(TokenType.STRING);
			}

			default: return makeToken(TokenType.ERROR);
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

		return tokens;
	}
}
