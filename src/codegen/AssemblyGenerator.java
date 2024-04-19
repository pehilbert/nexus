package codegen;

import parser.Statement;
import parser.IntDeclaration;
import parser.ExitStatement;

public class AssemblyGenerator implements StatementVisitor {
    public String visit(IntDeclaration stmt) {
        return "IntDeclaration\n";
    }

    public String visit(ExitStatement stmt) {
        return "ExitStatement\n";
    }

    public String visit(Statement stmt) {
        return "Statement\n";
    }

    public String generatePreamble()
    {
        String a = "";

        a += "section .text\n";
        a += "global _start\n\n";
        a += "_start:\n";

        return a;
    }
}
