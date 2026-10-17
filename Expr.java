package edu.ceng.smplminidil;

interface Expr {
    // sadece işaret arayüzü
}

// İkili işlemler: a + b, a < b, a && b, vb.
class Binary implements Expr {
    final Expr left;
    final Token op;
    final Expr right;

    Binary(Expr left, Token op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}

// Tekli işlemler: -x, !x
class Unary implements Expr {
    final Token op;
    final Expr right;

    Unary(Token op, Expr right) {
        this.op = op;
        this.right = right;
    }
}

// Sabitler: sayı, doğru, yanlış
class Literal implements Expr {
    final Object value; // Integer veya Boolean

    Literal(Object value) {
        this.value = value;
    }
}

// Değişken ismi
class VarExpr implements Expr {
    final String name;

    VarExpr(String name) {
        this.name = name;
    }
}

// Parantezli ifadeler: (expr)
class Grouping implements Expr {
    final Expr inner;

    Grouping(Expr inner) {
        this.inner = inner;
    }
}