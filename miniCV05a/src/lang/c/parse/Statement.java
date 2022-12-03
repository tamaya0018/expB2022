package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Statement extends CParseRule {
	// statement ::= statementAssign
	private CParseRule statement;
	
	public Statement(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return StatementAssign.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		statement = new StatementAssign(pcx);
		statement.parse(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		/*
		this.setCType(CType.getCType(CType.T_int));
		this.setConstant(true);
		*/
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		/*
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; number starts");
		if (num != null) {
			o.println("\tMOV\t#" + num.getText() + ", (R6)+\t; Number: 数を積む<" + num.toExplainString() + ">");
		}
		o.println(";;; number completes");
		*/
	}
}

class StatementAssign extends CParseRule {
	// statementAssign ::= primary ASSIGN expression SEMI
	private CParseRule primary, expression;
	//private CToken assign, semi;
	
	public StatementAssign(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Primary.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		primary = new Primary(pcx);
		primary.parse(pcx);
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (tk.getType() != CToken.TK_ASSIGN) {
			pcx.fatalError(tk.toExplainString() + "statementAssignにおいてprimaryの次はASSIGNです。");
		}
		// ASSIGNのあとのexpressionの処理
		tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "statementAssignにおいてASSIGNの次はexpressionです。");
		}
		// expressionのあとのSEMIの処理
		tk = ct.getCurrentToken(pcx);
		if (tk.getType() != CToken.TK_SEMI) {
			pcx.fatalError(tk.toExplainString() + "statementAssignにおいてexpressionの次はSEMIです。");
		}
		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		//this.setCType(CType.getCType(CType.T_int));
		//this.setConstant(true);
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		/*
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; number starts");
		if (num != null) {
			o.println("\tMOV\t#" + num.getText() + ", (R6)+\t; Number: 数を積む<" + num.toExplainString() + ">");
		}
		o.println(";;; number completes");
		*/
	}
}
