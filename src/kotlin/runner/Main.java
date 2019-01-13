package kotlin.runner;

import kotlin.interpreter.Interpreter;
import kotlin.lexer.Lexer;
import kotlin.node.Start;
import kotlin.parser.Parser;

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
				System.out.println(e);
			}
		} else {
			System.exit(1);
		}
	}
}
