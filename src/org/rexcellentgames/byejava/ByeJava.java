package org.rexcellentgames.byejava;

import org.rexcellentgames.byejava.scanner.ErrorToken;
import org.rexcellentgames.byejava.scanner.Scanner;
import org.rexcellentgames.byejava.scanner.Token;

public class ByeJava {
	public static void main(String[] args) {
		String source = "class _hello32Test 32.132 \"test\" + - += *";
		Scanner scanner = new Scanner(source);

		for (Token token : scanner.scan()) {
			if (token instanceof ErrorToken) {
				System.out.println(((ErrorToken) token).error);
			} else {
				System.out.println(token.type);
			}
		}
	}
}