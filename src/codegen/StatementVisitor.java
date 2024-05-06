package codegen;

import parser.NumDeclaration;
import parser.CharDeclaration;
import parser.StringDeclaration;
import parser.NumReassignment;
import parser.CharReassignment;
import parser.StringReassignment;
import parser.PrintStatement;
import parser.ExitStatement;

public interface StatementVisitor {
    String visit(NumDeclaration stmt) throws CompileException;
    String visit(CharDeclaration stmt) throws CompileException;
    String visit(StringDeclaration stmt) throws CompileException;
    String visit(NumReassignment stmt) throws CompileException;
    String visit(CharReassignment stmt) throws CompileException;
    String visit(StringReassignment stmt) throws CompileException;
    String visit(PrintStatement stmt) throws CompileException;
    String visit(ExitStatement stmt) throws CompileException;
}
