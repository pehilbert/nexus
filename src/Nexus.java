import java.nio.file.Files;
import java.nio.file.Paths;

import tokenizer.TokenException;
import tokenizer.Tokenizer;

import parser.Parser;

import java.io.IOException;

public class Nexus
{
    public static void main( String[] args )
    {
        if (args.length != 2)
        {
            System.out.println("Usage: nexc <input filename> <target filename>");
            System.exit(1);
        }

        Tokenizer tokenizer;
        Parser parser;

        try 
        {
            byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
            String content = new String(bytes);
            
            tokenizer = new Tokenizer(content);

            try
            {
                tokenizer.tokenize();
                tokenizer.printTokenList();

                parser = new Parser(tokenizer.getTokens());   
                parser.parseProgram();
                parser.printStatements();
            }
            catch (TokenException exception)
            {
                exception.printStackTrace();
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        System.exit(0);
    }
}