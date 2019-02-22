package org.rexcellentgames.byejava.parser;

import org.rexcellentgames.byejava.ast.*;
import org.rexcellentgames.byejava.scanner.Token;
import org.rexcellentgames.byejava.scanner.TokenType;

import java.util.ArrayList;
import java.util.HashMap;

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
		System.out.println(String.format("[line %d] %s", this.peek().line, message));
	}

	private Token consume(TokenType type, String error) {
		if (!this.match(type)) {
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

	private Statement parseImportStatement() {
		StringBuilder builder = new StringBuilder();

		while (true) {
			if (builder.length() > 0) {
				if (!this.match(TokenType.DOT)) {
					break;
				}

				builder.append('.');
			}

			if (this.match(TokenType.STAR)) {
				builder.append('*');
				break;
			}

			Token name = this.consume(TokenType.IDENTIFIER, "Package name expected");
			builder.append(name.getLexeme(this.code));
		}

		this.consume(TokenType.SEMICOLON, "';' expected");
		return new Statement.Import(builder.toString());
	}

	private ArrayList<Generetic> parseVarGenerics(boolean consumed) {
		ArrayList<Generetic> generetics = null;

		if (consumed || this.match(TokenType.LESS)) {
			generetics = new ArrayList<>();

			while (!this.match(TokenType.GREATER)) {
				Generetic generetic = new Generetic();
				generetic.name = this.consume(TokenType.IDENTIFIER, "Type name expected").getLexeme(this.code);
				generetics.add(generetic);

				if (this.peek().type != TokenType.GREATER) {
					this.consume(TokenType.COMMA, "',' expected");
				}
			}
		}

		return generetics;
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

		if (this.match(TokenType.CHAR)) {
			return new Expression.Literal(this.peekPrevious().getLexeme(this.code));
		}

		if (this.match(TokenType.NUMBER)) {
			return new Expression.Literal(this.peekPrevious().getLexeme(this.code));
		}

		if (this.match(TokenType.NEW)) {
			this.consume(TokenType.IDENTIFIER, "Class name expected");
			return new Expression.Variable(this.peekPrevious().getLexeme(this.code), parseVarGenerics(false), true);
		}

		if (this.match(TokenType.IDENTIFIER)) {
			return new Expression.Variable(this.peekPrevious().getLexeme(this.code), parseVarGenerics(false), false);
		}

		if (this.match(TokenType.LEFT_PAREN)) {
			Expression expression = this.parseExpression();
			this.consume(TokenType.RIGHT_PAREN, "')' expected");
			return new Expression.Grouping(expression);
		}

		if (this.match(TokenType.THIS)) {
			return new Expression.This();
		}

		if (this.match(TokenType.SUPER)) {
			return new Expression.Super();
		}

		this.error("Unexpected token " + this.peek().type);
		return null;
	}

	private Expression finishCall(Expression expression) {
		ArrayList<Expression> arguments = null;

		if (this.peek().type != TokenType.RIGHT_PAREN) {
			arguments = new ArrayList<>();

			do {
				arguments.add(this.parseExpression());
			} while (this.match(TokenType.COMMA));
		}

		this.consume(TokenType.RIGHT_PAREN, "')' expected");
		return new Expression.Call(expression, arguments);
	}

	private Expression parseCall() {
		Expression expression = this.parsePrimary();

		while (true) {
			boolean construct = this.match(TokenType.NEW);

			if (construct || this.match(TokenType.LEFT_PAREN)) {
				if (construct) {
					this.consume(TokenType.LEFT_PAREN, "'(' expected");
				}

				expression = this.finishCall(expression);
			} else if (this.match(TokenType.DOT)) {
				expression = new Expression.Get(expression, this.consume(TokenType.IDENTIFIER, "Field name expected").getLexeme(this.code));
			} else if (this.match(TokenType.LEFT_BRACKET)) {
				Expression index = this.parseExpression();
				this.consume(TokenType.RIGHT_BRACKET, "']' expected");

				expression = new Expression.Index(expression, index);
			} else {
				break;
			}
		}

		return expression;
	}

	private Expression parseDeunary() {
		Expression expression = this.parseCall();

		if (this.match(TokenType.MINUS_MINUS, TokenType.PLUS_PLUS)) {
			TokenType operator = this.peekPrevious().type;
			expression = new Expression.Deunary(operator, expression);
		}

		return expression;
	}

	private Expression parseUnary() {
		if (this.match(TokenType.MINUS, TokenType.BANG)) {
			TokenType operator = this.peekPrevious().type;
			return new Expression.Unary(operator, this.parseUnary());
		}

		return this.parseDeunary();
	}

	private Expression parseMultiplication() {
		Expression expression = this.parseUnary();

		while (this.match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
			TokenType operator = this.peekPrevious().type;
			expression = new Expression.Binary(operator, expression, this.parseUnary());
		}

		return expression;
	}

	private Expression parseAddition() {
		Expression expression = this.parseMultiplication();

		while (this.match(TokenType.PLUS, TokenType.MINUS)) {
			TokenType operator = this.peekPrevious().type;
			expression = new Expression.Binary(operator, expression, this.parseMultiplication());
		}

		return expression;
	}

	private Expression parseComparison() {
		Expression expression = this.parseAddition();

		while (this.match(TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.LESS)) {
			TokenType operator = this.peekPrevious().type;
			expression = new Expression.Binary(operator, expression, this.parseAddition());
		}

		return expression;
	}

	private Expression parseEquality() {
		Expression expression = this.parseComparison();

		while (this.match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
			TokenType operator = this.peekPrevious().type;
			expression = new Expression.Binary(operator, expression, this.parseComparison());
		}

		return expression;
	}

	private Expression parseAnd() {
		Expression expression = this.parseEquality();

		while (this.match(TokenType.AND)) {
			TokenType operator = this.peekPrevious().type;
			expression = new Expression.Binary(operator, expression, this.parseEquality());
		}

		return expression;
	}

	private Expression parseOr() {
		Expression expression = this.parseAnd();

		while (this.match(TokenType.OR)) {
			TokenType operator = this.peekPrevious().type;
			expression = new Expression.Binary(operator, expression, this.parseAnd());
		}

		return expression;
	}

	private Expression parseAssignment() {
		Expression expression = this.parseOr();

		if (this.match(TokenType.EQUAL)) {
			Expression value = this.parseAssignment();

			if (expression instanceof Expression.Variable || expression instanceof Expression.Index) {
				return new Expression.Assign(expression, value);
			} else if (expression instanceof Expression.Get) {
				Expression.Get get = (Expression.Get) expression;
				return new Expression.Set(get.from, value, get.field);
			}

			this.error("Invalid assignment target.");
		}

		return expression;
	}

	private Expression parseExpression() {
		Expression expression = this.parseAssignment();

		if (this.match(TokenType.QUESTION)) {
			Expression ifBranch = this.parseExpression();
			this.consume(TokenType.COLON, "':' expected");
			Expression elseBranch = this.parseExpression();

			expression = new Expression.If(expression, ifBranch, elseBranch);
		}

		return expression;
	}

	private Statement parseReturn() {
		Expression value = null;

		if (!this.match(TokenType.SEMICOLON)) {
			value = this.parseExpression();
			this.consume(TokenType.SEMICOLON, "';' expected");
		}

		return new Statement.Return(value);
	}

	private Statement parseIf() {
		Expression ifCondition = this.parseExpression();
		Statement ifBranch = this.parseStatement();
		ArrayList<Expression> ifElseConditions = null;
		ArrayList<Statement> ifElseBranches = null;
		Statement elseBranch = null;

		while (this.match(TokenType.ELSE)) {
			if (this.match(TokenType.IF)) {
				if (ifElseBranches == null) {
					ifElseConditions = new ArrayList<>();
					ifElseBranches = new ArrayList<>();
				}

				ifElseConditions.add(this.parseExpression());
				ifElseBranches.add(this.parseStatement());
			} else {
				elseBranch = this.parseStatement();
				break;
			}
		}

		return new Statement.If(ifCondition, ifBranch, ifElseConditions, ifElseBranches, elseBranch);
	}

	private Statement parseFor() {
		Statement init = null;
		Expression condition = null;
		Expression increment = null;

		this.consume(TokenType.LEFT_PAREN, "'(' expected");

		if (!this.match(TokenType.SEMICOLON)) {
			if (this.peek().type == TokenType.IDENTIFIER && this.peekNext().type == TokenType.IDENTIFIER) {
				init = this.parseVariable(false);

				if (this.match(TokenType.COLON)) {
					condition = this.parseExpression();
					this.consume(TokenType.RIGHT_PAREN, "')' expected");

					return new Statement.Foreach(init, condition, this.parseStatement());
				}
			} else {
				init = new Statement.Expr(this.parseExpression());
				this.consume(TokenType.SEMICOLON, "';' expected");
			}
		}

		if (!this.match(TokenType.SEMICOLON)) {
			condition = this.parseExpression();
			this.consume(TokenType.SEMICOLON, "';' expected");
		}

		if (!this.match(TokenType.RIGHT_PAREN)) {
			increment = this.parseExpression();
			this.consume(TokenType.RIGHT_PAREN, "')' expected");
		}

		Statement body = this.parseStatement();

		return new Statement.For(init, condition, increment, body);
	}

	private Statement parseVariable(boolean end) {
		String type = this.consume(TokenType.IDENTIFIER, "Variable type expected").getLexeme(this.code);
		ArrayList<Generetic> generetics = null;

		if (this.match(TokenType.LESS)) {
			generetics = new ArrayList<>();

			while (true) {
				Generetic generetic = new Generetic();
				generetics.add(generetic);
				generetic.name = this.consume(TokenType.IDENTIFIER, "Type name expected").getLexeme(this.code);

				if (this.match(TokenType.GREATER)) {
					break;
				}

				this.consume(TokenType.COMMA, "',' expected");
			}
		}

		String name = this.consume(TokenType.IDENTIFIER, "Variable name expected").getLexeme(this.code);
		Expression init = null;

		if (end && !this.match(TokenType.SEMICOLON)) {
			this.consume(TokenType.EQUAL, "'=' expected");
			init = this.parseExpression();
			this.consume(TokenType.SEMICOLON, "';' expected");
		}

		return new Statement.Var(new Argument(name, type, generetics, false), init);
	}

	private Statement parseDo() {
		Statement body = this.parseStatement();
		this.consume(TokenType.WHILE, "'while' expected");
		this.consume(TokenType.LEFT_PAREN, "'(' expected");
		Expression condition = this.parseExpression();
		this.consume(TokenType.RIGHT_PAREN, "')' expected");
		this.consume(TokenType.SEMICOLON, "';' expected");

		return new Statement.While(condition, body, true);
	}

	private Statement parseWhile() {
		this.consume(TokenType.LEFT_PAREN, "'(' expected");
		Expression condition = this.parseExpression();
		this.consume(TokenType.RIGHT_PAREN, "')' expected");
		Statement body = this.parseStatement();

		return new Statement.While(condition, body, false);
	}

	private Statement parseSwitch() {
		this.consume(TokenType.LEFT_PAREN, "'(' expected");
		Expression what = this.parseExpression();
		this.consume(TokenType.RIGHT_PAREN, "')' expected");
		this.consume(TokenType.LEFT_BRACE, "'{' expected");

		ArrayList<Statement.SwitchBranch> branches = null;

		while (!this.match(TokenType.RIGHT_BRACE)) {
			if (branches == null) {
				branches = new ArrayList<>();
			}

			Statement.SwitchBranch branch = new Statement.SwitchBranch();
			branches.add(branch);

			while (true) {
				if (branch.cases == null) {
					branch.cases = new ArrayList<>();
				}

				if (!this.match(TokenType.DEFAULT)) {
					this.consume(TokenType.CASE, "'case' expected");
					branch.cases.add(this.parseExpression());
				} else {
					branch.cases.add(null);
				}

				this.consume(TokenType.COLON, "':' expected");

				if (this.peek().type != TokenType.CASE && this.peek().type != TokenType.DEFAULT) {
					if (this.match(TokenType.LEFT_BRACE)) {
						branch.block = (Statement.Block) this.parseBlock();
					} else {
						ArrayList<Statement> list = new ArrayList<>();
						list.add(this.parseStatement());

						branch.block = new Statement.Block(list);
					}

					break;
				}
			}
		}

		return new Statement.Switch(branches, what);
	}

	private Statement parseStatement() {
		if (this.match(TokenType.LEFT_BRACE)) {
			return this.parseBlock();
		}

		if (this.peek().type == TokenType.IDENTIFIER) {
			int i = this.current;
			int balance = 0;

			while (true) {
				i++;

				if (i >= this.tokens.size()) {
					break;
				}

				TokenType type = this.tokens.get(i).type;

				if (type == TokenType.IDENTIFIER) {
					if (balance == 0) {
						return this.parseVariable(true);
					}
				} else if (type == TokenType.LESS) {
					balance++;
				} else if (type == TokenType.GREATER) {
					balance--;
				} else if (type != TokenType.COMMA) {
					break;
				}
			}
		}

		if (this.match(TokenType.SWITCH)) {
			return this.parseSwitch();
		}

		if (this.match(TokenType.IF)) {
			return this.parseIf();
		}

		if (this.match(TokenType.FOR)) {
			return this.parseFor();
		}

		if (this.match(TokenType.RETURN)) {
			return this.parseReturn();
		}

		if (this.match(TokenType.DO)) {
			return this.parseDo();
		}

		if (this.match(TokenType.WHILE)) {
			return this.parseWhile();
		}

		if (this.match(TokenType.BREAK)) {
			this.consume(TokenType.SEMICOLON, "';' expected");
			return new Statement.Break();
		}

		if (this.match(TokenType.CONTINUE)) {
			this.consume(TokenType.SEMICOLON, "';' expected");
			return new Statement.Continue();
		}

		Statement statement = new Statement.Expr(this.parseExpression());
		this.consume(TokenType.SEMICOLON, "';' expected");

		return statement;
	}

	private Statement parseEnumStatement(Modifier modifier) {
		ArrayList<String> values = null;
		HashMap<String, Expression.Literal> init = null;

		String enumName = this.consume(TokenType.IDENTIFIER, "Enum name expected").getLexeme(this.code);
		this.consume(TokenType.LEFT_BRACE, "'{' expected");

		if (this.peek().type != TokenType.RIGHT_BRACE) {
			while (true) {
				if (values == null) {
					values = new ArrayList<>();
				}

				String name = this.consume(TokenType.IDENTIFIER, "Enum field name expected").getLexeme(this.code);
				values.add(name);

				if (this.match(TokenType.EQUAL)) {
					String value = this.consume(TokenType.NUMBER, "Number expected").getLexeme(this.code);

					if (init == null) {
						init = new HashMap<>();
					}

					init.put(name, new Expression.Literal(Integer.parseInt(value)));
				}

				if (!this.match(TokenType.COMMA)) {
					break;
				}
			}
		}

		this.consume(TokenType.RIGHT_BRACE, "'}' expected");

		return new Statement.Enum(modifier, enumName, values, init);
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
		ArrayList<Generetic> gen = null;

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
					if (type == TokenType.LESS) {
						gen = this.parseVarGenerics(true);
						this.advance();
					}

					found = true;
					break;
				}
			}

			if (found) {
				break;
			}
		}

		if (this.peekPrevious().type == TokenType.CLASS) {
			return parseClassStatement(modifier);
		}

		if (this.peekPrevious().type == TokenType.ENUM) {
			return parseEnumStatement(modifier);
		}

		if (this.peekPrevious().type != TokenType.IDENTIFIER) {
			error("Field type expected");
		}

		String type = this.peekPrevious().getLexeme(this.code);
		String name = null;

		if (this.peek().type == TokenType.IDENTIFIER) {
			name = this.advance().getLexeme(this.code);
		} else {
			name = type;
			type = null;
		}

		Expression init = null;

		if (this.match(TokenType.EQUAL)) {
			init = this.parseExpression();
		} else if (this.match(TokenType.LEFT_PAREN)) {
			ArrayList<Argument> arguments = new ArrayList<>();

			while (!this.match(TokenType.RIGHT_PAREN)) {
				String argumentType = this.consume(TokenType.IDENTIFIER, "Argument type expected").getLexeme(this.code);
				ArrayList<Generetic> generetics = null;

				if (this.match(TokenType.LESS)) {
					generetics = new ArrayList<>();

					while (true) {
						Generetic generetic = new Generetic();
						generetic.name = this.consume(TokenType.IDENTIFIER, "Type name expected").getLexeme(this.code);

						if (this.match(TokenType.GREATER)) {
							break;
						}

						this.consume(TokenType.COMMA, "',' expected");
					}
				}

				boolean varg = this.match(TokenType.DOT_DOT_DOT);
				String argumentName = this.consume(TokenType.IDENTIFIER, "Argument name expected").getLexeme(this.code);

				arguments.add(new Argument(argumentName, argumentType, generetics, varg));

				if (varg || !this.match(TokenType.COMMA)) {
					this.consume(TokenType.RIGHT_PAREN, "')' expected");
					break;
				}
			}

			if (modifier.isAbstract) {
				this.consume(TokenType.SEMICOLON, "';' expected");
			} else {
				this.consume(TokenType.LEFT_BRACE, "'{' expected");
			}

			return new Statement.Method(null, type, name, modifier, modifier.isAbstract ? null : (Statement.Block) this.parseBlock(), arguments, gen);
		}

		this.consume(TokenType.SEMICOLON, "';' expected");
		return new Statement.Field(init, type, name, modifier);
	}

	private Statement parseClassStatement(Modifier modifier) {
		Token name = this.consume(TokenType.IDENTIFIER, "Class name expected");
		String base = null;
		ArrayList<String> implementations = null;
		ArrayList<Statement.Field> fields = null;
		ArrayList<Statement.Method> methods = null;
		ArrayList<Statement> inner = null;
		ArrayList<Statement.Block> init = null;
		ArrayList<Generetic> generetics = null;

		if (this.match(TokenType.LESS)) {
			generetics = new ArrayList<>();

			while (true) {
				Generetic generetic = new Generetic();
				generetics.add(generetic);

				generetic.name = this.consume(TokenType.IDENTIFIER, "Type name expected").getLexeme(this.code);

				if (this.match(TokenType.EXTENDS)) {
					generetic.extend = this.consume(TokenType.IDENTIFIER, "Class name expected").getLexeme(this.code);
				}

				if (this.match(TokenType.GREATER)) {
					break;
				}

				this.consume(TokenType.COMMA, "',' expected");
			}
		}

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

		while (!this.match(TokenType.RIGHT_BRACE)) {
			boolean override = this.match(TokenType.OVERRIDE);

			if (this.peek().type == TokenType.STATIC && this.peekNext().type == TokenType.LEFT_BRACE) {
				this.advance();
				this.advance();

				if (inner == null) {
					inner = new ArrayList<>();
				}

				inner.add(this.parseBlock());
				continue;
			}

			if (this.match(TokenType.LEFT_BRACE)) {
				if (init == null) {
					init = new ArrayList<>();
				}

				init.add((Statement.Block) this.parseBlock());
				continue;
			}

			Statement statement = this.parseField();

			if (statement != null) {
				if (statement instanceof Statement.Class || statement instanceof Statement.Enum) {
					if (inner == null) {
						inner = new ArrayList<>();
					}

					inner.add(statement);
				} else if (statement instanceof Statement.Method) {
					if (methods == null) {
						methods = new ArrayList<>();
					}

					if (override) {
						((Statement.Method) statement).override = true;
					}

					methods.add((Statement.Method) statement);
				} else {
					if (fields == null) {
						fields = new ArrayList<>();
					}

					fields.add((Statement.Field) statement);
				}
			}
		}

		return new Statement.Class(name.getLexeme(this.code), base, implementations, fields, modifier, methods, inner, init, generetics);
	}

	private Statement parseDeclaration(Modifier modifier) {
		TokenType type = this.advance().type;

		switch (type) {
			case CLASS: return parseClassStatement(modifier == null ? new Modifier() : modifier);
			case ENUM: return parseEnumStatement(modifier == null ? new Modifier() : modifier);

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

		while (this.match(TokenType.IMPORT)) {
			this.tryAdd(statements, this.parseImportStatement());
		}

		while (!this.isAtEnd()) {
			this.tryAdd(statements, this.parseDeclaration(null));
		}

		return statements;
	}
}