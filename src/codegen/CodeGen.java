package codegen;

import java.io.FileWriter;
import java.io.IOException;

import parser.Parser;

public class CodeGen {
    Parser parser;

    public CodeGen(Parser inParser)
    {
        parser = inParser;
    }

    public void compile(String outputFilename)
    {
        try (FileWriter writer = new FileWriter(outputFilename + ".asm")) 
        {
            int i = 0;

            while (i < parser.getProgram().size())
            {
                writer.write( parser.getProgram().get(i).getAssembly() );
                i++;
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        
    }
}
