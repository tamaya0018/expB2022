package lang.c.parse;

import java.io.PrintStream;
import lang.*;
import lang.c.*;

public class Condition extends CParseRule {
	// condition ::= TRUE | FALSE | expression ( brabrabra )
	private CParseRule expression = null, conditions = null;
	private CToken truth = null;
	
	public Condition(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return (tk.getType() == CToken.TK_TRUE) || (tk.getType() == CToken.TK_FALSE) || Expression.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		if (Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);
			if (ConditionLT.isFirst(tk)) {
				conditions = new ConditionLT(pcx, expression);
				conditions.parse(pcx);
			} else if (ConditionLE.isFirst(tk)) {
				conditions = new ConditionLE(pcx, expression);
				conditions.parse(pcx);
			} else if (ConditionGT.isFirst(tk)) {
				conditions = new ConditionGT(pcx, expression);
				conditions.parse(pcx);
			} else if (ConditionGE.isFirst(tk)) {
				conditions = new ConditionGE(pcx, expression);
				conditions.parse(pcx);
			} else if (ConditionEQ.isFirst(tk)) {
				conditions = new ConditionEQ(pcx, expression);
				conditions.parse(pcx);
			} else if (ConditionNE.isFirst(tk)) {
				conditions = new ConditionNE(pcx, expression);
				conditions.parse(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() + "条件式の文法に誤りがあります");
			}
		} else {
			truth = tk;
			tk = ct.getNextToken(pcx);
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (conditions != null) {
			conditions.semanticCheck(pcx);
			this.setCType(conditions.getCType());
		} else {
			this.setCType(CType.getCType(CType.T_bool));
			this.setConstant(true);
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition starts");
		if (conditions != null) {
			conditions.codeGen(pcx);
		} else if (truth != null) {
			if (truth.getType() == CToken.TK_TRUE) {
				o.println("\tMOV\t0x0001, (R6)+\t; Conditions: 真を積む<" + truth.toExplainString() + ">");
			} else if (truth.getType() == CToken.TK_FALSE) {
				o.println("\tMOV\t0x0000, (R6)+\t; Conditions: 偽を積む<" + truth.toExplainString() + ">");
			}
		}
		o.println(";;; condition completes");
	}
}

class ConditionLT extends CParseRule {
	
	private CParseRule left, right;
	private CToken op;
	private int seq;
	
	public ConditionLT(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		
		CToken tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			right = new Expression(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "<の後ろはexpressionです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (left.getCType().getType() == right.getCType().getType()) {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			} else {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は比較できません");
			}
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition < (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionLT: ２数を取り出して、比べる");
			o.println("\tMOV\t-(R6), R1\t; ConditionLT: R0 = right, R1 = left");
			o.println("\tMOV\t#0x0001, R2\t; ConditionLT: set true");
			o.println("\tCMP\tR0, R1\t\t; ConditionLT: R1<R0 = R1-R0<0");
			o.println("\tBRN\tLT" + seq + "\t\t; ConditionLT:");
			o.println("\tCLR\tR2\t\t; ConditionLT: set false");
			o.println("LT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionLT:");
		}
		o.println(";;; condition < (compare) completes");
	}
}

class ConditionLE extends CParseRule {
	
	private CParseRule left, right;
	private CToken op;
	private int seq;
	
	public ConditionLE(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LE;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		
		CToken tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			right = new Expression(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "<=の後ろはexpressionです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (left.getCType().getType() == right.getCType().getType()) {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			} else {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は比較できません");
			}
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition <= (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionLE: ２数を取り出して、比べる");
			o.println("\tMOV\t-(R6), R1\t; ConditionLE: R0 = right, R1 = left");
			o.println("\tMOV\t#0x0001, R2\t; ConditionLE: set true");
			o.println("\tCMP\tR0, R1\t\t; ConditionLE: R1<=R0 = (R1-R0<0 | R1-R0=0)");
			o.println("\tBRN\tLE" + seq + "\t\t; ConditionLE:");
			o.println("\tBRZ\tLE" + seq + "\t\t; ConditionLE:");
			o.println("\tCLR\tR2\t\t; ConditionLE: set false");
			o.println("LE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionLE:");
		}
		o.println(";;; condition <= (compare) completes");
	}
}

