package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Primary extends CParseRule {
	// primary ::= primaryMult | variable
	private CParseRule primary;
	public Primary(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return PrimaryMult.isFirst(tk) || Variable.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (PrimaryMult.isFirst(tk)) {
			primary = new PrimaryMult(pcx);
			primary.parse(pcx);
		} else {
			primary = new Variable(pcx);
			primary.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null) {
			primary.semanticCheck(pcx);
			this.setCType(primary.getCType());
			this.setConstant(primary.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; primary starts");
		if (primary != null) primary.codeGen(pcx);
		o.println(";;; primary completes");
	}
}

class PrimaryMult extends CParseRule {
	// primaryMult ::= MULT variable
	//private CToken op;
	private CParseRule variable;
	public PrimaryMult(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MULT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		//op = ct.getCurrentToken(pcx);
		CToken tk = ct.getNextToken(pcx);
		if (Variable.isFirst(tk)) {
			variable = new Variable(pcx);
			variable.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "*の次はvariableです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (variable != null) {
			variable.semanticCheck(pcx);
			this.setCType(variable.getCType());
			this.setConstant(variable.isConstant());
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; primaryMult starts");
		if (variable != null) {
			variable.codeGen(pcx);
		}
		o.println(";;; primaryMult conpletes");
	}
}
