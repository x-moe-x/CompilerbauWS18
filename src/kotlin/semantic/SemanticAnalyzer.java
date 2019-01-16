package kotlin.semantic;

import kotlin.analysis.DepthFirstAdapter;
import kotlin.node.*;

import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer extends DepthFirstAdapter {
	enum VarType {
		Int("Int"), Boolean("Boolean");

		private final String name;

		VarType(String name) {
			this.name = name;
		}
	}

	private final Map<String, VarType> symbolTable = new HashMap<>();
	private boolean hasErrors = false;

	@Override
	public void outAVarDeclaration(AVarDeclaration node) {
		TIdentifier identifier = node.getIdentifier();

		String name = identifier.getText();
		VarType type = VarType.valueOf(node.getType().getText());

		if (symbolTable.containsKey(name)) {
			setError(identifier, "variable " + name + " already declared");
		}
		symbolTable.put(name, type);
	}

	@Override
	public void outAAssignmentStatement(AAssignmentStatement node) {
		enforceIndentifierIsDeclared(node.getIdentifier());
	}

	@Override
	public void caseAIdentifierAtomicExpression(AIdentifierAtomicExpression node) {
		enforceIndentifierIsDeclared(node.getIdentifier());
	}

	private void enforceIndentifierIsDeclared(TIdentifier identifier) {
		String name = identifier.getText();
		if (!symbolTable.containsKey(name)) {
			setError(identifier, "variable " + name + " not declared");
		}
	}

	private void setError(Token token, String message) {
		System.err.println("Error: [" + token.getLine() + "," + token.getPos() + "] " + message);
		hasErrors = true;
	}
}
