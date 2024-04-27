package parser;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable {
    private Map<String, VarInfo> table;
    private int wordSize;

    public SymbolTable(int inSize)
    {
        table = new HashMap<String, VarInfo>();
        wordSize = inSize;
    }

    public boolean addIdentifier(String type, String identifier)
    {
        if (!table.containsKey(identifier))
        {
            VarInfo info = new VarInfo(type, wordSize * table.size());
            table.put(identifier, info);
            return true;
        }

        return false;
    }

    public int getTrueOffset(String identifier)
    {
        if (table.containsKey(identifier))
        {
            return table.get(identifier).getOffset() + wordSize;
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
