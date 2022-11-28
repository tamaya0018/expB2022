package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class UnsignedFactor extends CParseRule {
	// unsignedFactor ::= number | factorAmp | LPAR expression RPAR
	//private CToken lpar = null, rpar = null;
	private CParseRule unsignedFactor;
	public UnsignedFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAmp.isFirst(tk) || tk.getType() == CToken.TK_LPAR;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (Number.isFirst(tk)) {
			unsignedFactor = new Number(pcx);
			unsignedFactor.parse(pcx);
		} else if (FactorAmp.isFirst(tk)) {
			unsignedFactor = new FactorAmp(pcx);
			unsignedFactor.parse(pcx);
		} else {
			//lpar = tk;
			tk = ct.getNextToken(pcx);
			unsignedFactor = new Expression(pcx);
			unsignedFactor.parse(pcx);
			//rpar = ct.getCurrentToken(pcx);
			tk = ct.getCurrentToken(pcx);
			if (tk.getType() != CToken.TK_RPAR) {
				pcx.fatalError(tk.toExplainString() + "括弧が閉じていません");
			}
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (unsignedFactor != null) {
			unsignedFactor.semanticCheck(pcx);
			this.setCType(unsignedFactor.getCType());		
			this.setConstant(unsignedFactor.isConstant());	
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; unsignedFactor starts");
		if (unsignedFactor != null) { unsignedFactor.codeGen(pcx); }
		o.println(";;; unsignedFactor completes");
	}
}

class FactorAmp extends CParseRule {
	// facorAmp ::= '&' number
	//private CToken amp = null;
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
