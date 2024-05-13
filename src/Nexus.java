import java.nio.file.Files;
import java.nio.file.Paths;
import tokenizer.TokenException;
import tokenizer.Tokenizer;

import parser.Parser;
import parser.ParseException;
import codegen.CompileException;
import codegen.Compiler;

import java.io.IOException;

public class Nexus
{
    public static void main( String[] args )
    {
        String inFilename;
        String outFilename;
        boolean verbose = false;

        if (args.length < 2)
        {
            System.out.println("Usage: nexc <options> <input filename> <target filename>");
            System.exit(1);
        }

        if (args.length == 2)
        {
            inFilename = args[0];
            outFilename = args[1];
        }

        int i = 0;

        while (args[i].charAt(0) == '-')
        {
            switch (args[i].charAt(1))
            {
                case 'v': verbose = true;
                default: break;
            }

            i++;
        }

        if (i + 1 >= args.length)
        {
            System.out.println("Usage: nexc <options> <input filename> <target filename>");
            System.exit(1);
        }

        inFilename = args[i];
        outFilename = args[i + 1];

        Tokenizer tokenizer;
        Parser parser;
        Compiler codeGenerator;

        try 
        {
            byte[] bytes = Files.readAllBytes(Paths.get(inFilename));
            String content = new String(bytes);
            
            tokenizer = new Tokenizer(content);

            try
            {
                tokenizer.tokenize();

                if (verbose)
                {
                    System.out.println("Program was successfuly tokenized.");
                    System.out.println("Tokens in program: ");
                    tokenizer.printTokenList();
                    System.out.println();
                }
                
                try
                {
                    parser = new Parser(tokenizer.getTokens());
                    parser.parseProgram();

                    if (verbose)
                    {
                        System.out.println("Program was successfully parsed.");
                        System.out.println("Parsed program: ");
                        parser.printProgram();
                        System.out.println();
                    }

                    try
                    {
                        codeGenerator = new Compiler(parser);
                        codeGenerator.compile(outFilename, verbose);
                    }
                    catch (CompileException exception)
                    {
                        exception.printStackTrace();
                        System.exit(1);
                    }
                }
                catch (ParseException exception)
                {
                    exception.printStackTrace();
                    System.exit(1);
                }
            }
            catch (TokenException exception)
            {
                exception.printStackTrace();
                System.exit(1);
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}