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
		if (statement != null) { statement.semanticCheck(pcx); }
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statement starts");
		if (statement != null) { statement.codeGen(pcx); }
		o.println(";;; statement completes");
	}
}

class StatementAssign extends CParseRule {
	// statementAssign ::= primary ASSIGN expression SEMI
	private CParseRule primary, expression;
	private CToken assign, semi;
	
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
			pcx.fatalError(tk.toExplainString() + "statementAssignにおいてprimaryの次はASSIGNです");
		} else {
			assign = tk;
			// ASSIGNのあとのexpressionの処理
			tk = ct.getNextToken(pcx);
			if (Expression.isFirst(tk)) {
				expression = new Expression(pcx);
				expression.parse(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() + "statementAssignにおいてASSIGNの次はexpressionです");
			}
			// expressionのあとのSEMIの処理
			tk = ct.getCurrentToken(pcx);
			if (tk.getType() != CToken.TK_SEMI) {
				pcx.fatalError(tk.toExplainString() + "statementAssignにおいてexpressionの次はSEMIです");
			}
			semi = tk;
			tk = ct.getNextToken(pcx);
		}
		
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null && expression != null) {
			primary.semanticCheck(pcx);
			expression.semanticCheck(pcx);
			if (primary.getCType().getType() != expression.getCType().getType()) {
				pcx.fatalError(assign.toExplainString() + semi.toExplainString() + "左辺の型[" + primary.getCType().toString() + "]と右辺の型[" + expression.getCType().toString() + "]が異なります");
			}
			
			if (primary.isConstant()) {
				pcx.fatalError(assign.toExplainString() + semi.toExplainString() + "代入文の左辺が定数になっています");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementAssign starts");
		if (primary != null && expression != null) {
			primary.codeGen(pcx);
			expression.codeGen(pcx);
			o.println("\tMOV\t-(R6)" + ", R0\t; StatementAssign: 代入される値を取り出す");
			o.println("\tMOV\t-(R6)" + ", R1\t; StatementAssign: 代入する番地を取り出す");
			o.println("\tMOV\tR0" + ", (R1)\t; StatementAssign: 代入する番地に値を入れる");
		}
		o.println(";;; statementAssign completes");
	}
}
