package org.rexcellentgames.byejava;

import org.rexcellentgames.byejava.emitter.Emitter;
import org.rexcellentgames.byejava.parser.Parser;
import org.rexcellentgames.byejava.scanner.Scanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ByeJava {
	private static String getSource(String file) {
		try {
			return new String(Files.readAllBytes(Paths.get(file)), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "error";
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("byejava [input] [output]");
			return;
		}

		// todo: support directories
		long ms = System.currentTimeMillis();

		if (!compileFile(args[0], args[1])) {
			System.out.println("Failed to compile " + args[1]);
		} else {
			System.out.println(args[1] + " compiled in " + (System.currentTimeMillis() - ms) + " ms");
		}
	}

	public static boolean compileFile(String in, String to) {
		String code = getSource(in);
		Scanner scanner = new Scanner(code);
		Parser parser = new Parser(scanner.scan(), code);

		if (scanner.hadError) {
			return false;
		}

		Emitter emitter = new Emitter(parser.parse());

		if (parser.hadError) {
			return false;
		}

		try (PrintWriter out = new PrintWriter(to)) {
			out.println(emitter.emit());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return true;
	}
}