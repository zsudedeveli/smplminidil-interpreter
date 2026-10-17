package edu.ceng.smplminidil;
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    public final int col;

    public Token(TokenType type, String lexeme, Object literal, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return type + " '" + lexeme + "'" + (literal != null ? ("=" + literal) : "")
               + " @" + line + ":" + col;
    }
}