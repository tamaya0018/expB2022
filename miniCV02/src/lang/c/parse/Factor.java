package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Factor extends CParseRule {
	// factor ::= number | factorAmp
	private CParseRule number;
	public Factor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAmp.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (Number.isFirst(tk)) {
			number = new Number(pcx);
			number.parse(pcx);
		} else {
			number = new FactorAmp(pcx);
			number.parse(pcx);
		}	
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(number.getCType());		// number の型をそのままコピー
			setConstant(number.isConstant());	// number は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factor starts");
		if (number != null) { number.codeGen(pcx); }
		o.println(";;; factor completes");
	}
}

class FactorAmp extends CParseRule {
	// facorAmp ::= '&' number
	//private CToken amp;
	private CParseRule number;
	public FactorAmp(CParseContext pcx) {
	}	
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_AMP;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// CurrentToken is now '&'
		CTokenizer ct = pcx.getTokenizer();
		//amp = ct.getCurrentToken(pcx);
		CToken tk = ct.getNextToken(pcx);
		if (Number.isFirst(tk)) {
			number = new Number(pcx);
			number.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "&の後ろはnumberです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		this.setCType(CType.getCType(CType.T_pint));
		this.setConstant(true);
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factorAmp starts");
		if (number != null) number.codeGen(pcx);
		o.println(";;; factorAmp completes");
	}
}
























