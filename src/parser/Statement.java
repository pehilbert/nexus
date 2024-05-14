package parser;

import codegen.AssemblyVisitor;
import codegen.CompileException;

public interface Statement 
{
    public void printStatement();
    public String accept(AssemblyVisitor visitor) throws CompileException;
}