package parser;

import java.util.List;

import codegen.CompileException;
import codegen.AssemblyVisitor;

import java.util.ArrayList;

public class FunctionDeclaration implements Statement
{
    private String name;
    private List<Parameter> params;
    private String returnType;
    private Scope funcScope;
    private String label;

    public FunctionDeclaration(String inName, String inType)
    {
        name = inName;
        returnType = inType;
        params = new ArrayList<Parameter>();
        funcScope = null;
        label = null;
    }

    public String getName()
    {
        return name;
    }

    public List<Parameter> getParams()
    {
        return params;
    }

    public String getReturnType()
    {
        return returnType;
    }

    public Scope getScope()
    {
        return funcScope;
    }

    public String getLabel()
    {
        return label;
    }

    public void addParam(Parameter param)
    {
        params.add(param);
    }

    public void setScope(Scope inScope)
    {
        funcScope = inScope;
    }

    public void setLabel(String newLabel)
    {
        label = newLabel;
    }

    public boolean equals(FunctionDeclaration other)
    {
        if (!returnType.equals(other.getReturnType()))
        {
            return false;
        }

        if (!name.equals(other.getName()))
        {
            return false;
        }

        if (params.size() != other.getParams().size())
        {
            return false;
        }

        for (int i = 0; i < params.size(); i++)
        {
            if (!params.get(i).equals(other.getParams().get(i)))
            {
                return false;
            }
        }

        return true;
    }

    public void printStatement()
    {
        System.out.println("Function declaration: " + returnType + " " + name);
        System.out.print("Parameters: ");
        
        if (params.size() == 0)
        {
            System.out.println("none");
        }
        else
        {
            for (int i = 0; i < params.size(); i++)
            {
                Parameter current = params.get(i);
                System.out.print(current.getType() + " " + current.getIdentifier() + " | ");
            }

            System.out.println();
        }

        if (funcScope != null)
        {
            funcScope.printStatement();
        }
        else
        {
            System.out.println("PROTOTYPE");
        }
    }

    public String accept(AssemblyVisitor visitor) throws CompileException
    {
        return visitor.visit(this);
    }
}