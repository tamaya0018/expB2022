package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Term extends CParseRule {
	// term ::= factor { termMult | termDiv }
	private CParseRule term;
	
	public Term(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Factor.isFirst(tk) || TermMult.isFirst(tk) || TermDiv.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CParseRule factor = null, list = null;
		factor = new Factor(pcx);
		factor.parse(pcx);
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		while (TermMult.isFirst(tk) || TermDiv.isFirst(tk)) {
			if (TermMult.isFirst(tk)) {
				list = new TermMult(pcx, factor);
				list.parse(pcx);
				factor = list;
				tk = ct.getCurrentToken(pcx);
			} else if (TermDiv.isFirst(tk)) {
				list = new TermDiv(pcx, factor);
				list.parse(pcx);
				factor = list;
				tk = ct.getCurrentToken(pcx);
			}
		}
		term = factor;
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (term != null) {
			term.semanticCheck(pcx);
			this.setCType(term.getCType());	
			this.setConstant(term.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; term starts");
		if (term != null) { term.codeGen(pcx); }
		o.println(";;; term completes");
	}
}

class TermMult extends CParseRule {
	// termMult ::= MULT factor
	private CToken op;
	private CParseRule left, right;
	public TermMult(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MULT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		// read next to '*'
		CToken tk = ct.getNextToken(pcx);
		if (Factor.isFirst(tk)) {
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "*の後ろはfactorです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		final int s[][] = {
		//		T_err			T_int			T_pint
			{	CType.T_err,	CType.T_err,	CType.T_err  },	// T_err
			{	CType.T_err,	CType.T_int,	CType.T_err },	// T_int
			{	CType.T_err,	CType.T_err,	CType.T_err  }, // T_pint
		};
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			int lt = left.getCType().getType();		// *の左辺の型
			int rt = right.getCType().getType();	// *の右辺の型
			int nt = s[lt][rt];						// 規則による型計算
			if (nt == CType.T_err) {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]はかけられません");
			}
			this.setCType(CType.getCType(nt));
			this.setConstant(left.isConstant() && right.isConstant());	// +の左右両方が定数のときだけ定数
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (left != null && right != null) {
			o.println(";;; TermMult starts");
			left.codeGen(pcx);		// 左部分木のコード生成を頼む
			right.codeGen(pcx);		// 右部分木のコード生成を頼む
			o.println("\tJSR\tMUL\t\t; TermMult: サブルーチンに飛ぶ<" + op.toExplainString() + ">");
			o.println("\tSUB\t#2, R6\t\t; TermMult: サブルーチン用のスタックをおろす");
			o.println("\tMOV\tR2, (R6)+\t; TermMult: 返却値をスタックに積む");
			o.println(";;; TermMult completes");
		}
	}
}

class TermDiv extends CParseRule {
	// termDiv ::= DIV factor
	private CToken op;
	private CParseRule left, right;
	public TermDiv(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_DIV;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		op = ct.getCurrentToken(pcx);
		// read next to '/'
		CToken tk = ct.getNextToken(pcx);
		if (Factor.isFirst(tk)) {
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "/の後ろはfactorです");
		}
	}
	
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		final int s[][] = {
		//		T_err			T_int			T_pint
			{	CType.T_err,	CType.T_err,	CType.T_err  },	// T_err
			{	CType.T_err,	CType.T_int,	CType.T_err },	// T_int
			{	CType.T_err,	CType.T_err,	CType.T_err  }, // T_pint
		};
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			int lt = left.getCType().getType();		// *の左辺の型
			int rt = right.getCType().getType();	// *の右辺の型
			int nt = s[lt][rt];						// 規則による型計算
			if (nt == CType.T_err) {
				pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は割れません");
			}
			this.setCType(CType.getCType(nt));
			this.setConstant(left.isConstant() && right.isConstant());	// +の左右両方が定数のときだけ定数
		}
	}
	
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (left != null && right != null) {
			o.println(";;; TermDiv starts");
			left.codeGen(pcx);		// 左部分木のコード生成を頼む
			right.codeGen(pcx);		// 右部分木のコード生成を頼む
			o.println("\tJSR\tDIV\t\t; TermDiv: サブルーチンに飛ぶ<" + op.toExplainString() + ">");
			o.println("\tSUB\t#2, R6\t\t; TermDiv: サブルーチン用のスタックをおろす");
			o.println("\tMOV\tR2, (R6)+\t; TermDiv: 返却値をスタックに積む");
			o.println(";;; TermDiv completes");
		}
	}
}








