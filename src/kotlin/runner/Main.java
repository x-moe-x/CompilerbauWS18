package kotlin.runner;

import kotlin.semantic.SemanticAnalyzer;
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
				SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
				ast.apply(semanticAnalyzer);
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			System.exit(1);
		}
	}
}
