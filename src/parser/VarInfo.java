package parser;

public class VarInfo {
    private int offset;
    private int size;
    private String type;

    public VarInfo(String inType, int inSize, int inOffset)
    {
        type = inType;
        size = inSize;
        offset = inOffset;
    }

    public int getSize()
    {
        return size;
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
