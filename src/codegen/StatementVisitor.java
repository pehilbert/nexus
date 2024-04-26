package codegen;

import parser.IntDeclaration;
import parser.ExitStatement;

public interface StatementVisitor {
    String visit(IntDeclaration stmt) throws CompileException;
    String visit(ExitStatement stmt) throws CompileException;
}
