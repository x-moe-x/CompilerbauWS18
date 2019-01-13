package calculator.runner;

import calculator.interpreter.Interpreter;
import calculator.lexer.Lexer;
import calculator.node.Start;
import calculator.parser.Parser;

import java.io.FileReader;
import java.io.PushbackReader;

public class Main {
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				Lexer lexer = new Lexer(new PushbackReader(new FileReader(args[0]), 1024));
				Parser parser = new Parser(lexer);
				Start ast = parser.parse();
				Interpreter interpreter = new Interpreter();
				ast.apply(interpreter);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else {
			System.exit(1);
		}
	}
}
