package codegen;

import parser.IntDeclaration;
import parser.CharDeclaration;
import parser.StringDeclaration;
import parser.IntReassignment;
import parser.CharReassignment;
import parser.StringReassignment;
import parser.PrintStatement;
import parser.ExitStatement;

public interface StatementVisitor {
    String visit(IntDeclaration stmt) throws CompileException;
    String visit(CharDeclaration stmt) throws CompileException;
    String visit(StringDeclaration stmt) throws CompileException;
    String visit(IntReassignment stmt) throws CompileException;
    String visit(CharReassignment stmt) throws CompileException;
    String visit(StringReassignment stmt) throws CompileException;
    String visit(PrintStatement stmt) throws CompileException;
    String visit(ExitStatement stmt) throws CompileException;
}
