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

	public void writeILoad(int varNr) {
		sb.append("\t");
		sb.append("iload_");
		sb.append(varNr);
		sb.append("\n");
	}

	public void writeOut() {
		sb.append("\tgetstatic java/lang/System/out Ljava/io/PrintStream;\n");
	}

	public void writePrintln() {
		sb.append("\tinvokevirtual java/io/PrintStream/println(I)V\n");
	}

	public void prependBoilerplateInit(String moduleName, int stackSize, int nrOfLocals) {
		String boilerPlate = ".class  public synchronized " +
				moduleName +
				"\n" +
				".super  java/lang/Object\n\n" +
				".method public <init>()V\n" +
				"\t.limit stack 1\n" +
				"\t.limit locals 1\n" +
				"\taload_0\n" +
				"\tinvokenonvirtual java/lang/Object/<init>()V\n" +
				"\treturn\n" +
				".end method\n\n" +
				".method public static main([Ljava/lang/String;)V\n" +
				"\t.limit stack " +
				stackSize +
				"\n" +
				"\t.limit locals " +
				nrOfLocals +
				"\n";
		// prepend
		sb.insert(0, boilerPlate);
	}

	public void writeBoilerplateExit() {
		sb.append("\treturn\n");
		sb.append(".end method\n");
	}
}
