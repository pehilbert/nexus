package codegen;

import parser.Statement;
import parser.IntDeclaration;
import parser.ExitStatement;

public interface StatementVisitor {
    String visit(IntDeclaration stmt);
    String visit(ExitStatement stmt);
    String visit(Statement stmt);
}
