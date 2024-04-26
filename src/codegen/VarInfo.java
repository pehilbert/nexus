package codegen;

public class VarInfo {
    private int offset;
    private String type;

    public VarInfo(String inType, int inOffset)
    {
        type = inType;
        offset = inOffset;
    }

    public int getOffset()
    {
        return offset;
    }

    public String getType()
    {
        return type;
    }
}
