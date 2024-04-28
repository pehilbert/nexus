package parser;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable {
    private Map<String, VarInfo> table;
    private int totalSize;

    public SymbolTable()
    {
        table = new HashMap<String, VarInfo>();
        totalSize = 0;
    }

    public boolean identifierExists(String identifier)
    {
        return table.containsKey(identifier);
    }

    public boolean addIdentifier(String type, String identifier, int size)
    {
        if (!table.containsKey(identifier))
        {
            VarInfo info = new VarInfo(type, size, size + totalSize);
            table.put(identifier, info);
            totalSize += size;
            return true;
        }

        return false;
    }

    public VarInfo getVarInfo(String identifier)
    {
        if (table.containsKey(identifier))
        {
            return table.get(identifier);
        }

        return null;
    }

    public int getTrueOffset(String identifier)
    {
        if (table.containsKey(identifier))
        {
            return table.get(identifier).getOffset();
        }

        return -1;
    }

    public String getIdentifierType(String identifier)
    {
        if (table.containsKey(identifier))
        {
            return table.get(identifier).getType();
        }

        return null;
    }

    public String toString()
    {
        return table.toString();
    }
}
