package org.rexcellentgames.byejava;

import org.rexcellentgames.byejava.ast.Statement;
import org.rexcellentgames.byejava.emitter.Emitter;
import org.rexcellentgames.byejava.parser.Parser;
import org.rexcellentgames.byejava.renamer.Renamer;
import org.rexcellentgames.byejava.scanner.Scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ByeJava {
	private static int compiledOk;
	private static int errored;
	private static long startTime;
	private static int totalLines;

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

		String out = args[1];

		if (!out.endsWith("/")) {
			out = out + "/";
		}

		File file = new File(out);

		if (!file.exists()) {
			file.mkdirs();
		} else if (!file.isDirectory()) {
			System.out.println("Output file " + file.getPath() + " is not a directory");
			return;
		}

		startTime = System.currentTimeMillis();
		parseFile(new File(args[0]), out);
		log();
	}

	private static void log() {
		System.out.println(String.format("Totally compiled %d files, %f%% (%d) failed", errored + compiledOk, ((float) errored) / (errored + compiledOk) * 100, errored));
		System.out.println(String.format("Compiled in %d ms (%d lines totally)", (System.currentTimeMillis() - startTime), totalLines));
	}

	private static void parseFile(File file, String out) {
		if (!file.exists()) {
			System.out.println("Failed to open " + file.getPath());
			return;
		}

		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				parseFile(f, out);
			}
		} else if (file.getName().endsWith(".java")) {
			compile(file, out.replace(file.getParent(), "") + file.getPath().replace(".java", ".cs"));
		}
	}

	private static void compile(File file, String out) {
		System.out.print(String.format("Compiling %s... ", file.getPath()));

		long ms = System.currentTimeMillis();
		int lines = compileFile(file.getAbsolutePath(), out);

		if (lines == -1) {
			errored ++;
			System.out.println("Failed");
			log();
			System.exit(-1);
		} else {
			compiledOk ++;
			totalLines += lines;
			System.out.println(String.format("Compiled in %d ms (%d lines)", (System.currentTimeMillis() - ms), lines));
		}
	}

	public static int compileFile(String in, String to) {
		String code = getSource(in);
		Scanner scanner = new Scanner(code);
		Parser parser = new Parser(scanner.scan(), code);

		if (scanner.hadError) {
			return -1;
		}

		ArrayList<Statement> ast = parser.parse();

		if (parser.hadError) {
			return -1;
		}

		Renamer renamer = new Renamer();
		renamer.rename(ast);

		Emitter emitter = new Emitter(ast);

		File file = new File(to);
		file.getParentFile().mkdirs();

		try (PrintWriter out = new PrintWriter(to)) {
			out.println(emitter.emit());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return scanner.line;
	}
}