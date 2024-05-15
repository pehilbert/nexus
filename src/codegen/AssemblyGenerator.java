package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import tokenizer.*;
import parser.*;

public class AssemblyGenerator implements AssemblyVisitor {
    private Parser parser;
    private TableStack tableStack;
    private Map<String, Integer> typeSizes;

    static final String STR_SIZE_MD = "strSize";
    static final String PTR_DATA = "pd";
    static final String BUFFER = "buf";
    static final String FLOAT_NEG_MASK = "nmask";
    static final int BUFFER_SIZE = 1024;

    static final int INT_SIZE = 4;
    static final int FLOAT_SIZE = 4;
    static final int CHAR_SIZE = 1;
    static final int PTR_SIZE = 4;

    public AssemblyGenerator(Parser inParser)
    {
        parser = inParser;
        tableStack = new TableStack();
        typeSizes = new HashMap<String, Integer>();
        
        // fill out type size table
        typeSizes.put(Tokenizer.TYPE_INT, INT_SIZE);
        typeSizes.put(Tokenizer.TYPE_FLOAT, FLOAT_SIZE);
        typeSizes.put(Tokenizer.TYPE_CHAR, CHAR_SIZE);
        typeSizes.put(Tokenizer.TYPE_STRING, PTR_SIZE);
    }

