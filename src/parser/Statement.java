package parser;

import codegen.StatementVisitor;

public interface Statement 
{
    public void printStatement();
    public String accept(StatementVisitor visitor);
}