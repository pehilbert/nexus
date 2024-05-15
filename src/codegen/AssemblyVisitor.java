package codegen;

import parser.*;

public interface AssemblyVisitor {
    String visit(Scope stmt) throws CompileException;
    String visit(FunctionDeclaration stmt) throws CompileException;
    String visit(FunctionCall stmt) throws CompileException;
    String visit(ReturnStatement stmt) throws CompileException;
    String visit(Declaration stmt) throws CompileException;
    String visit(Reassignment stmt) throws CompileException;
    String visit(PrintStatement stmt) throws CompileException;
    String visit(ExitStatement stmt) throws CompileException;
    String visit(NumExpression expr, String register, boolean floatMode) throws CompileException;
    String visit(CharExpression expr, String register) throws CompileException;
    String visit(StringExpression expr, String register) throws CompileException;
}
