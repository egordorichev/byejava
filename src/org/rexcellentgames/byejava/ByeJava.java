package org.rexcellentgames.byejava;

import org.rexcellentgames.byejava.ast.Statement;
import org.rexcellentgames.byejava.emitter.Emitter;
import org.rexcellentgames.byejava.parser.Parser;
import org.rexcellentgames.byejava.scanner.Scanner;
import org.rexcellentgames.byejava.scanner.Token;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ByeJava {
	private static String getSource() {
		try {
			return new String(Files.readAllBytes(Paths.get("src/test/Test.java")), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "error";
	}

	public static void main(String[] args) {
		String code = getSource();
		Scanner scanner = new Scanner(code);
		ArrayList<Token> tokens = scanner.scan();

		/*
		for (Token token : tokens) {
			System.out.println(token.type);
		}
		*/

		System.out.println("=================");

		Parser parser = new Parser(tokens, code);
		ArrayList<Statement> ast = parser.parse();

		for (Statement statement : ast) {
			System.out.println(statement.getClass().getSimpleName());
		}

		Emitter emitter = new Emitter(ast);

		try (PrintWriter out = new PrintWriter("src/test/Test.cs")) {
			out.println(emitter.emit());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}