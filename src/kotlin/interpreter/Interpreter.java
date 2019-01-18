package kotlin.interpreter;

import kotlin.analysis.DepthFirstAdapter;
import kotlin.node.*;

import java.util.HashMap;
import java.util.Map;

public class Interpreter extends DepthFirstAdapter {
	enum Type {
		Int, Bool;

		static Type getType(PType type) {
			return type instanceof ABoolType ? Bool : Int;
		}
	}

	private Map<String, Type> symbolTable = new HashMap<>();
	private Map<PExpr, Type> expressionTypes = new HashMap<>();

	@Override
	public void outADeclarationStmt(ADeclarationStmt node) {
		TIdentifier identifier = node.getVar();
		String var = identifier.getText();

		if (symbolTable.containsKey(var)) {
			setError(identifier, "Variable '" + var + "' is already defined");
			return;
		}

		Type type = Type.getType(node.getType());
		symbolTable.put(var, type);

		Type exprType = expressionTypes.get(node.getExpr());
		if (exprType != type) {
			setError(identifier, "Incompatible types, required " + type + ", found " + exprType);
		}
	}

	@Override
	public void outAAssignStmt(AAssignStmt node) {
		TIdentifier identifier = node.getVar();
		String var = identifier.getText();

		if (!symbolTable.containsKey(var)) {
			setError(identifier, "Cannot resolve symbol " + var);
			return;
		}

		Type type = symbolTable.get(var);
		Type exprType = expressionTypes.get(node.getExpr());
		if (exprType != type) {
			setError(identifier, "Incompatible types, required " + type + ", found " + exprType);
		}
	}

	@Override
	public void outAWhileStmt(AWhileStmt node) {
		Type conditionType = expressionTypes.get(node.getCondition());
		if (conditionType != Type.Bool) {
			setError(null, "Incompatible types, required Bool, found " + conditionType);
		}
	}

	@Override
	public void outAIfStmt(AIfStmt node) {
		Type conditionType = expressionTypes.get(node.getCondition());
		if (conditionType != Type.Bool) {
			setError(null, "Incompatible types, required Bool, found " + conditionType);
		}
	}

	@Override
	public void outAVariableExpr(AVariableExpr node) {
		TIdentifier identifier = node.getIdentifier();
		String var = identifier.getText();

		if (!symbolTable.containsKey(var)) {
			setError(identifier, "Cannot resolve symbol " + var);
			return;
		}

		Type type = symbolTable.get(var);
		expressionTypes.put(node, type);
	}

	@Override
	public void outAIntegerLiteralExpr(AIntegerLiteralExpr node) {
		expressionTypes.put(node, Type.Int);
	}

	@Override
	public void outABooleanLiteralExpr(ABooleanLiteralExpr node) {
		expressionTypes.put(node, Type.Bool);
	}

	@Override
	public void outAUnaryExpr(AUnaryExpr node) {
		Type requiredType = node.getOp() instanceof ANotUnop ? Type.Bool : Type.Int;
		Type exprType = expressionTypes.get(node.getR());

		if (exprType != requiredType) {
			setError(null, "Incompatible types, required " + requiredType + ", found " + exprType);
			return;
		}

		expressionTypes.put(node, requiredType);
	}

	@Override
	public void outABinaryExpr(ABinaryExpr node) {
		Type leftType = expressionTypes.get(node.getL());
		Type rightType = expressionTypes.get(node.getR());

		if (leftType != rightType) {
			setError(null, "Incompatible types, required " + leftType + ", found " + rightType);
			return;
		}

		PBinop binop = node.getOp();
		// int required, int result
		if (binop instanceof APlusBinop
				|| binop instanceof AMinusBinop
				|| binop instanceof AMulBinop
				|| binop instanceof ADivBinop
				|| binop instanceof AModBinop) {
			if (leftType != Type.Int) {
				setError(null, "Incompatible types, required " + Type.Int + ", found " + leftType);
				return;
			}
			expressionTypes.put(node, Type.Int);
		}
		// int required, bool result
		else if (binop instanceof ALtBinop
				|| binop instanceof AGtBinop
				|| binop instanceof ALeqBinop
				|| binop instanceof AGeqBinop) {
			if (leftType != Type.Int) {
				setError(null, "Incompatible types, required " + Type.Int + ", found " + leftType);
				return;
			}
			expressionTypes.put(node, Type.Bool);

		}
		// works for both, bool result
		else if (binop instanceof ANotEqualsBinop
				|| binop instanceof AEqualsBinop) {
			expressionTypes.put(node, Type.Bool);
		}
		// requires bool
		else if (binop instanceof AAndBinop
				|| binop instanceof AOrBinop) {
			if (leftType != Type.Bool) {
				setError(null, "Incompatible types, required " + Type.Bool + ", found " + leftType);
				return;
			}
			expressionTypes.put(node, Type.Bool);
		}
	}

	private void setError(Token token, String message) {
		if (token != null) {
			System.err.println("Error [" + token.getLine() + "," + token.getPos() + "] " + message);
		} else {
			System.err.println("Error " + message);
		}
	}
}