class ConditionGT extends CParseRule {
	
	private CParseRule left, right;
	private CToken op;
	private int seq;
	
	public ConditionGT(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_GT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		
		CToken tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			right = new Expression(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + ">の後ろはexpressionです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (left.getCType().getType() == right.getCType().getType()) {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			} else {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は比較できません");
			}
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition > (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionGT: ２数を取り出して、比べる");
			o.println("\tMOV\t-(R6), R1\t; ConditionGT: R0 = right, R1 = left");
			o.println("\tMOV\t#0x0001, R2\t; ConditionGT: set true");
			o.println("\tCMP\tR1, R0\t\t; ConditionGT: R1>R0 = R0-R1<0");
			o.println("\tBRN\tGT" + seq + "\t\t; ConditionGT:");
			o.println("\tCLR\tR2\t\t; ConditionGT: set false");
			o.println("GT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionGT:");
		}
		o.println(";;; condition > (compare) completes");
	}
}

class ConditionGE extends CParseRule {
	
	private CParseRule left, right;
	private CToken op;
	private int seq;
	
	public ConditionGE(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_GE;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		
		CToken tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			right = new Expression(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + ">=の後ろはexpressionです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (left.getCType().getType() == right.getCType().getType()) {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			} else {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は比較できません");
			}
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition >= (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionGE: ２数を取り出して、比べる");
			o.println("\tMOV\t-(R6), R1\t; ConditionGE: R0 = right, R1 = left");
			o.println("\tMOV\t#0x0001, R2\t; ConditionGE: set true");
			o.println("\tCMP\tR1, R0\t\t; ConditionGE: R1>=R0 = (R0-R1<0 | R0-R1=0)");
			o.println("\tBRN\tGE" + seq + "\t\t; ConditionGE:");
			o.println("\tBRZ\tGE" + seq + "\t\t; ConditionGE:");
			o.println("\tCLR\tR2\t\t; ConditionGE: set false");
			o.println("GE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionGE:");
		}
		o.println(";;; condition >= (compare) completes");
	}
}

class ConditionEQ extends CParseRule {
	
	private CParseRule left, right;
	private CToken op;
	private int seq;
	
	public ConditionEQ(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_EQ;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		
		CToken tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			right = new Expression(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "==の後ろはexpressionです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (left.getCType().getType() == right.getCType().getType()) {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			} else {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は比較できません");
			}
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition == (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionEQ: ２数を取り出して、比べる");
			o.println("\tMOV\t-(R6), R1\t; ConditionEQ: R0 = right, R1 = left");
			o.println("\tMOV\t#0x0001, R2\t; ConditionEQ: set true");
			o.println("\tCMP\tR0, R1\t\t; ConditionEQ: R1=R0 = R1-R0=0");
			o.println("\tBRZ\tEQ" + seq + "\t\t; ConditionEQ:");
			o.println("\tCLR\tR2\t\t; ConditionEQ: set false");
			o.println("EQ" + seq + ":\tMOV\tR2, (R6)+\t; ConditionEQ:");
		}
		o.println(";;; condition == (compare) completes");
	}
}

class ConditionNE extends CParseRule {
	
	private CParseRule left, right;
	private CToken op;
	private int seq;
	
	public ConditionNE(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_NE;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		
		CToken tk = ct.getNextToken(pcx);
		if (Expression.isFirst(tk)) {
			right = new Expression(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "!=の後ろはexpressionです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (left.getCType().getType() == right.getCType().getType()) {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			} else {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は比較できません");
			}
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition != (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionNE: ２数を取り出して、比べる");
			o.println("\tMOV\t-(R6), R1\t; ConditionNE: R0 = right, R1 = left");
			o.println("\tCLR\tR2\t\t; ConditionNE: set false");
			o.println("\tCMP\tR0, R1\t\t; ConditionNE: R0!=R1 は R0=R1 のときに false になっていればよい");
			o.println("\tBRZ\tNE" + seq + "\t\t; ConditionNE:");
			o.println("\tMOV\t#0x0001, R2\t; ConditionNE: set true");
			o.println("NE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionNE:");
		}
		o.println(";;; condition != (compare) completes");
	}
}