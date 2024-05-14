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

    public boolean equals(Parameter other)
    {
        return type.equals(other.getType()) && identifier.equals(other.getIdentifier());
    }
}
