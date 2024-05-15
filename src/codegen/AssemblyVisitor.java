package codegen;

import parser.Scope;
import parser.FunctionDeclaration;
import parser.CharExpression;
import parser.Declaration;
import parser.Reassignment;
import parser.PrintStatement;
import parser.ExitStatement;
import parser.ReturnStatement;
import parser.NumExpression;
import parser.StringExpression;

public interface AssemblyVisitor {
    String visit(Scope stmt) throws CompileException;
    String visit(FunctionDeclaration stmt) throws CompileException;
    String visit(ReturnStatement stmt) throws CompileException;
    String visit(Declaration stmt) throws CompileException;
    String visit(Reassignment stmt) throws CompileException;
    String visit(PrintStatement stmt) throws CompileException;
    String visit(ExitStatement stmt) throws CompileException;
    String visit(NumExpression expr, String register, boolean floatMode) throws CompileException;
    String visit(CharExpression expr, String register) throws CompileException;
    String visit(StringExpression expr, String register) throws CompileException;
}
