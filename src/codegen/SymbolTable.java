package codegen;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable {
    private Map<String, VarInfo> table;
    private int stackSize;
    private int allDataSize;

    public SymbolTable()
    {
        table = new HashMap<String, VarInfo>();
        stackSize = 0;
        allDataSize = 0;
    }

    public boolean identifierExists(String identifier)
    {
        return table.containsKey(identifier);
    }

    public boolean addIdentifier(String type, String identifier)
    {
        if (!identifierExists(identifier))
        {
            VarInfo info = new VarInfo(type);
            table.put(identifier, info);
            return true;
        }

        return false;
    }

    public boolean addIdentifier(String type, String identifier, int size)
    {
        if (!identifierExists(identifier))
        {
            VarInfo info = new VarInfo(type, size, size + stackSize);
            table.put(identifier, info);
            stackSize += size;
            return true;
        }

        return false;
    }

    public boolean makePointer(String identifier, String primitive, int unitSize, int totalSize)
    {
        if (identifierExists(identifier))
        {
            VarInfo info = table.get(identifier);
            info.addPointerInfo(primitive, allDataSize, unitSize, totalSize);
            allDataSize += totalSize;
            return true;
        }

        return false;
    }

    public int getStackSize()
    {
        return stackSize;
    }

    public int getAllDataSize()
    {
        return allDataSize;
    }

    public VarInfo getVarInfo(String identifier)
    {
        if (identifierExists(identifier))
        {
            return table.get(identifier);
        }

        return null;
    }

    public int getStackOffset(String identifier)
    {
        if (identifierExists(identifier))
        {
            return table.get(identifier).getOffset();
        }

        return -1;
    }

    public int getDataOffset(String identifier)
    {
        if (identifierExists(identifier))
        {
            return table.get(identifier).getDataOffset();
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
        String s = "";
        s += "Total stack size: " + stackSize + "\n";
        s += "Total data size: " + allDataSize + "\n";
        s += table.toString();
        return s;
    }
}
