package codegen;

import parser.FunctionDeclaration;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class FunctionTableStack 
{
    private List<Map<String, FunctionDeclaration>> stack;

    public FunctionTableStack()
    {
        stack = new ArrayList<Map<String, FunctionDeclaration>>();
    }

    public void pushEmptyTable()
    {
        stack.add(new HashMap<String, FunctionDeclaration>());
    }

    public Map<String, FunctionDeclaration> peek()
    {
        return stack.get(stack.size() - 1);
    }

    public Map<String, FunctionDeclaration> pop()
    {
        return stack.remove(stack.size() - 1);
    }

    public FunctionDeclaration getFunctionDeclaration(String identifier)
    {
        for (int i = stack.size() - 2; i >= 0; i++)
        {
            if (stack.get(i).containsKey(identifier))
            {
                return stack.get(i).get(identifier);
            }
        }

        return null;
    }
}
