package org.rexcellentgames.byejava;

import org.rexcellentgames.byejava.ast.Statement;
import org.rexcellentgames.byejava.parser.Parser;
import org.rexcellentgames.byejava.scanner.Scanner;
import org.rexcellentgames.byejava.scanner.Token;

import java.io.IOException;
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

		Parser parser = new Parser(tokens, code);
		System.out.println("=================");

		for (Statement statement : parser.parse()) {
			System.out.println(statement.getClass().getSimpleName());
		}
	}
}