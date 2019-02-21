package org.rexcellentgames.byejava.parser;

import org.rexcellentgames.byejava.ast.*;
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
		return this.peek().type == TokenType.EOF;
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

	private Expression parsePrimary() {
		if (this.match(TokenType.FALSE)) {
			return new Expression.Literal(false);
		}

		if (this.match(TokenType.TRUE)) {
			return new Expression.Literal(true);
		}

		if (this.match(TokenType.NULL)) {
			return new Expression.Literal(null);
		}

		if (this.match(TokenType.STRING)) {
			return new Expression.Literal(this.peekPrevious().getLexeme(this.code));
		}

		if (this.match(TokenType.NUMBER)) {
			return new Expression.Literal(Double.parseDouble(this.peekPrevious().getLexeme(this.code)));
		}

		this.error("Unexpected token " + this.peek().type);
		return null;
	}

	private Expression parseExpression() {
		return this.parsePrimary();
	}

	private Statement parseStatement() {
		if (this.match(TokenType.LEFT_BRACE)) {
			return this.parseBlock();
		}

		return new Statement.Expr(this.parseExpression());
	}

	private Statement parseBlock() {
		ArrayList<Statement> statements = null;

		if (!this.match(TokenType.RIGHT_BRACE)) {
			statements = new ArrayList<>();

			while (true) {
				tryAdd(statements, this.parseStatement());

				if (this.match(TokenType.RIGHT_BRACE)) {
					break;
				}
			}
		}

		return new Statement.Block(statements);
	}

	private Statement parseField() {
		Modifier modifier = new Modifier();
		boolean found = false;

		while (true) {
			TokenType type = this.advance().type;

			switch (type) {
				case PUBLIC: modifier.access = Access.PUBLIC; break;
				case PROTECTED: modifier.access = Access.PROTECTED; break;
				case PRIVATE: modifier.access = Access.PRIVATE; break;
				case FINAL: modifier.isFinal = true; break;
				case STATIC: modifier.isStatic = true; break;
				case ABSTRACT: modifier.isAbstract = true; break;

				default: {
					found = true;
					break;
				}
			}

			if (found) {
				break;
			}
		}

		if (this.peekPrevious().type != TokenType.IDENTIFIER) {
			error("Field type expected");
		}

		String type = this.peekPrevious().getLexeme(this.code);
		String name = this.consume(TokenType.IDENTIFIER, "Field name expected").getLexeme(this.code);
		Expression init = null;

		if (this.match(TokenType.EQUAL)) {
			init = this.parseExpression();
		} else if (this.match(TokenType.LEFT_PAREN)) {
			ArrayList<Argument> arguments = new ArrayList<>();

			while (!this.match(TokenType.RIGHT_PAREN)) {
				String argumentType = this.consume(TokenType.IDENTIFIER, "Argument type expected").getLexeme(this.code);
				String argumentName = this.consume(TokenType.IDENTIFIER, "Argument name expected").getLexeme(this.code);

				arguments.add(new Argument(argumentName, argumentType));

				if (!this.match(TokenType.COMMA)) {
					this.consume(TokenType.RIGHT_PAREN, "')' expected");
					break;
				}
			}

			if (modifier.isAbstract) {
				this.consume(TokenType.SEMICOLON, "';' expected");
			} else {
				this.consume(TokenType.LEFT_BRACE, "'{' expected");
			}

			return new Statement.Method(null, type, name, modifier, modifier.isAbstract ? null : (Statement.Block) this.parseBlock(), arguments);
		}

		this.consume(TokenType.SEMICOLON, "';' expected");
		return new Statement.Field(init, type, name, modifier);
	}

	private Statement parseClassStatement(Modifier modifier) {
		Token name = this.consume(TokenType.IDENTIFIER, "Class name expected");
		String base = null;
		ArrayList<String> implementations = null;
		ArrayList<Statement.Field> fields = null;

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

		if (this.peek().type != TokenType.RIGHT_BRACE) {
			fields = new ArrayList<>();
		}

		while (!this.match(TokenType.RIGHT_BRACE)) {
			Statement statement = this.parseField();

			if (statement != null) {
				fields.add((Statement.Field) statement);
			}
		}

		return new Statement.Class(name.getLexeme(this.code), base, implementations, fields, modifier);
	}

	private Statement parseDeclaration(Modifier modifier) {
		TokenType type = this.advance().type;

		switch (type) {
			case CLASS: return parseClassStatement(modifier == null ? new Modifier() : modifier);

			case ABSTRACT: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.isAbstract = true;
				return parseDeclaration(modifier);
			}

			case PUBLIC: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.access = Access.PUBLIC;
				return parseDeclaration(modifier);
			}

			case PROTECTED: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.access = Access.PROTECTED;
				return parseDeclaration(modifier);
			}

			case PRIVATE: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.access = Access.PRIVATE;
				return parseDeclaration(modifier);
			}

			case STATIC: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.isStatic = true;
				return parseDeclaration(modifier);
			}

			case FINAL: {
				if (modifier == null) {
					modifier = new Modifier();
				}

				modifier.isFinal = true;
				return parseDeclaration(modifier);
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
			this.tryAdd(statements, this.parseDeclaration(null));
		}

		return statements;
	}
}