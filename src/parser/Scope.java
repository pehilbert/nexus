package parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import codegen.CompileException;
import codegen.StatementVisitor;

public class Scope implements Statement {
    private List<Statement> statements;

    public Scope()
    {
        statements = new ArrayList<Statement>();
    }

    public void addStatement(Statement newStatement)
    {
        statements.add(newStatement);
    }

    public Iterator<Statement> getIterator()
    {
        return statements.iterator();
    }

    public void printStatement()
    {
        Iterator<Statement> i = getIterator();

        System.out.println("Scope: {");

        while (i.hasNext())
        {
            i.next().printStatement();
        }

        System.out.println("}");
    }

    public String accept(StatementVisitor visitor) throws CompileException
    {
        return visitor.visit(this);
    }
}
