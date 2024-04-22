package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import parser.Parser;
import parser.Statement;
import parser.IntDeclaration;
import parser.ExitStatement;
import parser.IntExpression;
import parser.IntTerm;
import parser.IntFactor;

import tokenizer.Token;
import tokenizer.TokenType;

public class AssemblyGenerator implements StatementVisitor {
    private Parser parser;
    private OffsetTable identifiers;

    public AssemblyGenerator(Parser inParser)
    {
        parser = inParser;
        identifiers = new OffsetTable(4);
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

            System.out.println(identifiers.toString());

            return true;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        return false;
    }

    public String visit(IntDeclaration stmt) throws CompileException {
        String a = "";
        a += intExpressionAssembly( stmt.getExpression(), "ebx" );

        if ( identifiers.addIdentifier(stmt.getIdentifier().getValue() ) )
        {
            a += "\tpush ebx\n\n";
            return a;
        }
        
        throw new CompileException("Identifier '" + stmt.getIdentifier().getPos() + "' already defined");
    }

    public String visit(ExitStatement stmt) {
        String a = "";

        a += "\tmov eax, 1\n";
        a += intExpressionAssembly(stmt.getExpression(), "ebx");
        a += "\tint 0x80\n\n";

        return a;
    }

    public String visit(Statement stmt) {
        return "";
    }

    private String intExpressionAssembly(IntExpression expr, String register)
    {
        String a = "";

        a += intTermAssembly(expr.getTerm(), register);

        if (expr.getOperator() != null)
        {
            a += "\tpush edx\n";
            a += intExpressionAssembly(expr.getExpression(), "edx");

            if (expr.getOperator().getType() == TokenType.PLUS)
            {
                a += "\tadd " + register + ", edx\n";
            }
            else if (expr.getOperator().getType() == TokenType.MINUS)
            {
                a += "\tsub " + register + ", edx\n";
            }

            a += "\tpop edx\n";
        }

        return a;
    }

    private String intTermAssembly(IntTerm term, String register)
    {
        String a = "";

        a += intFactorAssembly(term.getFactor(), register);

        if (term.getOperator() != null)
        {
            if (term.getOperator().getType() == TokenType.TIMES)
            {
                a += "\tpush edx\n";
                a += intTermAssembly(term.getTerm(), "edx");
                a += "\timul " + register + ", edx\n";
                a += "\tpop edx\n";
            }
            else if (term.getOperator().getType() == TokenType.DIVISION)
            {
                a += "\tpush edx\n";
                a += "\tpush eax\n";
                a += "\txor edx, edx\n";
                a += "\tmov eax, " + register + "\n";
                a += intTermAssembly(term.getTerm(), register);
                a += "\tidiv " + register + "\n";
                a += "\tmov " + register + ", eax\n";
                a += "\tpop eax\n";
                a += "\tpop edx\n";
            }
        }

        return a;
    }

    private String intFactorAssembly(IntFactor factor, String register)
    {
        String a = "";
        Token token = factor.getToken();
        IntExpression expr = factor.getExpression();

        if (token != null)
        {
            switch (token.getType())
            {
                case LITERAL_INT:
                a += "\tmov " + register + ", " + token.getValue() + "\n";
                break;

                case IDENTIFIER:
                Integer offset = identifiers.getOffset(token.getValue());

                if (offset != -1)
                {
                    a += "\tmov " + register + ", [esp + " + offset.toString() + "]\n";
                }
                break;

                default:
                break;
            }
        }
        else
        {
            a += intExpressionAssembly(expr, register);
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
