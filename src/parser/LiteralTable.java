package parser;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class LiteralTable {
    private static String TYPE_KEY = "type";
    private static String LABEL_KEY = "label"; 
    private Map<String, Map<String, String>> table;

    public LiteralTable()
    {
        table = new HashMap<String, Map<String, String>>();
    }

    public boolean literalExists(String literal)
    {
        return table.containsKey(literal);
    }

    public void addLiteral(String literal)
    {
        if (!literalExists(literal))
        {
            table.put(literal, new HashMap<String, String>());
        }
    }

    public void addLiteralWithType(String literal, String type)
    {
        if (!literalExists(literal))
        {
            addLiteral(literal);
            table.get(literal).put(TYPE_KEY, type);
        }
    }

    public boolean addLabelForLiteral(String literal, String label)
    {
        if (literalExists(literal))
        {
            table.get(literal).put(LABEL_KEY, label);
            return true;
        }

        return false;
    }

    public Map<String, String> getLiteralInfo(String literal)
    {
        return table.get(literal);
    }

    public String getLiteralType(String literal)
    {
        Map<String, String> info = getLiteralInfo(literal);

        if (info != null)
        {
            return info.get(TYPE_KEY);
        }

        return null;
    }

    public String getLiteralLabel(String literal)
    {
        Map<String, String> info = getLiteralInfo(literal);

        if (info != null)
        {
            return info.get(LABEL_KEY);
        }

        return null;
    }

    public Iterator<String> getLiteralIterator()
    {
        return table.keySet().iterator();
    }
}
