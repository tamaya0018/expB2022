package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class UnsignedFactor extends CParseRule {
	// unsignedFactor ::= number | factorAmp | LPAR expression RPAR | addressToValue
	//private CToken lpar = null, rpar = null;
	private CParseRule unsignedFactor;
	public UnsignedFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAmp.isFirst(tk) || tk.getType() == CToken.TK_LPAR || AddressToValue.isFirst(tk);
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
		} else if (tk.getType() == CToken.TK_LPAR) {
			//lpar = tk;
			tk = ct.getNextToken(pcx);
			if (Expression.isFirst(tk)) {
				unsignedFactor = new Expression(pcx);
				unsignedFactor.parse(pcx);
				//rpar = ct.getCurrentToken(pcx);
				tk = ct.getCurrentToken(pcx);
				if (tk.getType() != CToken.TK_RPAR) {
					pcx.fatalError(tk.toExplainString() + "括弧が閉じていません");
				}
				tk = ct.getNextToken(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() + "'('の次はexpressionです");
			}
		} else {
			unsignedFactor = new AddressToValue(pcx);
			unsignedFactor.parse(pcx);
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
	// facorAmp ::= '&' (number | primary)
	private CToken factor;
	private CParseRule factorAmp;
	public FactorAmp(CParseContext pcx) {
	}	
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_AMP;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// CurrentToken is now '&'
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		factor = tk;
		if (Number.isFirst(tk)) {
			factorAmp = new Number(pcx);
			factorAmp.parse(pcx);
		} else if (Primary.isFirst(tk)) {
			if (PrimaryMult.isFirst(tk)) {
				pcx.fatalError(factor.toExplainString() + "&primaryにおいてprimaryはprimaryMultを子節点にもってはいけません");
			} else {
				factorAmp = new Primary(pcx);
				factorAmp.parse(pcx);
			}
		} else {
			pcx.fatalError(tk.toExplainString() + "&の後ろは(number|primary)です");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factorAmp != null) {
			factorAmp.semanticCheck(pcx);
			
			if (factorAmp.getCType().getType() == CType.T_int) {
				this.setCType(CType.getCType(CType.T_pint));
				this.setConstant(true);
			} else if (factorAmp.getCType().getType() == CType.T_int_array) {
				this.setCType(CType.getCType(CType.T_pint_array));
				this.setConstant(true);
			} else {
				pcx.fatalError(factor.toExplainString() + "&(number|primary)において(number|primary)はintかint[]である必要があります");
			}
		}
		
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factorAmp starts");
		if (factorAmp != null) factorAmp.codeGen(pcx);
		o.println(";;; factorAmp completes");
	}
}

class AddressToValue extends CParseRule {
	// addressToValue ::= primary
	private CParseRule addressToValue;
	public AddressToValue(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Primary.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		addressToValue = new Primary(pcx);
		addressToValue.parse(pcx);
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (addressToValue != null) {
			addressToValue.semanticCheck(pcx);
			this.setCType(addressToValue.getCType());		
			this.setConstant(addressToValue.isConstant());	
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; addressToValue starts");
		if (addressToValue != null) { 
			addressToValue.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; AddressToValue: 変数のアドレスを取り出す");
			o.println("\tMOV\t(R0), (R6)+\t; AddressToValue: 内容を参照して積む");
		}
		o.println(";;; addressToValue completes");
	}
}
