package codegen;

import java.util.Stack;
import java.util.Iterator;

public class TableStack
{
    Stack<SymbolTable> stack;

    public TableStack()
    {
        stack = new Stack<SymbolTable>();
    }

    public SymbolTable pop()
    {
        return stack.pop();
    }

    public SymbolTable peek()
    {
        return stack.peek();
    }

    public void push(SymbolTable newTable)
    {
        stack.push(newTable);
    }

    // gets the offset from current base pointer of a certain variable, or -1 if not found
    public int getOffset(String identifier)
    {
        Iterator<SymbolTable> i = stack.iterator();
        SymbolTable current;
        int totalSize = 0;

        while (i.hasNext())
        {
            current = i.next();

            if (current.identifierExists(identifier))
            {
                return totalSize - current.getStackOffset(identifier);
            }

            totalSize += current.getStackSize();
        }

        return -1;
    }

    public boolean identifierInUse(String identifier)
    {
        Iterator<SymbolTable> i = stack.iterator();
        SymbolTable current;

        while (i.hasNext())
        {
            current = i.next();

            if (current.identifierExists(identifier))
            {
                return true;
            }
        }

        return false;
    }

    public VarInfo getVarInfo(String identifier)
    {
        Iterator<SymbolTable> i = stack.iterator();
        SymbolTable current;

        while (i.hasNext())
        {
            current = i.next();

            if (current.identifierExists(identifier))
            {
                return current.getVarInfo(identifier);
            }
        }

        return null;
    }
}