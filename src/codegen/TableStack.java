package codegen;

import java.util.List;
import java.util.ArrayList;

public class TableStack
{
    List<SymbolTable> stack;

    public TableStack()
    {
        stack = new ArrayList<SymbolTable>();
    }

    public SymbolTable pop()
    {
        return stack.remove(stack.size() - 1);
    }

    public SymbolTable peek()
    {
        return stack.get(stack.size() - 1);
    }

    public void push(SymbolTable newTable)
    {
        stack.add(newTable);
    }

    // gets the offset from current base pointer of a certain variable, or -1 if not found
    public int getOffset(String identifier)
    {
        SymbolTable current;
        int baseOffset = 0;

        for (int i = stack.size() - 1; i >= 0; i--)
        {
            current = stack.get(i);

            if (current.identifierExists(identifier))
            {
                // go back up from the stack frame we're currently looking at
                return baseOffset - current.getStackOffset(identifier);
            }

            if (i - 1 >= 0)
            {
                // essentially, go down to the next stack frame on the stack, accounting for the extra preserved base pointer
                baseOffset += stack.get(i - 1).getStackSize() + AssemblyGenerator.PTR_SIZE;
            }
        }

        return -1;
    }

    public boolean identifierInUse(String identifier)
    {
        SymbolTable current;

        for (int i = stack.size() - 1; i >= 0; i--)
        {
            current = stack.get(i);

            if (current.identifierExists(identifier))
            {
                return true;
            }
        }

        return false;
    }

    public VarInfo getVarInfo(String identifier)
    {
        SymbolTable current;

        for (int i = stack.size() - 1; i >= 0; i--)
        {
            current = stack.get(i);

            if (current.identifierExists(identifier))
            {
                return current.getVarInfo(identifier);
            }
        }

        return null;
    }
}