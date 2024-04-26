package parser;

import codegen.StatementVisitor;
import codegen.CompileException;

public interface Statement 
{
    public void printStatement();
    public String accept(StatementVisitor visitor) throws CompileException;
}