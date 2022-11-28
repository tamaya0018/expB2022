package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Factor extends CParseRule {
	// factor ::= plusFactor | minusFactor | unsignedFactor
	private CParseRule factor;
	public Factor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return PlusFactor.isFirst(tk) || MinusFactor.isFirst(tk) || UnsignedFactor.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (PlusFactor.isFirst(tk)) {
			factor = new PlusFactor(pcx);
			factor.parse(pcx);
		} else if (MinusFactor.isFirst(tk)){
			factor = new MinusFactor(pcx);
			factor.parse(pcx);
		} else {
			factor = new UnsignedFactor(pcx);
			factor.parse(pcx);
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			this.setCType(factor.getCType());	
			this.setConstant(factor.isConstant());
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factor starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; factor completes");
	}
}

class PlusFactor extends CParseRule {
	// plusFactor ::= PLUS unsignedFactor
	//private CToken op;
	private CParseRule unsignedFactor;
	public PlusFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_PLUS;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		//op = ct.getCurrentToken(pcx);
		// read next to "+"
		CToken tk = ct.getNextToken(pcx);
		if (UnsignedFactor.isFirst(tk)) {
			unsignedFactor = new UnsignedFactor(pcx);
			unsignedFactor.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "+の後ろはunsignedFactorです");
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
		o.println(";;; plusFactor starts");
		if (unsignedFactor != null) { unsignedFactor.codeGen(pcx); }
		o.println(";;; plusFactor completes");
	}
}

class MinusFactor extends CParseRule {
	// MinusFactor ::= MINUS unsignedFactor
	private CToken op;
	private CParseRule unsignedFactor;
	public MinusFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MINUS;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		// read next to "-"
		CToken tk = ct.getNextToken(pcx);
		if (UnsignedFactor.isFirst(tk)) {
			unsignedFactor = new UnsignedFactor(pcx);
			unsignedFactor.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "-の後ろはunsignedFactorです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (unsignedFactor != null) {
			unsignedFactor.semanticCheck(pcx);
			if (unsignedFactor.getCType().isCType(CType.T_pint)) {
				pcx.fatalError(op.toExplainString() + "ポインタの値が負の値です");
				this.setCType(CType.getCType(CType.T_err));
			} else {
				this.setCType(unsignedFactor.getCType());	
			}
			this.setConstant(unsignedFactor.isConstant());	
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; minusFactor starts");
		if (unsignedFactor != null) { 
			unsignedFactor.codeGen(pcx); 
			o.println("\tMOV\t-(R6), R0\t; MinusFactor: スタックトップから値を取り出し、その値をビット反転し１を足す<" + op.toExplainString() + ">");
			o.println("\tXOR\t#0xffff, R0\t; MinusFactor:");
			o.println("\tADD\t#1, R0\t\t; MinusFactor:");
			o.println("\tMOV\tR0, (R6)+\t; MinusFactor:");
			o.println(";;; minusFactor completes");
		}
	}
}