    /*
     PROGRAM GENERATION FUNCTIONS 
    */
    public boolean generateProgram(String outputFile) throws CompileException
    {
        try (FileWriter writer = new FileWriter(outputFile)) 
        {   
            writer.write( generateDataSegment() );
            writer.write("\n");
            writer.write( generatePreamble() );
            writer.write( parser.getProgram().accept(this) );

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

    private String generatePreamble()
    {
        String a = "";

        a += "section .text\n";
        a += "global _start\n\n";
        a += "_start:\n";

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

        //a += PTR_DATA + " db " + parser.getSymbolTable().getAllDataSize() + " dup(0)\n";
        // a += BUFFER + " db " + BUFFER_SIZE + " dup(0)\n";
        a += FLOAT_NEG_MASK + " dd 0x80000000\n";

        return a;
    }

    /*
     STATEMENT VISITOR FUNCTIONS
    */
    public String visit(Scope stmt) throws CompileException
    {
        SymbolTable scopeTable = new SymbolTable();

        tableStack.push(scopeTable);
        
        Iterator<Statement> i = stmt.getIterator();
        String a = "";

        // save the old base pointer
        a += "\tpush ebp\n";

        // move the base pointer up to the current stack pointer
        a += "\tmov ebp, esp\n";

        while (i.hasNext())
        {
            a += i.next().accept(this);
        }

        tableStack.pop();

        // move the stack pointer back down to the base, where the old
        // base pointer is stored
        a += "\tmov esp, ebp\n";

        // pop the old base pointer off the stack
        a += "\tpop ebp\n";

        return a;
    }

    public String visit(FunctionDeclaration stmt) throws CompileException
    {
        return "";
    }

    public String visit(FunctionCall stmt) throws CompileException
    {
        return "";
    }

    public String visit(ReturnStatement stmt) throws CompileException
    {
        return "";
    }

    public String visit(Declaration stmt) throws CompileException
    {
        String a = "";
        String type = stmt.getType().getValue();
        String identifier = stmt.getIdentifier().getValue();
        Expression expr = stmt.getExpression();
        String register = expr.asmRegister();
        Integer dataSize = typeSizes.get(type);

        if (dataSize == null)
        {
            throw new CompileException("Unknown size of type " + stmt.getType().getValue());
        }

        // evaluate the expression, and hold the result in the asmRegister
        a += expr.accept(this);

        // push result onto the stack
        a += "\tsub esp, " + dataSize.toString() + "\n";
        
        // make sure to use the correct move instruction for the register
        if (register.startsWith("xmm"))
        {
            a += "\tmovss ";
        }
        else
        {
            a += "\tmov ";
        }

        a += "[esp], " + register + "\n";

        // add to symbol table
        if (!tableStack.identifierInUse(identifier))
        {
            tableStack.peek().addIdentifier(type, identifier, dataSize);
        }
        else
        {
            throw new CompileException("Identifier " + identifier + " already in use");
        }

        if (type.equals(Tokenizer.TYPE_STRING))
        {
            updateStringLength(identifier, (StringExpression)stmt.getExpression());
        }

        return a;
    }

    public String visit(Reassignment stmt) throws CompileException
    {
        String a = "";
        Expression expr = stmt.getExpression();
        String identifier = stmt.getIdentifier().getValue();
        VarInfo info = tableStack.getVarInfo(identifier);
        String type;
        Integer offset;
        String register = expr.asmRegister();

        if (info != null)
        {
            type = info.getType();
            offset = info.getOffset();

            // evaluate the expression and hold the result in the asmRegister of expression
            a += expr.accept(this);

            // make sure to use the correct move instruction for the register
            if (register.startsWith("xmm"))
            {
                a += "\tmovss ";
            }
            else
            {
                a += "\tmov ";
            }

            // update the memory location in the stack
            a += "[ebp + " + offset.toString() + "], " + register + "\n";

            if (type.equals(Tokenizer.TYPE_STRING))
            {
                updateStringLength(identifier, (StringExpression)stmt.getExpression());
            }
            
            return a;
        }

        return a;
    }

    public String visit(PrintStatement stmt) throws CompileException 
    {
        StringExpression expr = stmt.getExpression();
        Token exprToken = expr.getToken();
        String dataLen;
        VarInfo info;

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
            info = tableStack.getVarInfo(exprToken.getValue());

            if (info == null)
            {
                throw new CompileException("Unknown identifier: " + exprToken.getValue());
            }

            dataLen = info.getMetaData(STR_SIZE_MD);

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
        String a = "";

        a += "\tmov eax, 1\n";
        a += visit(stmt.getExpression(), "ebx", false);
        a += "\tint 0x80\n\n";

        return a;
    }

    /*
     EXPRESSION VISITOR FUNCTIONS 
    */
    
    // Handles a string expression, puts the memory location of the resulting string into the given register
    public String visit(StringExpression expr, String register) throws CompileException 
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
    
    public String visit(CharExpression expr, String register) throws CompileException
    {
        String a = "";
        NumExpression numExpr = expr.getExpression();

        // evaluate expression in 32-bit register, and then transfer the lower 8 bits to 8-bit register
        a += visit(numExpr, "eax", false);
        a += "\tmov " + register + ", al\n";

        return a;
    }

    public String visit(NumExpression expr, String register, boolean floatMode) throws CompileException
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
            a += visit(expr.getLeft(), register, floatMode);

            // preserve xmm5 and register
            a += "\tsub esp, 4\n";
            a += "\tmovss [esp], xmm5\n";
            a += "\tsub esp, 4\n";
            a += "\tmovss [esp], " + register + "\n";

            // evaluate right hand side, put into register
            a += visit(expr.getRight(), register, floatMode);

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
            a += visit(expr.getLeft(), register, floatMode);

            // Preserve ecx and register
            a += "\tpush ecx\n";
            a += "\tpush " + register + "\n";

            // Evalute right hand side, put into register
            a += visit(expr.getRight(), register, floatMode);

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

    /*
     NUMBER EXPRESSION HELPER FUNCTIONS 
    */
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
                Integer offset = tableStack.getOffset(token.getValue());
                String type = tableStack.getVarInfo(token.getValue()).getType();

                if (offset == -1)
                {
                    throw new CompileException("Unknown identifier: '" + token.getValue() + "'");
                }

                if (floatMode)
                {
                    if ( type.equals(Tokenizer.TYPE_INT) || type.equals(Tokenizer.TYPE_CHAR) )
                    {
                        valueToConvert = "[ebp + " + offset.toString() + "]";
                        a += handleConversion(valueToConvert, register);
                    }
                    else if (type.equals(Tokenizer.TYPE_FLOAT))
                    {
                        a += "\tmovss " + register + ", [ebp + " + offset.toString() + "]\n";
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

                    a += "\tmov " + register + ", [ebp + " + offset.toString() + "]\n";
                }

                break;

                default:
                break;
            }
        }
        else
        {
            a += visit(expr, register, floatMode);
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

    /*
     STRING EXPRESSION HELPER FUNCTIONS
    */
    private void updateStringLength(String identifier, StringExpression expr) throws CompileException
    {
        VarInfo info = tableStack.getVarInfo(identifier);

        if (info == null)
        {
            throw new CompileException("Unknown identifier: " + identifier);
        }

        switch (expr.getToken().getType())
        {
            case LITERAL_STR:
            info.updateMetaData(STR_SIZE_MD, Integer.toString(expr.getToken().getValue().length()));
            break;

            case IDENTIFIER:
            info.updateMetaData(STR_SIZE_MD, tableStack.getVarInfo(identifier).getMetaData(STR_SIZE_MD));
            break;

            default:
            throw new CompileException("Could not update string length of " + identifier);
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
        Integer offset = tableStack.getOffset(expr.getToken().getValue());

        if (offset != -1)
        {
            return "\tmov " + register + ", [ebp + " + offset.toString() + "]\n";
        }

        throw new CompileException("Unknown identifier: " + expr.getToken().getValue());
    }

    private String stringToAsmLiteral(String inputString) {
        String asmLiteral = "";
        
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
