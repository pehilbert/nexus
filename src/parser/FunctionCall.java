package parser;

import java.util.List;
import java.util.ArrayList;

import codegen.AssemblyVisitor;
import codegen.CompileException;

public class FunctionCall implements Statement {
    private String returnType;
    private String name;
    private List<Expression> args;

    public FunctionCall(String inName, String inReturnType)
    {
        returnType = inReturnType;
        name = inName;
        args = new ArrayList<Expression>();
    }

    public String getReturnType()
    {
        return returnType;
    }

    public String getName()
    {
        return name;
    }

    public List<Expression> getArgs()
    {
        return args;
    }

    public void addArgument(Expression arg)
    {
        args.add(arg);
    }

    public String toString()
    {
        String s = "";

        s += "Function call: " + name + "\n";
        s += "Arguments:\n";

        if (args.size() == 0)
        {
            s += "none\n";
        }
        else
        {
            for (int i = 0; i < args.size(); i++)
            {
                s += args.get(i).toString() + "\n";
            }
        }

        return s;
    }

    public void printStatement()
    {
        System.out.print(this.toString());
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this);
    }
}
