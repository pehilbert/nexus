package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import parser.Parser;
import parser.Statement;
import parser.IntDeclaration;
import parser.ExitStatement;
import parser.IntExpression;

import tokenizer.Token;

public class AssemblyGenerator implements StatementVisitor {
    private Parser parser;

    public AssemblyGenerator(Parser inParser)
    {
        parser = inParser;
    }

    public boolean generateProgram(String outputFile)
    {
        try (FileWriter writer = new FileWriter(outputFile)) 
        {          
            List<Statement> program = parser.getProgram();
            writer.write( generatePreamble() );

            // write assembly code
            int i = 0;

            while (i < program.size())
            {
                writer.write( program.get(i).accept(this) );
                i++;
            }

            return true;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        return false;
    }

    public String visit(IntDeclaration stmt) {
        String a = "";

        a += intExpressionAssembly( stmt.getExpression(), "ebx" );
        a += "\tpush ebx\n";

        return a;
    }

    public String visit(ExitStatement stmt) {
        String a = "";

        a += "\tmov eax, 1\n";
        a += intExpressionAssembly(stmt.getExpression(), "ebx");
        a += "\tint 0x80\n";

        return a;
    }

    public String visit(Statement stmt) {
        return "";
    }

    private String intExpressionAssembly(IntExpression expr, String register)
    {
        String a = "";
        Token termToken = expr.getTerm().getToken();

        switch (termToken.getType())
        {
            case LITERAL_INT:
            a += "\tmov " + register + ", " + termToken.getValue() + "\n";
            break;

            case IDENTIFIER:
            a += "\tmov " + register + ", 0\n";
            break;

            default:
            break;
        }

        return a;
    }

    private String generatePreamble()
    {
        String a = "";

        a += "section .text\n";
        a += "global _start\n\n";
        a += "_start:\n";

        return a;
    }
}
