package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Variable extends CParseRule {
	// variable ::= ident [ array ]
	private CParseRule variable;
	public Variable(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Ident.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CParseRule ident = null, array = null;
		ident = new Ident(pcx);
		ident.parse(pcx);
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (Array.isFirst(tk)) {
			array = new Array(pcx);
			array.parse(pcx);
		}
		variable = ident;
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
		o.println(";;; variable starts");
		if (variable != null) {
			variable.codeGen(pcx);
		}
		o.println(";;; variable completes");
	}
}

class Array extends CParseRule {
	// array ::= LBRA expression RBRA
	private CParseRule expression;
	public Array(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LBRA;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);
			if (tk.getType() != CToken.TK_RBRA) {
				pcx.fatalError(tk.toExplainString() + "']'括弧が閉じていません");
			}
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "'['の次はarrayです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (expression != null) {
			expression.semanticCheck(pcx);
			this.setCType(expression.getCType());		
			this.setConstant(expression.isConstant());	
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; array starts");
		if (expression != null) { expression.codeGen(pcx); }
		o.println(";;; array completes");
	}
}