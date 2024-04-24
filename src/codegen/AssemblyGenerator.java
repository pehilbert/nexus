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

            //System.out.println(identifiers.toString());

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

        // base case: single int term, simply put it into the register
        if (expr.getTerm() != null)
        {
            return intTermAssembly(expr.getTerm(), register);
        }

        // Evaluate left hand side, put into register
        a += intExpressionAssembly(expr.getLeft(), register);

        // Preserve ecx and register
        a += "\tpush ecx\n";
        a += "\tpush " + register + "\n";

        // Evalute right hand side, put into register
        a += intExpressionAssembly(expr.getRight(), register);

        // Get left hand side off of the stack
        a += "\tpop ecx\n";

        // Perform operation
        if (expr.getOperator().getType() == TokenType.PLUS)
        {
            a += "\tadd ecx, " + register + "\n";
        }
        else if (expr.getOperator().getType() == TokenType.MINUS)
        {
            a += "\tsub ecx, " + register + "\n";
        }

        // Move result into register
        a += "\tmov " + register + ", ecx\n";

        // Restore original ecx
        a += "\tpop ecx\n";

        return a;
    }

    private String intTermAssembly(IntTerm term, String register)
    {
        String a = "";

        // base case: single int factor, simply move into register
        if (term.getOperator() == null)
        {
            return intFactorAssembly(term.getLeft(), register);
        }

        // Handle multiplication
        if (term.getOperator().getType() == TokenType.TIMES)
        {
            // Evaluate left hand side, put into register
            a += intFactorAssembly(term.getLeft(), register);

            // Preserve edx and register
            a += "\tpush edx\n";
            a += "\tpush " + register + "\n";

            // Evaluate right hand side, put into register
            a += intFactorAssembly(term.getRight(), register);

            // Get left hand side off of the stack
            a += "\tpop edx\n";

            // Perform operation
            a += "\timul edx, " + register + "\n";

            // Move result into register
            a += "\tmov " + register + ", edx\n";

            // Restore original edx
            a += "\tpop edx\n";
        }
        else if (term.getOperator().getType() == TokenType.DIVISION)
        {
            // Evaluate left hand side, put into register
            a += intFactorAssembly(term.getLeft(), register);

            // preserve eax, edx, and register
            a += "\tpush eax\n";
            a += "\tpush edx\n";
            a += "\tpush " + register + "\n";

            // evaluate right hand side, put into register
            a += intFactorAssembly(term.getRight(), register);

            // get the left hand side off of the stack, put in eax
            a += "\tpop eax\n";

            // sign extend left hand side
            a += "\tcdq\n";

            // perform division with register
            a += "\tidiv " + register + "\n";

            // move result in eax into register 
            a += "\tmov " + register + ", eax\n";

            // restore eax and edx
            a += "\tpop edx\n";
            a += "\tpop eax\n";
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
                    a += "\tmov " + register + ", [ebp - " + offset.toString() + "]\n";
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
        a += "\tmov ebp, esp\n";

        return a;
    }
}
