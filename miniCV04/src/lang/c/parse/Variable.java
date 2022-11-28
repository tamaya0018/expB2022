package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Variable extends CParseRule {
	// variable ::= ident [ array ]
	private CToken var;
	private CParseRule ident = null, array = null;
	public Variable(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Ident.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		var = tk;
		ident = new Ident(pcx);
		ident.parse(pcx);
		tk = ct.getCurrentToken(pcx);
		if (Array.isFirst(tk)) {
			array = new Array(pcx);
			array.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (ident != null) {
			ident.semanticCheck(pcx);
			if (array != null) {
				array.semanticCheck(pcx);
				if (ident.getCType().getType() != CType.T_int_array && ident.getCType().getType() != CType.T_pint_array) {
					pcx.fatalError(var.toExplainString() + "arrayが存在するとき、identはint[]かint*[]のいずれかである必要があります");
				} else if (ident.getCType().getType() == CType.T_int_array) {
					this.setCType(CType.getCType(CType.T_int));
				} else {
					this.setCType(CType.getCType(CType.T_pint));
				}
			} else {
				if (ident.getCType().getType() == CType.T_int_array || ident.getCType().getType() == CType.T_pint_array) {
					pcx.fatalError(var.toExplainString() + "identがint[]かint*[]のいずれかだった場合、arrayが必要です");
				}
				this.setCType(ident.getCType());
			}
			this.setConstant(ident.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; variable starts");
		if (ident != null) {
			ident.codeGen(pcx);
			if (array != null) {
				array.codeGen(pcx);
			}
		}
		o.println(";;; variable completes");
	}
}

class Array extends CParseRule {
	// array ::= LBRA expression RBRA
	private CToken exp;
	private CParseRule expression;
	public Array(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LBRA;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		exp = tk;
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
		// expression が T_int であることを確認する。
		if (expression != null) {
			expression.semanticCheck(pcx);
			if (expression.getCType().getType() != CType.T_int) {
				pcx.fatalError(exp.toExplainString() + "arrayの型がint型ではありません");
			}
			this.setCType(expression.getCType());
			this.setConstant(expression.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; array starts");
		if (expression != null) { 
			expression.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; Array: 配列の何要素目かを取り出す<" + exp.toExplainString() + ">");
			o.println("\tMOV\t-(R6), R1\t; Array: 変数アドレスを取り出す");
			o.println("\tADD\tR1, R0\t\t; Array: アドレスと配列の要素数を加算する");
			o.println("\tMOV\tR0, (R6)+\t; Array: 求めた変数アドレスを積む");
		}
		o.println(";;; array completes");
	}
}