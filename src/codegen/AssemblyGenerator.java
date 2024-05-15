package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import tokenizer.*;
import parser.*;

public class AssemblyGenerator implements AssemblyVisitor {
    private Parser parser;
    private TableStack tableStack;
    private FunctionTableStack fTableStack;
    private Map<String, Integer> typeSizes;
    private Integer funcLabelCount;

    static final String MAIN_FUNC_NAME = "main";
    
    static final String STR_SIZE_MD = "strSize";
    static final String PTR_DATA = "pd";
    static final String BUFFER = "buf";
    static final String FLOAT_NEG_MASK = "nmask";
    static final int BUFFER_SIZE = 1024;

    public static final String INT_EXPR_REGISTER = "eax";
    public static final String FLOAT_EXPR_REGISTER = "xmm0";
    public static final String CHAR_EXPR_REGISTER = "al";
    public static final String STR_EXPR_REGISTER = "eax";

    static final int INT_SIZE = 4;
    static final int FLOAT_SIZE = 4;
    static final int CHAR_SIZE = 1;
    static final int PTR_SIZE = 4;

    public AssemblyGenerator(Parser inParser)
    {
        parser = inParser;
        tableStack = new TableStack();
        fTableStack = new FunctionTableStack();
        typeSizes = new HashMap<String, Integer>();
        funcLabelCount = 0;
        
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
                a += label + ":\n";
                a += " dd " + current.length() + "\n";
                a += " db " + stringToAsmLiteral(current) + "\n";
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
        fTableStack.pushEmptyTable();
        
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
        fTableStack.pop();

        // move the stack pointer back down to the base, where the old
        // base pointer is stored
        a += "\tmov esp, ebp\n";

        // pop the old base pointer off the stack
        a += "\tpop ebp\n";

        // if scope is for a function, add a safety return command
        if (stmt.getFunctionName() != null)
        {
            a += "\tret\n";
        }

        return a;
    }

    public String visit(FunctionDeclaration stmt) throws CompileException
    {
        String funcName = stmt.getName();
        FunctionDeclaration function = fTableStack.getFunctionDeclaration(funcName);

        // check if this function does not yet have an entry in function table
        if (function == null)
        {
            // if so, create one, including a label
            if (stmt.getName().equals(MAIN_FUNC_NAME))
            {
                stmt.setLabel("_start");
            }
            else
            {
                String newLabel = "func" + funcLabelCount.toString();
                stmt.setLabel(newLabel);
                funcLabelCount++;
            }

            fTableStack.peek().put(funcName, stmt);
        }

        // if there is a body for this declaration, generate the code for it
        if (stmt.getScope() != null)
        {
            // make sure there isn't already a body for this function
            if (function != null && function.getScope() != null)
            {
                throw new CompileException("Attempt to redefine function '" + funcName + "'");
            }

            // update our version of the function
            function = fTableStack.getFunctionDeclaration(funcName);

            // add our scope to it and put it back
            function.setScope(stmt.getScope());

            // make sure there is a label
            if (function.getLabel() == null)
            {
                throw new CompileException("Could not find label for function '" + funcName + "'");
            }

            // push parameter symbol table
            tableStack.push(getParamSymbolTable(stmt));

            String a = "";

            // put label
            a += function.getLabel() + ":\n";

            // generate code for scope
            a += function.getScope().accept(this);

            // pop parameter symbol table
            tableStack.pop();

            return a;
        }

        return "";
    }

    public String visit(FunctionCall stmt) throws CompileException
    {
        // look up function, ensure it exists, get the label for it
        FunctionDeclaration function = fTableStack.getFunctionDeclaration(stmt.getName());
        Integer cleanupOffset = getParamSymbolTable(function).getStackSize() - PTR_SIZE;
        List<Parameter> params;
        List<Expression> args = stmt.getArgs();

        String a = "";

        if (function == null)
        {
            throw new CompileException("Function '" + stmt.getName() + "' not defined");
        }

        params = function.getParams();

        // check for argument count mismatch
        if (params.size() != args.size()) 
        {
            throw new CompileException("Argument count mismatch for function '" + stmt.getName() + "'");
        }

        // push arguments onto the stack in reverse order
        for (int i = args.size() - 1; i >= 0; i--)
        {
            a += pushExpression(args.get(i), params.get(i).getType());
        }

        // call function by its label
        if (function.getLabel() == null)
        {
            throw new CompileException("Label not found for function " + function.getName());
        }

        a += "\tcall " + function.getLabel() + "\n";

        // clean up stack
        a += "\tadd esp, " + cleanupOffset.toString() + "\n";

        return a;
    }

