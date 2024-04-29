package parser;

public class VarInfo {
    // attributes for all variables
    private int offset;
    private int size;
    private String type;
    private boolean isPointer;

    // additional pointer attributes
    private String primitiveDataType;
    private int dataOffset;
    private int unitDataSize;
    private int totalDataSize;

    public VarInfo(String inType, int inSize, int inOffset)
    {
        type = inType;
        size = inSize;
        offset = inOffset;
        isPointer = false;
        primitiveDataType = "";
        unitDataSize = 0;
        totalDataSize = 0;
    }

    public void addPointerInfo(String primitive, int offset, int unitSize, int totalSize)
    {
        isPointer = true;
        primitiveDataType = primitive;
        dataOffset = offset;
        unitDataSize = unitSize;
        totalDataSize = totalSize;
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

    public boolean isPointer()
    {
        return isPointer;
    }

    public String getPrimitive()
    {
        if (isPointer)
        {
            return primitiveDataType;
        }

        return null;
    }

    public int getDataOffset()
    {
        if (isPointer())
        {
            return dataOffset;
        }

        return -1;
    }

    public int getUnitSize()
    {
        if (isPointer)
        {
            return unitDataSize;
        }

        return -1;
    }

    public int getTotalSize()
    {
        if (isPointer)
        {
            return totalDataSize;
        }

        return -1;
    }

    public String toString()
    {
        String s = "";

        if (isPointer)
        {
            s += "\nPOINTER\n";
            s += "Primitive type: " + primitiveDataType;
            s += ", Unit size: " + unitDataSize;
            s += ", Total data size: " + totalDataSize; 
            s += ", Data offset: " + dataOffset;
        }

        s += "\nType: " + type;
        s += ", Offset: " + offset;
        s += ", Size: " + size + "\n";

        return s;
    }
}
