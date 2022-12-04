package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.*;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	@SuppressWarnings("unused")
	private CTokenRule	rule;
	private int			lineNo, colNo;
	private char			backCh;
	private boolean		backChExist = false;

	public CTokenizer(CTokenRule rule) {
		this.rule = rule;
		lineNo = 1; colNo = 1;
	}

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n')  { colNo = 1; ++lineNo; }
//		System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}
	
	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') { --lineNo; }
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;
	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}
	// 次のトークンを読んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
//		System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}
	private CToken readToken() {
		CToken tk = null;
		char ch;
		int  startCol = colNo;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		while (!accept) {
			switch (state) {
			case 0:					// 初期状態
				ch = readChar();
				if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
				} else if (ch == (char) -1) {	// EOF
					startCol = colNo - 1;
					state = 1;
				} else if (ch >= '1' && ch <= '9') {
					startCol = colNo - 1;
					text.append(ch);
					state = 3;
				} else if (ch == '0') {
					startCol = colNo - 1;
					text.append(ch);
					state = 11;
				} else if (ch == '+') {
					startCol = colNo - 1;
					text.append(ch);
					state = 4;
				} else if (ch == '-') {
					startCol = colNo - 1;
					text.append(ch);
					state = 5;
				} else if (ch == '/') {
					startCol = colNo - 1;
					state = 6;
				} else if (ch == '&') {
					startCol = colNo - 1;
					state = 10;
				} else if (ch == '*') {
					startCol = colNo - 1;
					state = 16;
				} else if (ch == '(') {
					startCol = colNo - 1;
					state = 17;
				} else if (ch == ')') {
					startCol = colNo - 1;
					state = 18;
				} else if (ch == '[') {
					startCol = colNo - 1;
					state = 19;
				} else if (ch == ']') {
					startCol = colNo - 1;
					state = 20;
				} else if (Character.isLowerCase(ch) || Character.isUpperCase(ch) || ch == '_') {
					startCol = colNo - 1;
					text.append(ch);
					state = 21;
				} else if (ch == '=') {
					startCol = colNo - 1;
					state = 22;
				} else if (ch == ';') {
					startCol = colNo - 1;
					state = 23;
				} else if (ch == '<') {
					startCol = colNo - 1;
					state = 24;
				} else if (ch == '>') {
					startCol = colNo - 1;
					state = 25;
				} else if (ch == '!') {
					startCol = colNo - 1;
					text.append(ch);
					state = 26;
				} else {			// ヘンな文字を読んだ
					startCol = colNo - 1;
					text.append(ch);
					state = 2;
				}
				break;
			case 1:					// EOFを読んだ
				tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
				accept = true;
				break;
			case 2:					// ヘンな文字を読んだ
				System.err.println("	Error: " + text.toString() + " を読み込みました");
				tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 3:					// 数（10進数）の開始
				ch = readChar();
				if (Character.isDigit(ch)) {
					text.append(ch);
				} else {
					backChar(ch);
					tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
					state = 14;
				}
				break;
			case 4:					// +を読んだ
				tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
				accept = true;
				break;
			case 5:					// read -
				tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
				accept = true;
				break;
			case 6:					// コメント前処理
				ch = readChar();
				if (ch == '/') {
					state = 7;
				} else if (ch == '*') {
					state = 8;
				} else {
					backChar(ch);
					tk = new CToken(CToken.TK_DIV, lineNo, startCol, "/");
					accept = true;
				}
				break;
			case 7:					// //で始まるコメント処理
				ch = readChar();
				if (ch == (char) -1) {
					state = 2;
				} else if (ch == '\n' || ch == '\r') {		
					state = 0;
				}
				break;
			case 8:					// /*で始まるコメント処理
				ch = readChar();
				if (ch == (char) -1) {
					state = 2;
					System.err.println("Error: /*の途中でEOFを検出しました");
				} else if (ch == '*') {
					// コメントの終わりと思われる部分
					state = 9;
				}
				break;
			case 9:					// コメント処理の終了前処理
				ch = readChar();
				if (ch == '/') {
					// コメントの終わり
					state = 0;
				} else if (ch == (char) -1) {
					state = 2;
					System.err.println("Error: /*の途中でEOFを検出しました");
				} else {
					// コメントの終わりではなかったので*をコメントとして文字列に追加する
					backChar(ch);
					state = 8;
				}
				break;
			case 10:	//&を読んだ
				tk = new CToken(CToken.TK_AMP, lineNo, startCol, "&");
				accept = true;
				break;
			case 11:	//0を最初に読み取ったとき
				ch = readChar();
				if ('0' <= ch && ch <= '7') {	//8進数のとき
					text.append(ch);
					state = 12;
				} else if (ch == 'x') {			//16進数のとき
					text.append(ch);
					state = 13;
				} else {						//10進数の0のとき
					backChar(ch);
					state = 3;
				}
				break;
			case 12:	//8進数読み取り
				ch = readChar();
				if ('0' <= ch && ch <= '7') {
					text.append(ch);
				} else {
					backChar(ch);
					tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
					state = 15;
				}
				break;
			case 13:	//16進数読み取り
				ch = readChar();
				if ('0' <= ch && ch <= '9' || 'a' <= ch && ch <= 'f') {
					text.append(ch);
				} else {
					backChar(ch);
					tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
					state = 15;
				}
				break;
			case 14:	//10進数用正常終了処理(16bit範囲を超えるかの判定)
				if (Integer.decode(text.toString()) < -32768 || 32767 < Integer.decode(text.toString())) {
					state = 2;
					System.err.println("Error: 数値が16bit表現可能範囲を超えています");
				} else {
					tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
					accept = true;
				}
				break;
			case 15:	//8.16進数用正常終了処理(16bit範囲を超えるかの判定)
				if (text.toString().equals("0x")) {
					state = 2;
					System.err.println("Error: 16進数の数値がnullです");
				} else if (Integer.decode(text.toString()) > 65535) {
					state = 2;
					System.err.println("Error: 数値が16bit表現可能範囲を超えています");
				} else {
					tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
					accept = true;
				}
				break;
			case 16:	// read one '*'
				tk = new CToken(CToken.TK_MULT, lineNo, startCol, "*");
				accept = true;
				break;
			case 17:
				tk = new CToken(CToken.TK_LPAR, lineNo, startCol, "(");
				accept = true;
				break;
			case 18:
				tk = new CToken(CToken.TK_RPAR, lineNo, startCol, ")");
				accept = true;
				break;
			case 19:
				tk = new CToken(CToken.TK_LBRA, lineNo, startCol, "[");
				accept = true;
				break;
			case 20:
				tk = new CToken(CToken.TK_RBRA, lineNo, startCol, "]");
				accept = true;
				break;
			case 21: //read name of variable
				ch = readChar();
				if (Character.isLowerCase(ch) || Character.isUpperCase(ch) || ch == '_' || Character.isDigit(ch)) {
					text.append(ch);
				} else {
					backChar(ch);
					
					String s = text.toString();
					Integer i = (Integer) rule.get(s);
					tk = new CToken(((i == null) ? CToken.TK_IDENT : i.intValue()), lineNo, startCol, s);
					//tk = new CToken(CToken.TK_IDENT, lineNo, startCol, text.toString());
					accept = true;
				}
				break;
			case 22:
				ch = readChar();
				if (ch == '=') {
					tk = new CToken(CToken.TK_EQ, lineNo, startCol, "==");
				} else {
					backChar(ch);
					tk = new CToken(CToken.TK_ASSIGN, lineNo, startCol, "=");
				}
				accept = true;
				break;
			case 23:
				tk = new CToken(CToken.TK_SEMI, lineNo, startCol, ";");
				accept = true;
				break;
			case 24:
				ch = readChar();
				if (ch == '=') {
					tk = new CToken(CToken.TK_LE, lineNo, startCol, "<=");
				} else {
					backChar(ch);
					tk = new CToken(CToken.TK_LT, lineNo, startCol, "<");
				}
				accept = true;
				break;
			case 25:
				ch = readChar();
				if (ch == '=') {
					tk = new CToken(CToken.TK_GE, lineNo, startCol, ">=");
				} else {
					backChar(ch);
					tk = new CToken(CToken.TK_GT, lineNo, startCol, ">");
				}
				accept = true;
				break;
			case 26:
				ch = readChar();
				if (ch == '=') {
					tk = new CToken(CToken.TK_NE, lineNo, startCol, "!=");
					accept = true;
				} else {
					backChar(ch);
					state = 2;
					System.err.println("Error: miniCV06では!の後は=が必要です");
				}
				break;
			}
		}
		return tk;
	}
}

