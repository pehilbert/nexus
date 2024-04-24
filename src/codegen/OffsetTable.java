package codegen;

import java.util.LinkedHashMap;

public class OffsetTable {
    private LinkedHashMap<String, Integer> table;
    private int size;

    public OffsetTable(int inSize)
    {
        table = new LinkedHashMap<String, Integer>();
        size = inSize;
    }

    public boolean addIdentifier(String identifier)
    {
        if (!table.containsKey(identifier))
        {
            table.put(identifier, size * table.size());
            return true;
        }

        return false;
    }

    public int getOffset(String identifier)
    {
        if (table.containsKey(identifier))
        {
            return table.get(identifier) + size;
        }

        return -1;
    }

    public String toString()
    {
        return table.toString();
    }
}
