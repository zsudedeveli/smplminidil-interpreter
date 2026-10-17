package edu.ceng.smplminidil;

import java.util.List;
import java.util.Objects;

public class Interpreter {

    private final Environment env = new Environment();

    public void execute(List<Stmt> program) {
        for (Stmt s : program) {
            execStmt(s);
        }
    }

    private void execStmt(Stmt s) {
        if (s instanceof Var) {
            execVar((Var) s);
        } else if (s instanceof Assign) {
            execAssign((Assign) s);
        } else if (s instanceof Print) {
            execPrint((Print) s);
        } else if (s instanceof Block) {
            execBlock((Block) s);
        } else if (s instanceof If) {
            execIf((If) s);
        } else if (s instanceof While) {
            execWhile((While) s);
        } else if (s instanceof For) {
            execFor((For) s);
        } else {
            throw new RuntimeException("Bilinmeyen deyim tipi: " + s.getClass());
        }
    }

    private void execVar(Var s) {
        Object v = evalExpr(s.value);
        env.define(s.name, v);
    }

    private void execAssign(Assign s) {
        Object v = evalExpr(s.value);
        env.assign(s.name, v);
    }

    private void execPrint(Print s) {
        Object v = evalExpr(s.value);
        System.out.println(stringify(v));
    }

    private void execBlock(Block s) {
        env.pushScope();
        try {
            for (Stmt st : s.statements) {
                execStmt(st);
            }
        } finally {
            env.popScope();
        }
    }

    private void execIf(If s) {
        Object c = evalExpr(s.condition);
        requireBoolean(c, "if koşulu");
        if ((Boolean) c) {
            execBlock(s.thenBlk);
        } else if (s.elseBlk != null) {
            execBlock(s.elseBlk);
        }
    }

    private void execWhile(While s) {
        while (true) {
            Object c = evalExpr(s.condition);
            requireBoolean(c, "while koşulu");
            if (!((Boolean) c)) break;
            execBlock(s.body);
        }
    }

  private void execFor(For s) {

    execStmt(s.initializer);

    if (!(s.initializer instanceof Assign)) {
        throw new RuntimeException("dön başlangıcı atama olmalıdır");
    }

    Assign initAssign = (Assign) s.initializer;
    String loopVar = initAssign.name;

    if (!(s.increment instanceof Assign)) {
        throw new RuntimeException("adım atama olmalıdır");
    }

    Assign stepAssign = (Assign) s.increment;
    Object stepObj = evalExpr(stepAssign.value);

    if (!(stepObj instanceof Integer)) {
        throw new RuntimeException("adım sayısal olmalıdır");
    }

    int step = (Integer) stepObj;

    while (true) {
        Object cond = evalExpr(s.condition);
        requireBoolean(cond, "dön koşulu");
        if (!((Boolean) cond)) break;

        execBlock(s.body);

        Object current = env.get(loopVar);
        if (!(current instanceof Integer)) {
            throw new RuntimeException("Döngü değişkeni sayısal olmalıdır");
        }

        env.assign(loopVar, (Integer) current + step);
    }
}


    private Object evalExpr(Expr e) {
        if (e instanceof Literal) {
            return ((Literal) e).value;
        } else if (e instanceof VarExpr) {
            return env.get(((VarExpr) e).name);
        } else if (e instanceof Grouping) {
            return evalExpr(((Grouping) e).inner);
        } else if (e instanceof Unary) {
            return evalUnary((Unary) e);
        } else if (e instanceof Binary) {
            return evalBinary((Binary) e);
        } else {
            throw new RuntimeException("Bilinmeyen ifade tipi: " + e.getClass());
        }
    }

    private Object evalUnary(Unary u) {
        Object v = evalExpr(u.right);
        switch (u.op.type) {
            case BANG:
                return !bool(v);
            case MINUS:
                return -num(v);
            default:
                throw new RuntimeException("Bilinmeyen tekli işlem: " + u.op.type);
        }
    }

    private Object evalBinary(Binary b) {
        Object L = evalExpr(b.left);
        Object R = evalExpr(b.right);

        switch (b.op.type) {
            case PLUS:           return num(L) + num(R);
            case MINUS:          return num(L) - num(R);
            case STAR:           return num(L) * num(R);
            case SLASH: {
                int d = num(R);
                if (d == 0) throw new RuntimeException("Sıfıra bölme hatası");
                return num(L) / d;
            }
            case PERCENT: {
                int d = num(R);
                if (d == 0) throw new RuntimeException("Sıfıra mod alma hatası");
                return num(L) % d;
            }
            case GREATER:        return num(L) > num(R);
            case GREATER_EQUAL:  return num(L) >= num(R);
            case LESS:           return num(L) < num(R);
            case LESS_EQUAL:     return num(L) <= num(R);
            case EQUAL_EQUAL:    return Objects.equals(L, R);
            case BANG_EQUAL:     return !Objects.equals(L, R);
            case AND_AND:        return bool(L) && bool(R);
            case OR_OR:          return bool(L) || bool(R);
            default:
                throw new RuntimeException("Bilinmeyen ikili işlem: " + b.op.type);
        }
    }

    private int num(Object v) {
        if (v instanceof Integer) return (Integer) v;
        throw new RuntimeException("Sayı bekleniyordu, ancak bulunan: " + v);
    }

    private boolean bool(Object v) {
        if (v instanceof Boolean) return (Boolean) v;
        throw new RuntimeException("Boolean bekleniyordu, ancak bulunan: " + v);
    }

    private void requireBoolean(Object v, String ctx) {
        if (!(v instanceof Boolean)) {
            throw new RuntimeException(ctx + " için boolean bekleniyordu, ancak bulunan: " + v);
        }
    }

    private String stringify(Object v) {
        if (v instanceof Boolean b) {
            return b ? "doğru" : "yanlış";
        }
        return v == null ? "null" : v.toString();
    }
}
