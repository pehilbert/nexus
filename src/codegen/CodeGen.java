package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import parser.Parser;

public class CodeGen {
    Parser parser;

    public CodeGen(Parser inParser)
    {
        parser = inParser;
    }

    public void compile(String executableFile)
    {
        String sourceFile = executableFile + ".asm";
        String objectFile = executableFile + ".o";

        try (FileWriter writer = new FileWriter(sourceFile)) 
        {
            // write pre-code stuff
            writer.write("section .text\n");
            writer.write("global _start\n\n");
            writer.write("_start:\n");

            // write assembly code
            int i = 0;

            while (i < parser.getProgram().size())
            {
                writer.write( "" );
                i++;
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        System.out.println("Successfully wrote assembly code.");

        // Use ProcessBuilder to run NASM and ld
        ProcessBuilder assembler = new ProcessBuilder("nasm", "-f", "elf32", sourceFile, "-o", objectFile);
        ProcessBuilder linker = new ProcessBuilder("ld", "-m", "elf_i386", "-o", executableFile, objectFile);

        try 
        {
            // Start and execute the assembler process
            System.out.println("Assembling the file...");
            Process asmProcess = assembler.start();
            if (asmProcess.waitFor() == 0) 
            {
                System.out.println("Assembly successful. Linking...");
                
                // Start and execute the linker process
                Process linkProcess = linker.start();
                if (linkProcess.waitFor() == 0) 
                {
                    System.out.println("Linking successful.");
                    System.out.println("Executable created: " + executableFile);
                } 
                else 
                {
                    // Output error stream if linking fails
                    printProcessErrors(linkProcess);
                }
            } 
            else 
            {
                // Output error stream if assembly fails
                printProcessErrors(asmProcess);
            }
        } 
        catch (IOException | InterruptedException e) 
        {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    private void printProcessErrors(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            System.out.println("Error:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
