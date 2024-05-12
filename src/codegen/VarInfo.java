package codegen;

import java.util.Map;
import java.util.HashMap;

public class VarInfo {
    // attributes for all variables
    private int offset;
    private int size;
    private String type;
    private boolean isPointer;
    private Map<String, String> metaData;

    // additional pointer attributes
    private String primitiveDataType;
    private int dataOffset;
    private int unitDataSize;
    private int totalDataSize;

    public VarInfo(String inType)
    {
        type = inType;
        size = 0;
        offset = 0;
        isPointer = false;
        primitiveDataType = "";
        unitDataSize = 0;
        totalDataSize = 0;
        metaData = new HashMap<String, String>();
    }

    public VarInfo(String inType, int inSize, int inOffset)
    {
        type = inType;
        size = inSize;
        offset = inOffset;
        isPointer = false;
        primitiveDataType = "";
        unitDataSize = 0;
        totalDataSize = 0;
        metaData = new HashMap<String, String>();
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
        s += "Metadata: " + metaData.toString() + "\n";

        return s;
    }

    public void updateMetaData(String key, String value)
    {
        metaData.put(key, value);
    }

    public String getMetaData(String key)
    {
        return metaData.get(key);
    }
}
