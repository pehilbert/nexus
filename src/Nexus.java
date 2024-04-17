public class Nexus
{
    public static void main( String[] args )
    {
        if (args.length != 2)
        {
            System.out.println("Usage: nexc <input filename> <target filename>");
            System.exit(1);
        }

        System.out.println(args[0] + " compiled to " + args[1]);
        System.exit(0);
    }
}