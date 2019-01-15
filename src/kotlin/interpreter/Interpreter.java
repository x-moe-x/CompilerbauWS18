package kotlin.interpreter;

import kotlin.analysis.DepthFirstAdapter;
import kotlin.node.AStart;

public class Interpreter extends DepthFirstAdapter {
	@Override
	public void caseAStart(AStart node) {
		super.caseAStart(node);
	}
}
