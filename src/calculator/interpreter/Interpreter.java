package calculator.interpreter;

import calculator.analysis.DepthFirstAdapter;
import calculator.node.AProgram;

public class Interpreter extends DepthFirstAdapter {
	@Override
	public void caseAProgram(AProgram node) {
		String l = node.getLeft().getText().trim();
		String r = node.getRight().getText().trim();
		String sign = node.getOperation().getText().trim();

		double left = new Double(l);
		double right = new Double(r);
		double result = 0;

		switch (sign) {
			case ("+"):
				result = left + right;
				break;
			case ("-"):
				result = left - right;
				break;
			case ("*"):
				result = left * right;
				break;
			case ("/"):
				result = left / right;
				break;
		}
		System.out.println(left + " " + sign + " " + right + " = " + result);
	}
}
