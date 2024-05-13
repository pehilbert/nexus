package parser;

public class Parameter 
{
    private String type;
    private String identifier;

    public Parameter(String inType, String inIdentifier)
    {
        type = inType;
        identifier = inIdentifier;
    }

    public String getType()
    {
        return type;
    }

    public String getIdentifier()
    {
        return identifier;
    }
}
