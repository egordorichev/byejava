package org.rexcellentgames.byejava.parser;

import org.rexcellentgames.byejava.ast.Access;
import org.rexcellentgames.byejava.ast.Modifier;
import org.rexcellentgames.byejava.ast.Statement;
import org.rexcellentgames.byejava.scanner.Token;
import org.rexcellentgames.byejava.scanner.TokenType;

import java.util.ArrayList;

public class Parser {
	private ArrayList<Token> tokens;
	private String code;
	private int current;

	public Parser(ArrayList<Token> tokens, String code) {
		this.setTokens(tokens, code);
	}

	public void setTokens(ArrayList<Token> tokens, String code) {
		this.tokens = tokens;
		this.code = code;
		this.current = 0;
	}

	private boolean isAtEnd() {
		return this.current >= this.tokens.size();
	}

	private Token peekPrevious() {
		return this.tokens.get(Math.max(0, this.current - 1));
	}

	private Token peek() {
		return this.tokens.get(this.current);
	}

	private Token peekNext() {
		return this.current > this.tokens.size() - 2 ?
			this.tokens.get(this.tokens.size() - 1) :
			this.tokens.get(this.current + 1);
	}

	private Token advance() {
		if (!this.isAtEnd()) {
			this.current++;
		}

		return this.tokens.get(this.current - 1);
	}

	private boolean match(TokenType ... types) {
		TokenType current = this.peek().type;

		for (TokenType type : types) {
			if (current == type) {
				this.advance();
				return true;
			}
		}

		return false;
	}

	private void error(String message) {
		System.out.println(message);
		// todo: sync
	}

	private Token consume(TokenType type, String error) {
		if (!this.match(type)) {
			System.out.println(this.peek().type);
			this.error(error);
		}

		return this.peekPrevious();
	}

	private Statement parsePackageStatement() {
		StringBuilder builder = new StringBuilder();

		while (true) {
			if (builder.length() > 0) {
				if (!this.match(TokenType.DOT)) {
					break;
				}

				builder.append('.');
			}

			Token name = this.consume(TokenType.IDENTIFIER, "Package name expected");
			builder.append(name.getLexeme(this.code));
		}

		this.consume(TokenType.SEMICOLON, "';' expected");
		return new Statement.Package(builder.toString());
	}

	private Statement parseClassStatement(Modifier modifier) {
		Token name = this.consume(TokenType.IDENTIFIER, "Class name expected");
		String base = null;
		ArrayList<String> implementations = null;
		ArrayList<Statement.Field> fields = null;

		boolean forceImplement = false;

		if (this.match(TokenType.EXTENDS)) {
			base = this.consume(TokenType.IDENTIFIER, "Class name expected").getLexeme(this.code);
		}

		if (this.match(TokenType.IMPLEMENTS)) {
			implementations = new ArrayList<>();

			while (true) {
				implementations.add(this.consume(TokenType.IDENTIFIER, "Class name expected").getLexeme(this.code));

				if (!this.match(TokenType.COMMA)) {
					break;
				}
			}
		}

		this.consume(TokenType.LEFT_BRACE, "'{' expected");
		this.consume(TokenType.RIGHT_BRACE, "'}' expected");

		return new Statement.Class(name.getLexeme(this.code), base, implementations, fields, modifier);
	}

	private Statement parseStatement(Modifier modifier) {
		TokenType type = this.advance().type;

		switch (type) {
			case CLASS: return parseClassStatement(modifier == null ? new Modifier() : modifier);

			case PUBLIC: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.access = Access.PUBLIC;
				return parseStatement(modifier);
			}

			case PROTECTED: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.access = Access.PROTECTED;
				return parseStatement(modifier);
			}

			case PRIVATE: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.access = Access.PRIVATE;
				return parseStatement(modifier);
			}

			case STATIC: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.isStatic = true;
				return parseStatement(modifier);
			}

			case FINAL: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.isFinal = true;
				return parseStatement(modifier);
			}
		}

		this.error("Statement expected, got " + type.toString().toLowerCase());
		return null;
	}

	private void tryAdd(ArrayList<Statement> statements, Statement statement) {
		if (statement != null) {
			statements.add(statement);
		}
	}

	public ArrayList<Statement> parse() {
		ArrayList<Statement> statements = new ArrayList<>();

		if (this.match(TokenType.PACKAGE)) {
			this.tryAdd(statements, this.parsePackageStatement());
		}

		while (!this.isAtEnd()) {
			this.tryAdd(statements, this.parseStatement(null));
		}

		return statements;
	}
}