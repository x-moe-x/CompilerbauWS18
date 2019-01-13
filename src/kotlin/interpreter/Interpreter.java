package kotlin.interpreter;

import kotlin.analysis.DepthFirstAdapter;
import kotlin.node.AFunction;

public class Interpreter extends DepthFirstAdapter {
	@Override
	public void caseAFunction(AFunction node) {
		super.caseAFunction(node);
	}
}
