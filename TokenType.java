package edu.ceng.smplminidil;
public enum TokenType {
    // Tek/çift karakterli işaretler
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, SEMICOLON, EQUAL, BANG, BANG_EQUAL,
    EQUAL_EQUAL, GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL, PLUS, MINUS, STAR, SLASH, PERCENT,
    AND_AND, OR_OR,

    // Kimlikler, sabitler, anahtar sözcükler
    IDENT, NUMBER,
    TRUE, FALSE,        // "doğru", "yanlış"
    LET,                // "tanım"
    IF, THEN, ELSE,     // "eğer", "ise", "değilse"
    WHILE,              // "doğruyken"
    FOR,                // "dön"
    PRINT,              // "yaz"

    EOF
}