    public String visit(ReturnStatement stmt) throws CompileException
    {
        String a = "";

        // put the value of the expression in its default register
        a += stmt.getExpression().accept(this);

        // restore stack
        a += "\tmov esp, ebp\n";
        a += "\tpop ebp\n";

        // return
        a += "\tret\n";

        return a;
    }

    public String visit(Declaration stmt) throws CompileException
    {
        String a = "";
        String type = stmt.getType().getValue();
        String identifier = stmt.getIdentifier().getValue();
        Expression expr = stmt.getExpression();
        Integer dataSize = typeSizes.get(type);

        if (dataSize == null)
        {
            throw new CompileException("Unknown size of type " + stmt.getType().getValue());
        }

        a += pushExpression(expr, type);

        // add to symbol table
        if (!tableStack.identifierInUse(identifier))
        {
            tableStack.peek().addIdentifier(type, identifier, dataSize);
        }
        else
        {
            throw new CompileException("Identifier " + identifier + " already in use");
        }

        return a;
    }

    public String visit(Reassignment stmt) throws CompileException
    {
        String a = "";
        Expression expr = stmt.getExpression();
        String identifier = stmt.getIdentifier().getValue();
        Integer offset = tableStack.getOffset(identifier);
        String register = expr.asmRegister();

        if (offset != -1)
        {
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
            
            return a;
        }
        else
        {
            throw new CompileException("Unknown identifier: " + identifier);
        }
    }

    public String visit(PrintStatement stmt) throws CompileException 
    {
        StringExpression expr = stmt.getExpression();

        String a = "";

        // get the string and its length
        a += visit(expr, "ecx");
        a += "\tmov edx, [ecx]\n";
        a += "\tadd ecx, 4\n";

        // load syscall number and 1 for stdout
        a += "\tmov eax, 4\n";
        a += "\tmov ebx, 1\n";

        // perform syscall
        a += "\tint 0x80\n";

        return a;
    }

    public String visit(ExitStatement stmt) throws CompileException 
    {
        String a = "";

        a += visit(stmt.getExpression(), "ebx", false);
        a += "\tmov eax, 1\n";
        a += "\tint 0x80\n";

        return a;
    }

    /*
     EXPRESSION VISITOR FUNCTIONS 
    */
    
    // Handles a string expression, puts the memory location of the resulting string into the given register
    public String visit(StringExpression expr, String register) throws CompileException 
    {
        String a = "";
        if (expr.getFunctionCall() != null)
        {
            a += visit(expr.getFunctionCall());
            a += "\tmov " + register + ", " + STR_EXPR_REGISTER + "\n";
        }
        else
        {
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
     GENERAL HELPER FUNCTIONS 
    */
    private String pushExpression(Expression expr, String type) throws CompileException
    {
        String a = "";
        String register = expr.asmRegister();
        Integer dataSize = typeSizes.get(type);

        if (dataSize == null)
        {
            throw new CompileException("Unknown size of type " + type);
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

        return a;
    }

    /*
     FUNCTION HELPER FUNCTIONS 
    */
    private SymbolTable getParamSymbolTable(FunctionDeclaration function) throws CompileException
    {
        SymbolTable newTable = new SymbolTable();
        List<Parameter> params = function.getParams();

        for (int i = params.size() - 1; i >= 0; i--)
        {
            Parameter current = params.get(i);
            Integer dataSize = typeSizes.get(current.getType());

            if (dataSize == null)
            {
                throw new CompileException("Unknown size of type " + current.getType());
            }

            newTable.addIdentifier(current.getType(), current.getIdentifier(), dataSize);
        }

        // add placeholder for return address
        newTable.addIdentifier("-", "-", PTR_SIZE);

        return newTable;
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
        FunctionCall functionCall = factor.getFunctionCall();

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
        else if (expr != null)
        {
            a += visit(expr, register, floatMode);
        }
        else
        {
            a += visit(functionCall);

            switch (functionCall.getReturnType())
            {
                case Tokenizer.TYPE_INT:
                a += "\tmov " + register + ", " + INT_EXPR_REGISTER + "\n";
                break;

                case Tokenizer.TYPE_FLOAT:
                if (floatMode)
                {
                    a += "\tmovss " + register + ", " + FLOAT_EXPR_REGISTER + "\n";
                    break;
                }
                
                throw new CompileException("Float value cannot be used for int number expression");

                case Tokenizer.TYPE_CHAR:
                a += "\tmov " + register + ", " + CHAR_EXPR_REGISTER + "\n";
            }
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
