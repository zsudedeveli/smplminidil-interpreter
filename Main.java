package edu.ceng.smplminidil;

import java.nio.file.*;
import java.util.*;

public class Main {
    
    public static void main(String[] args) throws Exception {

        String source;
        /* if (args.length == 0) {
            System.out.println("MiniDil yorumlayıcı: Kodu yapıştırın, ardından Ctrl+Z (Windows: Ctrl+Z) ile çalıştırın.");
            source = new String(System.in.readAllBytes());
        } else {
            source = Files.readString(Path.of(args[0]));
        } */
        source = Files.readString(Path.of("input-tr.min"));
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scan();

        Parser parser = new Parser(tokens);
        List<Stmt> program = parser.parseProgram();

        Interpreter interpreter = new Interpreter();
        interpreter.execute(program);
    }
}