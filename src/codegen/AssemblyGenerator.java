package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

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

    static final String STR_SIZE_MD = "strSize";
    static final String PTR_DATA = "pd";
    static final String BUFFER = "buf";
    static final String FLOAT_NEG_MASK = "nmask";
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
            writer.write( generateDataSegment() );
            writer.write("\n");
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
            if (stmt.getExpression().isFloat())
            {
                a += numExpressionAssembly( stmt.getExpression(), "xmm0", true );
                a += "\tsub esp, " + Parser.FLOAT_SIZE + "\n";
                a += "\tmovss [esp], xmm0\n";
            }
            else
            {
                a += numExpressionAssembly( stmt.getExpression(), "ebx", false );
                a += "\tsub esp, " + Parser.INT_SIZE + "\n";
                a += "\tmov [esp], ebx\n";
            }
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
            a += numExpressionAssembly( stmt.getExpression(), "ebx", false );
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

            updateStringLength(stmt.getIdentifier().getValue(), stmt.getExpression());

            String a = "";
            a += strExpressionAssembly( stmt.getExpression(), "ebx");
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
            updateStringLength(stmt.getIdentifier().getValue(), expr);

            String a = "";
            a += strExpressionAssembly(expr, "ebx");
            a += "\tmov [ebp - " + parser.getSymbolTable().getStackOffset(stmt.getIdentifier().getValue()) + "], ebx\n";
            
            return a;
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
            if (stmt.getExpression().isFloat())
            {
                a += numExpressionAssembly(stmt.getExpression(), "xmm0", true);
                a += a += "\tmovss [ebp - " + parser.getSymbolTable().getStackOffset(stmt.getIdentifier().getValue()) + "], xmm0\n";
            }
            else
            {
                a += numExpressionAssembly(stmt.getExpression(), "ebx", false);
                a += "\tmov [ebp - " + parser.getSymbolTable().getStackOffset(stmt.getIdentifier().getValue()) + "], ebx\n";
            }

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
            a += numExpressionAssembly( stmt.getExpression(), "ebx", false );
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
        String dataLen;

        String a = "";

        a += "\tmov eax, 4\n";
        a += "\tmov ebx, 1\n";

        switch (exprToken.getType())
        {
            case LITERAL_STR:
            String str = exprToken.getValue();
            dataLen = Integer.toString(str.length() + 1);

            a += "\tmov edx, " + dataLen + "\n";
            a += handleStringLiteral(expr, "ecx");
            a += "\tint 0x80\n";
            break;

            case IDENTIFIER:
            dataLen = parser.getSymbolTable().getVarInfo(exprToken.getValue()).getMetaData(STR_SIZE_MD);

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
            a += numExpressionAssembly(stmt.getExpression(), "ebx", false);
            a += "\tint 0x80\n\n";

            return a;
        }
        catch (CompileException exception)
        {
            throw exception;
        }
    }

    private void updateStringLength(String identifier, StringExpression expr)
    {
        switch (expr.getToken().getType())
        {
            case LITERAL_STR:
            parser.getSymbolTable().getVarInfo(identifier).updateMetaData(STR_SIZE_MD, Integer.toString(expr.getToken().getValue().length()));
            break;

            case IDENTIFIER:
            parser.getSymbolTable().getVarInfo(identifier).updateMetaData(STR_SIZE_MD, parser.getSymbolTable().getVarInfo(identifier).getMetaData(STR_SIZE_MD));
            break;

            default:
            break;
        }
    }

    // Loads the memory location of a string literal into a given register
    private String handleStringLiteral(StringExpression expr, String register) throws CompileException 
    {
        String a = "";
        String str = expr.getToken().getValue();
        String type = parser.getLitTable().getLiteralType(str);
        String label = parser.getLitTable().getLiteralLabel(str);

        if (!type.equals(Tokenizer.TYPE_STRING) || label == null)
        {
            throw new CompileException("Could not resolve string literal: " + str);
        }

        a += "\tlea " + register + ", " + label + "\n";
    
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
    private String strExpressionAssembly(StringExpression expr, String register) throws CompileException 
    {
        String a = "";
        switch (expr.getToken().getType()) 
        {
            case LITERAL_STR:
            a += handleStringLiteral(expr, register);
            break;

            case IDENTIFIER:
            a += handleIdentifier(expr, register);
            break;

            default:
            throw new CompileException("Could not compile this string expression");
        }

        return a;
    }    

    private String numExpressionAssembly(NumExpression expr, String register, boolean floatMode) throws CompileException
    {
        String a = "";

        // base case: single int term, simply put it into the register
        if (expr.getTerm() != null)
        {
            return numTermAssembly(expr.getTerm(), register, floatMode);
        }

        if (floatMode)
        {
            // evaluate left hand side, put into register
            a += numExpressionAssembly(expr.getLeft(), register, floatMode);

            // preserve xmm5 and register
            a += "\tsub esp, 4\n";
            a += "\tmovss [esp], xmm5\n";
            a += "\tsub esp, 4\n";
            a += "\tmovss [esp], " + register + "\n";

            // evaluate right hand side, put into register
            a += numExpressionAssembly(expr.getRight(), register, floatMode);

            // get the left hand side off the stack, and into xmm5
            a += "\tmovss xmm5, [esp]\n";
            a += "\tadd esp, 4\n";

            // perform operation with xmm5 and register
            if (expr.getOperator().getType() == TokenType.PLUS)
            {
                a += "\taddss xmm5, " + register + "\n";
            }
            else if (expr.getOperator().getType() == TokenType.MINUS)
            {
                a += "\tsubss xmm5, " + register + "\n";
            }

            // move result into register
            a += "\tmovss " + register + ", xmm5\n";

            // restore original xmm5
            a += "\tmovss xmm5, [esp]\n";
            a += "\tadd esp, 4\n";
        }
        else
        {
            // Evaluate left hand side, put into register
            a += numExpressionAssembly(expr.getLeft(), register, floatMode);

            // Preserve ecx and register
            a += "\tpush ecx\n";
            a += "\tpush " + register + "\n";

            // Evalute right hand side, put into register
            a += numExpressionAssembly(expr.getRight(), register, floatMode);

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
        }

        return a;
    }

    private String numTermAssembly(NumTerm term, String register, boolean floatMode) throws CompileException
    {
        String a = "";

        // base case: single int factor, simply move into register
        if (term.getFactor() != null)
        {
            return numFactorAssembly(term.getFactor(), register, floatMode);
        }

        if (floatMode)
        {
            // evaluate left hand side, put into register
            a += numTermAssembly(term.getLeft(), register, floatMode);

            // preserve xmm6 and register
            a += "\tsub esp, 4\n";
            a += "\tmovss [esp], xmm6\n";
            a += "\tsub esp, 4\n";
            a += "\tmovss [esp], " + register + "\n";

            // evaluate right hand side, put into register
            a += numTermAssembly(term.getRight(), register, floatMode);

            // get the left hand side off the stack, and into xmm6
            a += "\tmovss xmm6, [esp]\n";
            a += "\tadd esp, 4\n";

            // perform operation with xmm6 and register
            if (term.getOperator().getType() == TokenType.TIMES)
            {
                a += "\tmulss xmm6, " + register + "\n";
            }
            else if (term.getOperator().getType() == TokenType.DIVISION)
            {
                a += "\tdivss xmm6, " + register + "\n";
            }

            // move result into register
            a += "\tmovss " + register + ", xmm6\n";

            // restore original xmm6
            a += "\tmovss xmm6, [esp]\n";
            a += "\tadd esp, 4\n";
        }
        else
        {
            // Handle multiplication
            if (term.getOperator().getType() == TokenType.TIMES)
            {
                // Evaluate left hand side, put into register
                a += numTermAssembly(term.getLeft(), register, floatMode);

                // Preserve edx and register
                a += "\tpush edx\n";
                a += "\tpush " + register + "\n";

                // Evaluate right hand side, put into register
                a += numTermAssembly(term.getRight(), register, floatMode);

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
                    term.getOperator().getType() == TokenType.MOD) )
            {
                // Evaluate left hand side, put into register
                a += numTermAssembly(term.getLeft(), register, floatMode);

                // preserve eax, edx, and register
                a += "\tpush eax\n";
                a += "\tpush edx\n";
                a += "\tpush " + register + "\n";

                // evaluate right hand side, put into register
                a += numTermAssembly(term.getRight(), register, floatMode);

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
        }

        return a;
    }

    private String numFactorAssembly(NumFactor factor, String register, boolean floatMode) throws CompileException
    {
        String a = "";
        Token token = factor.getToken();
        NumExpression expr = factor.getExpression();

        if (token != null)
        {
            String valueToConvert;

            switch (token.getType())
            {
                // set the literal value with the appropriate syntax
                case LITERAL_INT:
                valueToConvert = token.getValue();

                if (floatMode)
                {
                    a += handleConversion(valueToConvert, register);
                }
                else
                {
                    a += "\tmov " + register + ", " + valueToConvert + "\n";
                }
                break;

                case LITERAL_CHAR:
                valueToConvert = "\'" + token.getValue() + "\'";

                if (floatMode)
                {
                    a += handleConversion(valueToConvert, register);
                }
                else
                {
                    a += "\tmov " + register + ", " + valueToConvert + "\n";
                }
                break;

                case LITERAL_FLOAT:
                if (floatMode)
                {
                    // Use macro to load float literal into register
                    a += "\tmovss " + register + ", [" + parser.getLitTable().getLiteralLabel(token.getValue()) + "]\n";
                    break;
                }

                throw new CompileException("Attempt to use a float literal in integer expression");

                case IDENTIFIER:
                Integer offset = parser.getSymbolTable().getStackOffset(token.getValue());
                String type = parser.getSymbolTable().getIdentifierType(token.getValue());

                if (offset == -1)
                {
                    throw new CompileException("Unknown identifier: '" + token.getValue() + "'");
                }

                if (floatMode)
                {
                    if ( type.equals(Tokenizer.TYPE_INT) || type.equals(Tokenizer.TYPE_CHAR) )
                    {
                        valueToConvert = "[ebp - " + offset.toString() + "]";
                        a += handleConversion(valueToConvert, register);
                    }
                    else if (type.equals(Tokenizer.TYPE_FLOAT))
                    {
                        a += "\tmovss " + register + ", [ebp - " + offset.toString() + "]\n";
                    }
                    else
                    {
                        throw new CompileException("Expected identifier for number, got one for type " + type);
                    }
                }
                else
                {
                    if ( !(type.equals(Tokenizer.TYPE_INT) || type.equals(Tokenizer.TYPE_CHAR)) )
                    {
                        throw new CompileException("Expected identifier for an integer or character, got one for type " + type);
                    }

                    a += "\tmov " + register + ", [ebp - " + offset.toString() + "]\n";
                }

                break;

                default:
                break;
            }
        }
        else
        {
            a += numExpressionAssembly(expr, register, floatMode);
        }

        if (factor.isNegative()) 
        {
            if (floatMode) 
            {
                a += "\tmovss xmm7, [" + FLOAT_NEG_MASK + "]\n";
                a += "\txorps " + register + ", xmm7\n";
            } 
            else 
            {
                a += "\tneg " + register + "\n";
            }
        }

        return a;
    }

    private String handleConversion(String value, String register)
    {
        String a = "";

        // We need to convert ints to floats

        // preserve eax first, in case it's being used
        a += "\tpush eax\n";

        // Perform the conversion
        a += "\tmov eax, " + value + "\n";
        a += "\tcvtsi2ss " + register + ", eax\n";

        // put eax back
        a += "\tpop eax\n";

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

    private String generateDataSegment() throws CompileException
    {
        String a = "";
        Iterator<String> literalIterator = parser.getLitTable().getLiteralIterator();
        int labelCount = 0;

        a += "section .data\n";

        while (literalIterator.hasNext())
        {
            String current = literalIterator.next();
            String label = "lit" + Integer.toString(labelCount);
            String type = parser.getLitTable().getLiteralType(current);
            
            switch (type)
            {
                case Tokenizer.TYPE_STRING:
                a += label + " db " + stringToAsmLiteral(current) + "\n";
                break;

                case Tokenizer.TYPE_FLOAT:
                a += label + " dd " + current + "\n";
                break;

                default:
                throw new CompileException("Unknown literal type: " + type);
            }

            parser.getLitTable().addLabelForLiteral(current, label);
            labelCount++;
        }

        a += PTR_DATA + " db " + parser.getSymbolTable().getAllDataSize() + " dup(0)\n";
        // a += BUFFER + " db " + BUFFER_SIZE + " dup(0)\n";
        a += FLOAT_NEG_MASK + " dd 0x80000000\n";

        return a;
    }

    private String stringToAsmLiteral(String inputString) {
        // Use StringBuilder for efficient string concatenation
        String asmLiteral = "";
        
        // Iterate through each character in the input string
        for (char c : inputString.toCharArray()) 
        {
            switch (c) 
            {
                case '\n':
                asmLiteral += "0x0A, ";
                break;

                case '\t':
                asmLiteral += "0x09, ";
                break;

                case '\\':
                asmLiteral += "0x5C, ";
                break;

                case '\"':
                asmLiteral += "0x22, ";
                break;

                case '\'':
                asmLiteral += "0x27, ";
                break;

                default:
                asmLiteral += "'" + c + "', ";
                break;
            }
        }
        
        // Append 0 at the end for null-termination
        asmLiteral += "0";
        
        return asmLiteral;
    }
}
