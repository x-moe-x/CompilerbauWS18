package kotlin.compiler;

import kotlin.analysis.DepthFirstAdapter;
import kotlin.node.*;

import java.util.LinkedList;

public class CodeGenerator extends DepthFirstAdapter {
	private final TypeChecker checker;
	private final String moduleName;

	private int labelNr = 0;
	private int maxStack = 0;
	private final CodeWriter writer = new CodeWriter();

	enum Binop {
		mul("imul"), div("idiv"), mod("irem"), plus("iadd"), minus("isub"),
		lt("if_icmplt"), gt("if_icmpgt"), leq("if_icmpgt"), geq("if_icmpgt"), equals("if_icmpgt"), not_equals("if_icmpgt"), and("if_icmpgt"), or("if_icmpgt");

		private final String code;

		Binop(String code) {
			this.code = code;
		}

		static Binop getOp(PBinop binop) {
			if (binop instanceof AMulBinop) {
				return mul;
			} else if (binop instanceof ADivBinop) {
				return div;
			} else if (binop instanceof AModBinop) {
				return mod;
			} else if (binop instanceof APlusBinop) {
				return plus;
			} else if (binop instanceof AMinusBinop) {
				return minus;
			} else if (binop instanceof ALtBinop) {
				return lt;
			} else if (binop instanceof AGtBinop) {
				return gt;
			} else if (binop instanceof ALeqBinop) {
				return leq;
			} else if (binop instanceof AGeqBinop) {
				return geq;
			} else if (binop instanceof AEqualsBinop) {
				return equals;
			} else if (binop instanceof ANotEqualsBinop) {
				return not_equals;
			} else if (binop instanceof AAndBinop) {
				return and;
			} else {
				return or;
			}
		}

		public String getCode() {
			return code;
		}

	}

	public CodeGenerator(TypeChecker checker, String moduleName) {
		this.checker = checker;
		this.moduleName = moduleName;
	}

	@Override
	public void outAFunction(AFunction node) {
		generateStatements(node.getDeclarations(), 0);
		generateStatements(node.getStatements(), 0);
		writer.prependBoilerplateInit(moduleName, maxStack, checker.getNrOfVars());
		writer.writeBoilerplateExit();
	}

	private void generateStatements(LinkedList<PStmt> stmts, int currentStack) {
		for (PStmt stmt : stmts) {
			generateStatement(stmt, currentStack);
		}
	}

	private void generateStatement(PStmt stmt, int currentStack) {
		if (stmt instanceof ADeclarationStmt) {
			generateDeclaration((ADeclarationStmt) stmt, currentStack);
		} else if (stmt instanceof AWhileStmt) {
			generateWhile((AWhileStmt) stmt, currentStack);
		} else if (stmt instanceof AIfStmt) {
			generateIf((AIfStmt) stmt, currentStack);
		} else if (stmt instanceof AAssignStmt) {
			generateAssign((AAssignStmt) stmt, currentStack);
		} else if (stmt instanceof APrintStmt) {
			generatePrint((APrintStmt) stmt, currentStack);
		} else if (stmt instanceof ABlockStmt) {
			generateBlock((ABlockStmt) stmt, currentStack);
		} else {
			System.err.println("not implemented for " + stmt.getClass().getSimpleName());
		}
	}

	private void generateBlock(ABlockStmt stmt, int currentStack) {
		generateStatements(stmt.getStatements(), currentStack);
	}

	private void generatePrint(APrintStmt stmt, int currentStack) {
		writer.writeOut();
		generateExpr(stmt.getMessage(), currentStack);
		writer.writePrintln();
	}

	private void generateAssign(AAssignStmt stmt, int currentStack) {
		generateExpr(stmt.getExpr(), currentStack);
		int varNr = checker.getVarNr(stmt.getVar());
		writer.writeIStore(varNr);
	}

	private void generateIf(AIfStmt stmt, int currentStack) {
		String L1 = "FalseBranch" + labelNr;
		String L2 = "EndIf" + labelNr++;
		generateExpr(stmt.getCondition(), currentStack);
		writer.writeIfeq(L1);
		generateStatements(stmt.getTrueStatements(), currentStack);
		writer.writeGoto(L2);
		writer.writeLabel(L1);
		generateStatements(stmt.getFalseStatements(), currentStack);
		writer.writeLabel(L2);
	}

	private void generateWhile(AWhileStmt stmt, int currentStack) {
		String L1 = "StartWhile" + labelNr;
		String L2 = "EndWhile" + labelNr++;
		writer.writeLabel(L1);
		generateExpr(stmt.getCondition(), currentStack);
		writer.writeIfeq(L2);
		generateStatements(stmt.getStatements(), 0);
		writer.writeGoto(L1);
		writer.writeLabel(L2);
	}


	private void generateDeclaration(ADeclarationStmt stmt, int currentStack) {
		generateExpr(stmt.getExpr(), currentStack);
		writer.writeIStore(checker.getVarNr(stmt.getVar()));
		updateMaxStack(currentStack + 1);
	}

	private void updateMaxStack(int currentStack) {
		if (currentStack > maxStack) {
			maxStack = currentStack;
		}
	}

	private void generateExpr(PExpr expr, int currentStack) {
		if (expr instanceof AIntegerLiteralExpr) {
			generateIntegerLiteral((AIntegerLiteralExpr) expr, currentStack);
		} else if (expr instanceof ABooleanLiteralExpr) {
			generateBooleanLiteral((ABooleanLiteralExpr) expr, currentStack);
		} else if (expr instanceof ABinaryExpr) {
			generateBinaryExpression((ABinaryExpr) expr, currentStack);
		} else if (expr instanceof AVariableExpr) {
			generateVariableExpression((AVariableExpr) expr, currentStack);
		} else {
			System.err.println("not implemented for " + expr.getClass().getSimpleName());
		}
	}

	private void generateVariableExpression(AVariableExpr expr, int currentStack) {
		int varNr = checker.getVarNr(expr.getIdentifier());
		writer.writeILoad(varNr);
		updateMaxStack(currentStack + 1);
	}

	private void generateBinaryExpression(ABinaryExpr expr, int currentStack) {
		Binop op = Binop.getOp(expr.getOp());
		generateExpr(expr.getL(), currentStack);
		generateExpr(expr.getR(), currentStack);
		switch (op) {
			case mul:
			case div:
			case mod:
			case plus:
			case minus:
			case and:
			case or:
				writer.writeOp(op);
				break;
			case lt:
			case gt:
			case leq:
			case geq:
			case equals:
			case not_equals:
				String labelTrueBranch = op.getCode() + labelNr;
				String labelDone = "done" + labelNr++;
				writer.writeOp(op, labelTrueBranch);
				writer.writeBoolean(false);
				writer.writeGoto(labelDone);
				writer.writeLabel(labelTrueBranch);
				writer.writeBoolean(true);
				writer.writeLabel(labelDone);
				break;
		}
		updateMaxStack(currentStack + 2);
	}

	private void generateBooleanLiteral(ABooleanLiteralExpr expr, int currentStack) {
		boolean value = Boolean.parseBoolean(expr.getBoolean().getText());
		writer.writeIPush(value ? 1 : 0);
		updateMaxStack(currentStack + 1);
	}


	private void generateIntegerLiteral(AIntegerLiteralExpr expr, int currentStack) {
		int value = Integer.parseInt(expr.getInteger().getText());
		writer.writeIPush(value);
		updateMaxStack(currentStack + 1);
	}


	@Override
	public String toString() {
		return writer.toString();
	}
}
