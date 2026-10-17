package edu.ceng.smplminidil;
import java.util.*;

public class Lexer {
    private final String src;
    private final int n;
    private int i = 0, line = 1, col = 1;
    private final List<Token> tokens = new ArrayList<>();

    // Türkçe anahtar sözcükler
    private static final Map<String, TokenType> keywords = Map.ofEntries(
		Map.entry("doğru",      TokenType.TRUE),
        Map.entry("yanlış",     TokenType.FALSE),
        Map.entry("tanım",      TokenType.LET),
        Map.entry("eğer",       TokenType.IF),
        Map.entry("ise",        TokenType.THEN),
        Map.entry("değilse",    TokenType.ELSE),
        Map.entry("doğruyken",  TokenType.WHILE),
        Map.entry("dön",        TokenType.FOR),
        Map.entry("yaz",        TokenType.PRINT)
    );

    public Lexer(String src) {
        this.src = src;
        this.n = src.length();
    }

    public List<Token> scan() {
        while (!isAtEnd()) {
            int startCol = col;
            char c = advance();

            // Spaces
            if (isWhitespace(c)) continue;

            // Comments // ...
            if (c == '/' && match('/')) {
                while (!isAtEnd() && peek() != '\n') advance();
                continue;
            }

            switch (c) {
                case '(' -> add(TokenType.LEFT_PAREN, "(", null, startCol);
                case ')' -> add(TokenType.RIGHT_PAREN, ")", null, startCol);
                case '{' -> add(TokenType.LEFT_BRACE, "{", null, startCol);
                case '}' -> add(TokenType.RIGHT_BRACE, "}", null, startCol);
                case ';' -> add(TokenType.SEMICOLON, ";", null, startCol);
                case ',' -> add(TokenType.COMMA, ",", null, startCol); // COMMA eklendi
                case '+' -> add(TokenType.PLUS, "+", null, startCol);
                case '-' -> add(TokenType.MINUS, "-", null, startCol);
                case '*' -> add(TokenType.STAR, "*", null, startCol);
                case '%' -> add(TokenType.PERCENT, "%", null, startCol);
                case '/' -> add(TokenType.SLASH, "/", null, startCol);
                case '&' -> add(match('&') ? TokenType.AND_AND : TokenType.BANG, matchLexeme('&', '&'), null, startCol);
                case '|' -> add(match('|') ? TokenType.OR_OR : TokenType.BANG, matchLexeme('|', '|'), null, startCol);
                case '!' -> add(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG, matchLexeme('!', '='), null, startCol);
                case '=' -> add(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL, matchLexeme('=', '='), null, startCol);
                case '<' -> add(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS, matchLexeme('<', '='), null, startCol);
                case '>' -> add(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER, matchLexeme('>', '='), null, startCol);
                
                default -> {
                    if (isDigit(c)) number(c, startCol);
                    else if (isAlpha(c)) ident(c, startCol);
                    else error("Beklenmeyen karakter: '" + c + "'", startCol);
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, "", null, line, col));
        return tokens;
    }

    private void ident(char first, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (isAlphaNum(peek())) {
           sb.append(advance());
        }
        String text = sb.toString();
        TokenType type = keywords.getOrDefault(text, TokenType.IDENT);
        add(type, text, null, startCol);
    }

    private void number(char first, int startCol) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (isDigit(peek())) {
            sb.append(advance());
        }
        add(TokenType.NUMBER, sb.toString(), Integer.parseInt(sb.toString()), startCol);
    }

    private boolean isWhitespace(char c) {
        if (c == ' ' || c == '\r' || c == '\t') { col++; return true; }
        if (c == '\n') { line++; col = 1; return true; }
        return false;
    }
	// -----------------------------------------------------------------
	// Helper functions...  DO NOT MODIFY
	// -----------------------------------------------------------------
    private boolean isTurkishAlpha(char c) {
        return isAlpha(c) || "çÇğĞıİöÖşŞüÜ".indexOf(c) != -1;
    }
    private boolean isAlpha(char c) {
        return Character.isLetter(c);
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isAlphaNum(char c) {
        return isTurkishAlpha(c) || isDigit(c) || c == '_';
    }

    private char advance() {
        char c = src.charAt(i++);
        if (c != '\n') col++;
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (src.charAt(i) != expected) return false;
        i++; col++;
        return true;
    }

    private String matchLexeme(char first, char eq) {
        if (!isAtEnd() && src.charAt(i - 1) == eq) return "" + first + eq;
        return "" + first;
    }

    private char peek() { return isAtEnd() ? '\0' : src.charAt(i); }
    private boolean isAtEnd() { return i >= n; }

    private void add(TokenType type, String lex, Object lit, int startCol) {
        tokens.add(new Token(type, lex, lit, line, startCol));
    }

    private void error(String msg, int startCol) {
        throw new RuntimeException("Syntax error " + line + ":" + startCol + " - " + msg);
    }
}