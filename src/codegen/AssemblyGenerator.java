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
import parser.IntReassignment;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

public class AssemblyGenerator implements StatementVisitor {
    private Parser parser;

    public AssemblyGenerator(Parser inParser)
    {
        parser = inParser;
    }

    public boolean generateProgram(String outputFile) throws CompileException
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
        catch (CompileException e)
        {
            throw e;
        }

        return false;
    }

    public String visit(IntDeclaration stmt) throws CompileException 
    {
        try
        {
            String a = "";
            a += intExpressionAssembly( stmt.getExpression(), "ebx" );
            a += "\tpush ebx\n\n";
            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(IntReassignment stmt) throws CompileException
    {
        try
        {
            String a = "";
            a += intExpressionAssembly( stmt.getExpression(), "ebx" );
            a += "\tmov [ebp - " + parser.getSymbolTable().getTrueOffset(stmt.getIdentifier().getValue()) + "], ebx\n";
            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(ExitStatement stmt) throws CompileException 
    {
        try
        {
            String a = "";

            a += "\tmov eax, 1\n";
            a += intExpressionAssembly(stmt.getExpression(), "ebx");
            a += "\tint 0x80\n\n";

            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    private String intExpressionAssembly(IntExpression expr, String register) throws CompileException
    {
        String a = "";

        // base case: single int term, simply put it into the register
        if (expr.getTerm() != null)
        {
            try
            {
                return intTermAssembly(expr.getTerm(), register);
            }
            catch (CompileException exception)
            {
                throw exception;
            }
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

    private String intTermAssembly(IntTerm term, String register) throws CompileException
    {
        String a = "";

        // base case: single int factor, simply move into register
        if (term.getFactor() != null)
        {
            try
            {
            return intFactorAssembly(term.getFactor(), register);
            }
            catch (CompileException exception)
            {
                throw exception;
            }
        }

        // Handle multiplication
        if (term.getOperator().getType() == TokenType.TIMES)
        {
            // Evaluate left hand side, put into register
            a += intTermAssembly(term.getLeft(), register);

            // Preserve edx and register
            a += "\tpush edx\n";
            a += "\tpush " + register + "\n";

            // Evaluate right hand side, put into register
            a += intTermAssembly(term.getRight(), register);

            // Get left hand side off of the stack
            a += "\tpop edx\n";

            // Perform operation
            a += "\timul edx, " + register + "\n";

            // Move result into register
            a += "\tmov " + register + ", edx\n";

            // Restore original edx
            a += "\tpop edx\n";
        }
        else if (term.getOperator().getType() == TokenType.DIVISION ||
                 term.getOperator().getType() == TokenType.MOD)
        {
            // Evaluate left hand side, put into register
            a += intTermAssembly(term.getLeft(), register);

            // preserve eax, edx, and register
            a += "\tpush eax\n";
            a += "\tpush edx\n";
            a += "\tpush " + register + "\n";

            // evaluate right hand side, put into register
            a += intTermAssembly(term.getRight(), register);

            // get the left hand side off of the stack, put in eax
            a += "\tpop eax\n";

            // sign extend left hand side
            a += "\tcdq\n";

            // perform division with register
            a += "\tidiv " + register + "\n";

            // quotient in eax, remainder in edx, move one of them into
            // register depending on operation
            if (term.getOperator().getType() == TokenType.DIVISION)
            {
                a += "\tmov " + register + ", eax\n";
            }
            else
            {
                a += "\tmov " + register + ", edx\n";
            }

            // restore eax and edx
            a += "\tpop edx\n";
            a += "\tpop eax\n";
        }

        return a;
    }

    private String intFactorAssembly(IntFactor factor, String register) throws CompileException
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
                Integer offset = parser.getSymbolTable().getTrueOffset(token.getValue());
                String type = parser.getSymbolTable().getIdentifierType(token.getValue());

                if (offset == -1)
                {
                    throw new CompileException("Unknown identifier: '" + token.getValue() + "'");
                }

                if (!type.equals(Tokenizer.TYPE_INT))
                {
                    throw new CompileException("Expected identifier of type " + Tokenizer.TYPE_INT + ", got " + type);
                }

                a += "\tmov " + register + ", [ebp - " + offset.toString() + "]\n";
                break;

                default:
                break;
            }
        }
        else
        {
            a += intExpressionAssembly(expr, register);
        }

        if (factor.isNegative())
        {
            a += "\tneg " + register + "\n";
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
