package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Ident extends CParseRule {
	// ident ::= IDENT
	private CToken ident;
	public Ident(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_IDENT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		ident = tk;
		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		String text = ident.getText();
		if (text.matches("c_.*")) {
			this.setCType(CType.getCType(CType.T_int));
			this.setConstant(true);
		} else if (text.matches("ipa_.*")) {
			this.setCType(CType.getCType(CType.T_pint_array));
			this.setConstant(false);
		} else if (text.matches("ia_.*")) {
			this.setCType(CType.getCType(CType.T_int_array));
			this.setConstant(false);
		} else if (text.matches("ip_.*")) {
			this.setCType(CType.getCType(CType.T_pint));
			this.setConstant(false);
		} else {
			this.setCType(CType.getCType(CType.T_int));
			this.setConstant(false);
		}
		
		//this.setCType(CType.getCType(CType.T_int));
		//this.setConstant(false);
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; ident starts");
		if (ident != null) {
			o.println("\tMOV\t#" + ident.getText() + ", (R6)+\t; Ident: 変数アドレスを積む<" + ident.toExplainString() + ">");
		}
		o.println(";;; ident completes");
	}
}
