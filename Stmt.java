package edu.ceng.smplminidil;

import java.util.List;

interface Stmt {
    // sadece işaret arayüzü
}

// tanım x = expr;
class Var implements Stmt {
    final String name;
    final Expr value;

    Var(String name, Expr value) {
        this.name = name;
        this.value = value;
    }
}

// x = expr;
class Assign implements Stmt {
    final String name;
    final Expr value;

    Assign(String name, Expr value) {
        this.name = name;
        this.value = value;
    }
}

// yaz expr;
class Print implements Stmt {
    final Expr value;

    Print(Expr value) {
        this.value = value;
    }
}

// { ... }
class Block implements Stmt {
    final List<Stmt> statements;

    Block(List<Stmt> statements) {
        this.statements = statements;
    }
}

// eğer (cond) ise { ... } değilse { ... }
class If implements Stmt {
    final Expr condition;
    final Block thenBlk;
    final Block elseBlk;  // null olabilir

    If(Expr condition, Block thenBlk, Block elseBlk) {
        this.condition = condition;
        this.thenBlk = thenBlk;
        this.elseBlk = elseBlk;
    }
}

// (cond) doğruyken { ... }
class While implements Stmt {
    final Expr condition;
    final Block body;

    While(Expr condition, Block body) {
        this.condition = condition;
        this.body = body;
    }
}

// dön (init, cond, step) { ... }
class For implements Stmt {
    final Stmt initializer;
    final Expr condition;
    final Stmt increment;
    final Block body;

    For(Stmt initializer, Expr condition, Stmt increment, Block body) {
        this.initializer = initializer;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }
}