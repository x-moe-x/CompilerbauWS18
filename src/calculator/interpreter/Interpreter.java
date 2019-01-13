package calculator.interpreter;

import calculator.analysis.DepthFirstAdapter;
import calculator.node.AProgram;

public class Interpreter extends DepthFirstAdapter {
	@Override
	public void caseAProgram(AProgram node) {
		super.caseAProgram(node);
	}
}
