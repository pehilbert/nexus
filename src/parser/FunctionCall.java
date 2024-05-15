package parser;

import java.util.List;
import java.util.ArrayList;

import codegen.AssemblyVisitor;
import codegen.CompileException;

public class FunctionCall implements Statement {
    private String name;
    private List<Expression> args;

    public FunctionCall(String inName)
    {
        name = inName;
        args = new ArrayList<Expression>();
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

    public void printStatement()
    {
        System.out.println("Function call: " + name);
        System.out.println("Arguments:");

        if (args.size() == 0)
        {
            System.out.println("none");
        }
        else
        {
            for (int i = 0; i < args.size(); i++)
            {
                System.out.println(args.get(i).toString());
            }
        }
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this);
    }
}
