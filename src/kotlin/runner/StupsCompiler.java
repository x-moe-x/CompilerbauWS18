package kotlin.runner;

import kotlin.compiler.CodeGenerator;
import kotlin.compiler.TypeChecker;
import kotlin.lexer.Lexer;
import kotlin.node.Start;
import kotlin.parser.Parser;

import java.io.FileReader;
import java.io.PushbackReader;

public class StupsCompiler {
	public static void main(String[] args) {
		if (args.length == 2 && args[0].equals("-compile")) {
			try {
				Lexer lexer = new Lexer(new PushbackReader(new FileReader(args[1]), 1024));
				Parser parser = new Parser(lexer);
				Start ast = parser.parse();
				TypeChecker typeChecker = new TypeChecker();
				ast.apply(typeChecker);

				if (!typeChecker.hasErrors()) {
					CodeGenerator generator = new CodeGenerator(typeChecker);
					ast.apply(generator);

					System.out.println(generator.toString());
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		} else if (args.length == 2 && args[0].equals("-liveness")) {
			//no op
		} else {
			System.exit(1);
		}
	}
}
