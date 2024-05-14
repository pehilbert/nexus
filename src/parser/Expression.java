package parser;

import codegen.AssemblyVisitor;
import codegen.CompileException;

public interface Expression 
{
    public String asmRegister();
    public String accept(AssemblyVisitor visitor) throws CompileException;
    public String toString();
}
