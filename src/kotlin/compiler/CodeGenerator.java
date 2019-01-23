package kotlin.compiler;

import kotlin.analysis.DepthFirstAdapter;
import kotlin.node.*;

import java.util.LinkedList;

public class CodeGenerator extends DepthFirstAdapter {
	private final TypeChecker checker;
	private int labelNr = 0;
	private final CodeWriter writer = new CodeWriter();

	enum Binop {
		mul("imul", false), div("idiv", false), mod("irem", false), plus("iadd", false), minus("isub", false),
		lt("iflt", true), gt("ifgt", true), leq("ifgt", true), geq("ifgt", true), equals("ifgt", true), not_equals("ifgt", true), and("ifgt", true), or("ifgt", true);

		private final String code;
		private final boolean isComparison;

		Binop(String code, boolean isComparison) {
			this.code = code;
			this.isComparison = isComparison;
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

		public boolean isComparison() {
			return isComparison;
		}
	}

	public CodeGenerator(TypeChecker checker) {
		this.checker = checker;
	}

	@Override
	public void outAFunction(AFunction node) {
		generateStatements(node.getDeclarations());
		generateStatements(node.getStatements());
	}

	private void generateStatements(LinkedList<PStmt> stmts) {
		for (PStmt stmt : stmts) {
			generateStatement(stmt);
		}
	}

	private void generateStatement(PStmt stmt) {
		if (stmt instanceof ADeclarationStmt) {
			generateDeclaration((ADeclarationStmt) stmt);
		} else if (stmt instanceof AWhileStmt) {
			generateWhile((AWhileStmt) stmt);
		} else {
			System.err.println("not implemented for " + stmt.getClass().getSimpleName());
		}
	}

	private void generateWhile(AWhileStmt stmt) {
		String L1 = "StartWhile" + labelNr;
		String L2 = "EndWhile" + labelNr++;
		writer.writeLabel(L1);
		generateExpr(stmt.getCondition());
		writer.writeIfeq(L2);
		generateStatements(stmt.getStatements());
		writer.writeGoto(L1);
		writer.writeLabel(L2);
	}


	private void generateDeclaration(ADeclarationStmt stmt) {
		generateExpr(stmt.getExpr());
		writer.writeIStore(checker.getVarNr(stmt.getVar()));
	}

	private void generateExpr(PExpr expr) {
		if (expr instanceof AIntegerLiteralExpr) {
			generateIntegerLiteral((AIntegerLiteralExpr) expr);
		} else if (expr instanceof ABooleanLiteralExpr) {
			generateBooleanLiteral((ABooleanLiteralExpr) expr);
		} else if (expr instanceof ABinaryExpr) {
			generateBinaryExpression((ABinaryExpr) expr);
		} else {
			System.err.println("not implemented for " + expr.getClass().getSimpleName());
		}
	}

	private void generateBinaryExpression(ABinaryExpr expr) {
		Binop op = Binop.getOp(expr.getOp());
		generateExpr(expr.getL());
		generateExpr(expr.getR());
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
	}

	private void generateBooleanLiteral(ABooleanLiteralExpr expr) {
		boolean value = Boolean.parseBoolean(expr.getBoolean().getText());
		writer.writeIPush(value ? 1 : 0);
	}


	private void generateIntegerLiteral(AIntegerLiteralExpr expr) {
		int value = Integer.parseInt(expr.getInteger().getText());
		writer.writeIPush(value);
	}


	@Override
	public String toString() {
		return writer.toString();
	}
}
