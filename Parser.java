package edu.ceng.smplminidil;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) { this.tokens = tokens; }

    public List<Stmt> parseProgram() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(statement());
        }
        return stmts;
    }

    // ---------- STATEMENTS ----------

    private Stmt statement() {
        if (match(TokenType.LET))   return varDecl();
        if (match(TokenType.PRINT)) return printStmt();
        if (match(TokenType.IF))    return ifStmt();
        if (match(TokenType.FOR)) return forStmt();
        if (match(TokenType.LEFT_BRACE)) return blockBody();
        
        // While döngüsü için özel kontrol: ( ile başlayıp sonra doğruyken beklenir.
        if (check(TokenType.LEFT_PAREN) && checkNext(TokenType.IDENT)) {
             // Basit bir heuristic: IDENT ifadesiyle başlıyorsa atama veya while olabilir.
             // Sadece '(' işaretini tüketerek expression()'a gireriz ve sonrasında ne geldiğine bakarız.
             // Bu dilin grameri while döngüsünü: "(" expression ")" "doğruyken" block olarak tanımlıyor.
             // Eğer '(' var ve sonraki ifade mantıklı bir şekilde bir koşul ifadesi olabilirse, expression()'ı deneriz.
             
             Token paren = advance(); // '(' tüket
             Expr cond = expression();
             consume(TokenType.RIGHT_PAREN, "While koşulundan sonra ')' bekleniyor.");
             if (match(TokenType.WHILE)) {
                 return new While(cond, block());
             } else {
                 // Eğer 'doğruyken' gelmediyse, bu bir grouping ifadesi olmalıydı. 
                 // Bu durum, expression/grouping/statement karışımından dolayı parser'ı karmaşıklaştırır.
                 // Basitçe: Eğer '(' ile başlayıp ')' gelmişse ve sonra 'doğruyken' yoksa, bu bir deyim hatasıdır.
                 // Geri alıp hata fırlatmak
                 current--; // ')' geri al
                 current--; // expr'ı geri al (zor)
                 current--; // '(' geri al
                 throw error(paren, "Deyim bekleniyor. 'eğer', 'tanım', 'yaz', 'dön', '{' veya while koşulu bekleniyor.");
             }
        }
        
        // Atama (Assign) - Eğer identifier ile başlıyorsa ve sonra '=' geliyorsa.
        if (check(TokenType.IDENT) && checkNext(TokenType.EQUAL)) {
             return assign();
        }
        
        throw error(peek(), "Deyim bekleniyor.");
    }
    
    // Var Decl, Assign, Print, If, Block: (İçerikleri önceki yanıtlardan alınmıştır)

    private Stmt varDecl() {
        Token name = consume(TokenType.IDENT, "'tanım' sonrası değişken adı bekleniyor.");
        consume(TokenType.EQUAL, "Değişken adından sonra '=' bekleniyor.");
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Değişken tanımı sonrası ';' bekleniyor.");
        return new Var(name.lexeme, value);
    }

    private Stmt assign() {
        Token name = consume(TokenType.IDENT, "Atama için değişken adı bekleniyor."); // IDENT'i burada tüketelim
        consume(TokenType.EQUAL, "Atama için '=' bekleniyor.");
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Atama sonrası ';' bekleniyor.");
        return new Assign(name.lexeme, value);
    }

    private Stmt printStmt() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "'yaz' sonrası ';' bekleniyor.");
        return new Print(value);
    }

    private Stmt ifStmt() {
        consume(TokenType.LEFT_PAREN, "'eğer' sonrasında '(' bekleniyor.");
        Expr cond = expression();
        consume(TokenType.RIGHT_PAREN, "'eğer' koşulundan sonra ')' bekleniyor.");
        consume(TokenType.THEN, "'eğer (...) ise' için 'ise' bekleniyor.");
        Block thenBlk = block();
        Block elseBlk = null;
        if (match(TokenType.ELSE)) {
            elseBlk = block();
        }
        return new If(cond, thenBlk, elseBlk);
    }

    private Stmt whileStmt() {
        consume(TokenType.LEFT_PAREN, "'(' bekleniyor.");
        Expr cond = expression();
        consume(TokenType.RIGHT_PAREN, "Koşul sonrası ')' bekleniyor.");
        consume(TokenType.WHILE, "'doğruyken' bekleniyor.");
        Block body = block();
        return new While(cond, body);
    }

    private Stmt forStmt() {
        consume(TokenType.LEFT_PAREN, "'dön' sonrasında '(' bekleniyor.");
        
        // 1. Başlatıcı (Initializer) -> Ya VarDecl ya da Assign olmalı, ancak ";" ile bitmemeli.
        Stmt initializer = null;
        if (match(TokenType.LET)) {
            Token name = consume(TokenType.IDENT, "'tanım' sonrası değişken adı bekleniyor.");
            consume(TokenType.EQUAL, "Değişken adından sonra '=' bekleniyor.");
            Expr value = expression();
            initializer = new Var(name.lexeme, value);
        } else if (check(TokenType.IDENT) && checkNext(TokenType.EQUAL)) {
            initializer = stepStatement(); // IDENT = expr formatını kullan
        } 
        
        consume(TokenType.COMMA, "Başlatıcı deyimden sonra ',' bekleniyor.");

        // 2. Koşul (Condition)
        Expr cond = expression();
        consume(TokenType.COMMA, "Koşul ifadesinden sonra ',' bekleniyor.");

        // 3. Adım (Increment/Step) -> Sadece Assign (IDENT = expr) bekleniyor
        Stmt increment = stepStatement(); 
        
        consume(TokenType.RIGHT_PAREN, "For döngüsü tanımlaması sonrası ')' bekleniyor.");
        
        Block body = block();
        return new For(initializer, cond, increment, body);
    }
    
    // stepStatement: IDENT "=" expression (noktalı virgül olmadan)
    private Stmt stepStatement() {
        Token name = consume(TokenType.IDENT, "Adım ifadesinde değişken adı bekleniyor.");
        consume(TokenType.EQUAL, "Adım ifadesinde '=' bekleniyor.");
        Expr value = expression();
        return new Assign(name.lexeme, value); 
    }


    private Block block() {
        consume(TokenType.LEFT_BRACE, "Blok başında '{' bekleniyor.");
        return blockBody();
    }

    private Block blockBody() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(statement());
        }
        consume(TokenType.RIGHT_BRACE, "Blok sonunda '}' bekleniyor.");
        return new Block(stmts);
    }

    // ---------- EXPRESSIONS ----------

    private Expr expression() { return logicOr(); }

    private Expr logicOr() {
        Expr expr = logicAnd();
        while (match(TokenType.OR_OR)) {
           Token op = previous();
           Expr right = logicAnd();
           expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr logicAnd() {
        Expr expr = equality();
        while (match(TokenType.AND_AND)) {
            Token op = previous();
            Expr right = equality();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token op = previous();
            Expr right = comparison();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            Token op = previous();
            Expr right = term();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
           Token op = previous();
           Expr right = factor();
           expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token op = previous();
            Expr right = unary();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token op = previous();
            Expr right = unary();
            return new Unary(op, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Literal(false);
        if (match(TokenType.TRUE)) return new Literal(true);
        if (match(TokenType.NUMBER)) return new Literal(previous().literal);
        if (match(TokenType.IDENT)) return new VarExpr(previous().lexeme);
        if (match(TokenType.LEFT_PAREN)) {
            Expr e = expression();
            consume(TokenType.RIGHT_PAREN, "Parantezli ifadeyi kapatmak için ')' bekleniyor.");
            return new Grouping(e);
        }
        throw error(peek(), "İfade bekleniyor.");
    }
	// ------------------------------------------------------------
    // ---------- HELPERS FUNCTIONS, DO NOT MODIFY ----------------
	// ------------------------------------------------------------
    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) { advance(); return true; }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() { return peek().type == TokenType.EOF; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }

    private Token consume(TokenType type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    private ParseError error(Token t, String msg) {
        return new ParseError("Ayrıştırma hatası " + t.line + ":" + t.col + " '" + t.lexeme + "' yakınında: " + msg);
    }

    static class ParseError extends RuntimeException {
        ParseError(String m) { super(m); }
    }
}