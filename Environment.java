package edu.ceng.smplminidil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();

    public Environment() { scopes.push(new HashMap<>()); }

    public void pushScope() { scopes.push(new HashMap<>()); }
    public void popScope()  { scopes.pop(); }

    public void define(String name, Object value) {
        scopes.peek().put(name, value);
    }

    public void assign(String name, Object value) {
        for (Map<String,Object> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        throw new RuntimeException("Tanımsız değişken: '" + name + "'");
    }

    public Object get(String name) {
        for (Map<String,Object> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        throw new RuntimeException("Tanımsız değişken: '" + name + "'");
    }
}