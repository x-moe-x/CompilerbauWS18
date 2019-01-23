package kotlin.compiler;

public class CodeWriter {
	private final StringBuilder sb = new StringBuilder();

	void writeLabel(String label) {
		sb.append(label);
		sb.append(":");
		sb.append("\n");
	}

	void writeIPush(int n) {
		sb.append("\t");
		switch (n) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				sb.append("iconst_");
				break;
			default:
				if (n >= -128 && n <= 127) {
					sb.append("bipush ");
				} else {
					sb.append("sipush ");
				}
		}

		sb.append(n);
		sb.append("\n");
	}

	void writeIStore(int varNr) {
		sb.append("\t");
		sb.append("istore_");
		sb.append(varNr);
		sb.append("\n");
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	void writeIfeq(String label) {
		sb.append("\tifeq ");
		sb.append(label);
		sb.append("\n");
	}

	void writeGoto(String label) {
		sb.append("\tgoto ");
		sb.append(label);
		sb.append("\n");
	}

	public void writeOp(CodeGenerator.Binop op) {
		sb.append("\t");
		sb.append(op.getCode());
		sb.append("\n");
	}

	public void writeOp(CodeGenerator.Binop op, String label) {
		sb.append("\t");
		sb.append(op.getCode());
		sb.append(" ");
		sb.append(label);
		sb.append("\n");
	}

	public void writeBoolean(boolean b) {
		writeIPush(b ? 1 : 0);
	}
}
