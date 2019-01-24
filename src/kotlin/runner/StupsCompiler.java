package kotlin.runner;

import kotlin.compiler.CodeGenerator;
import kotlin.compiler.TypeChecker;
import kotlin.lexer.Lexer;
import kotlin.node.Start;
import kotlin.parser.Parser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
					String moduleName = extractModuleName(args[1]);
					String modulePath = extractModulePath(args[1]);
					CodeGenerator generator = new CodeGenerator(typeChecker, moduleName);
					ast.apply(generator);

					FileWriter fileWriter = new FileWriter(modulePath + "/" + moduleName + ".j");
					fileWriter.write(generator.toString());
					fileWriter.close();
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

	private static String extractModulePath(String filename) {
		File f = new File(filename);
		String name = f.getName();
		return f.getPath().replace(name, "");
	}

	private static String extractModuleName(String filename) {
		File f = new File(filename);
		String name = f.getName();
		return name.replace(".kt", "");
	}
}
