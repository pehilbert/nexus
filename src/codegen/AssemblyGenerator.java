package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import parser.Parser;
import parser.Statement;
import parser.StringDeclaration;
import parser.StringReassignment;
import parser.NumDeclaration;
import parser.CharDeclaration;
import parser.ExitStatement;
import parser.PrintStatement;
import parser.NumExpression;
import parser.StringExpression;
import parser.NumTerm;
import parser.NumFactor;
import parser.NumReassignment;
import parser.CharReassignment;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

public class AssemblyGenerator implements StatementVisitor {
    private Parser parser;

    static final String PTR_DATA = "pd";
    static final String BUFFER = "buf";
    static final int BUFFER_SIZE = 1024;

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

            writer.write( generateDataSegment() );
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

    public String visit(NumDeclaration stmt) throws CompileException 
    {
        try
        {
            String a = "";
            a += numExpressionAssembly( stmt.getExpression(), "ebx" );
            a += "\tsub esp, " + Parser.INT_SIZE + "\n";
            a += "\tmov [esp], ebx\n";
            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(CharDeclaration stmt) throws CompileException 
    {
        try
        {
            String a = "";
            a += numExpressionAssembly( stmt.getExpression(), "ebx" );
            a += "\tsub esp, " + Parser.CHAR_SIZE + "\n";
            a += "\tmov [esp], bl\n";
            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(StringDeclaration stmt) throws CompileException
    {
        try
        {
            if (!parser.getSymbolTable().identifierExists(stmt.getIdentifier().getValue()))
            {
                throw new CompileException("Identifier '" + stmt.getIdentifier().getValue() + "' does not exist.");
            }

            int dataOffset = parser.getSymbolTable().getDataOffset(stmt.getIdentifier().getValue());
            String strAddr = PTR_DATA + " + " + dataOffset;

            String a = "";
            a += strExpressionAssembly( stmt.getExpression(), "ebx", strAddr);
            a += "\tsub esp, " + Parser.PTR_SIZE + "\n";
            a += "\tmov [esp], ebx\n";
            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(StringReassignment stmt) throws CompileException
    {
        try
        {
            StringExpression expr = stmt.getExpression();
            Token exprToken = expr.getToken();

            if (exprToken.getType() == TokenType.IDENTIFIER)
            {
                String identifierType = parser.getSymbolTable().getIdentifierType(exprToken.getValue());
                if (identifierType.equals(Tokenizer.TYPE_STRING))
                {
                    String a = "";
                    a += handleIdentifier(expr, "ebx");
                    a += "\tmov [ebp - " + parser.getSymbolTable().getStackOffset(stmt.getIdentifier().getValue()) + "], ebx\n";
                    return a;
                }
                else
                {
                    throw new CompileException("Expected identifier of type str, got one of type " + identifierType);
                }
            }
            else
            {
                throw new CompileException("Reassignment for strings only supported for string identifiers.");
            }
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(NumReassignment stmt) throws CompileException
    {
        try
        {
            String a = "";
            a += numExpressionAssembly( stmt.getExpression(), "ebx" );
            a += "\tmov [ebp - " + parser.getSymbolTable().getStackOffset(stmt.getIdentifier().getValue()) + "], ebx\n";
            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(CharReassignment stmt) throws CompileException
    {
        try
        {
            String a = "";
            a += numExpressionAssembly( stmt.getExpression(), "ebx" );
            a += "\tmov [ebp - " + parser.getSymbolTable().getStackOffset(stmt.getIdentifier().getValue()) + "], bl\n";
            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    public String visit(PrintStatement stmt) throws CompileException 
    {
        StringExpression expr = stmt.getExpression();
        Token exprToken = expr.getToken();
        int dataLen;

        String a = "";

        a += "\tmov eax, 4\n";
        a += "\tmov ebx, 1\n";

        switch (exprToken.getType())
        {
            case LITERAL_STR:
            String str = exprToken.getValue();
            dataLen = str.length() + 1;

            a += "\tmov edx, " + dataLen + "\n";
            a += handleStringLiteral(expr, BUFFER);
            a += "\tmov ecx, " + BUFFER + "\n";
            a += "\tint 0x80\n";
            break;

            case IDENTIFIER:
            dataLen = parser.getSymbolTable().getVarInfo(exprToken.getValue()).getTotalSize();

            a += "\tmov edx, " + dataLen + "\n";
            a += handleIdentifier(expr, "ecx");
            a += "\tint 0x80\n";
            break;

            default:
            throw new CompileException("'print' not supported for token: '" + exprToken.getValue() + "'");
        }
        
        return a;
    }

    public String visit(ExitStatement stmt) throws CompileException 
    {
        try
        {
            String a = "";

            a += "\tmov eax, 1\n";
            a += numExpressionAssembly(stmt.getExpression(), "ebx");
            a += "\tint 0x80\n\n";

            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    // Loads a string literal into the given memory location
    private String handleStringLiteral(StringExpression expr, String location) throws CompileException 
    {
        String a = "";
        String str = expr.getToken().getValue();
    
        for (int i = 0; i < str.length(); i++) {
            a += "\tmov byte [" + location + " + " + i + "], " + (int)str.charAt(i) + "\n";
        }
        a += "\tmov byte [" + location + " + " + str.length() + "], 0\n";
    
        return a;
    }  
    
    // Finds the memory location of the string pointed to by an identifier, and puts the result in the given register 
    private String handleIdentifier(StringExpression expr, String register) throws CompileException 
    {
        int offset = parser.getSymbolTable().getStackOffset(expr.getToken().getValue());

        if (offset != -1)
        {
            return "\tmov " + register + ", [ebp - " + offset + "]\n";
        }

        throw new CompileException("Unknown identifier: " + expr.getToken().getValue());
    }
    
    // Handles a string expression, puts the memory location of the resulting string into the given register
    private String strExpressionAssembly(StringExpression expr, String register, String location) throws CompileException 
    {
        String a = "";
        switch (expr.getToken().getType()) 
        {
            case LITERAL_STR:
            a += handleStringLiteral(expr, location);
            a += "\tlea " + register + ", [" + location +  "]\n";
            break;

            case IDENTIFIER:
            a += handleIdentifier(expr, register);
            break;

            default:
            throw new CompileException("Could not compile this string expression");
        }

        return a;
    }    

    private String numExpressionAssembly(NumExpression expr, String register) throws CompileException
    {
        String a = "";

        // base case: single int term, simply put it into the register
        if (expr.getTerm() != null)
        {
            try
            {
                return numTermAssembly(expr.getTerm(), register);
            }
            catch (CompileException exception)
            {
                throw exception;
            }
        }

        // Evaluate left hand side, put into register
        a += numExpressionAssembly(expr.getLeft(), register);

        // Preserve ecx and register
        a += "\tpush ecx\n";
        a += "\tpush " + register + "\n";

        // Evalute right hand side, put into register
        a += numExpressionAssembly(expr.getRight(), register);

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

    private String numTermAssembly(NumTerm term, String register) throws CompileException
    {
        String a = "";

        // base case: single int factor, simply move into register
        if (term.getFactor() != null)
        {
            try
            {
                return numFactorAssembly(term.getFactor(), register);
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
            a += numTermAssembly(term.getLeft(), register);

            // Preserve edx and register
            a += "\tpush edx\n";
            a += "\tpush " + register + "\n";

            // Evaluate right hand side, put into register
            a += numTermAssembly(term.getRight(), register);

            // Get left hand side off of the stack
            a += "\tpop edx\n";

            // Perform operation
            a += "\timul edx, " + register + "\n";

            // Move result into register
            a += "\tmov " + register + ", edx\n";

            // Restore original edx
            a += "\tpop edx\n";
        }
        else if ( (term.getOperator().getType() == TokenType.DIVISION ||
                 term.getOperator().getType() == TokenType.MOD) && !term.isFloat())
        {
            // Evaluate left hand side, put into register
            a += numTermAssembly(term.getLeft(), register);

            // preserve eax, edx, and register
            a += "\tpush eax\n";
            a += "\tpush edx\n";
            a += "\tpush " + register + "\n";

            // evaluate right hand side, put into register
            a += numTermAssembly(term.getRight(), register);

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
        else if (term.getOperator().getType() == TokenType.DIVISION && term.isFloat())
        {
            
        }

        return a;
    }

    private String numFactorAssembly(NumFactor factor, String register) throws CompileException
    {
        String a = "";
        Token token = factor.getToken();
        NumExpression expr = factor.getExpression();

        if (token != null)
        {
            switch (token.getType())
            {
                case LITERAL_INT:
                a += "\tmov " + register + ", " + token.getValue() + "\n";
                break;

                case LITERAL_CHAR:
                a += "\tmov " + register + ", \'" + token.getValue() + "\'\n";
                break;

                case IDENTIFIER:
                Integer offset = parser.getSymbolTable().getStackOffset(token.getValue());
                String type = parser.getSymbolTable().getIdentifierType(token.getValue());

                if (offset == -1)
                {
                    throw new CompileException("Unknown identifier: '" + token.getValue() + "'");
                }

                if ( !(type.equals(Tokenizer.TYPE_INT) || type.equals(Tokenizer.TYPE_CHAR)) )
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
            a += numExpressionAssembly(expr, register);
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

    private String generateDataSegment()
    {
        String a = "";

        a += "section .data\n";
        a += PTR_DATA + " db " + parser.getSymbolTable().getAllDataSize() + " dup(0)\n";
        a += BUFFER + " db " + BUFFER_SIZE + " dup(0)\n";

        return a;
    }
}
