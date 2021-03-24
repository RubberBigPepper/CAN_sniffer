package com.example.can_sniffer.misc;

public class ByteUtils {

    public static int Bytes2Int32(byte[] byArData, int nOffset)
    {
        return (byArData[nOffset++]<<24)&0xff000000|
                (byArData[nOffset++]<<16)&0x00ff0000|
                (byArData[nOffset++]<< 8)&0x0000ff00|
                (byArData[nOffset++]<< 0)&0x000000ff;
    }

    public static int Bytes2Int32(byte[] byArData)
    {
        return Bytes2Int32(byArData,0);
    }

    public static int Bytes2Int16(byte[] byArData) {
        return Bytes2Int16(byArData, 0);
    }

    public static int Bytes2Int16(byte[] byArData, int offset)
    {
        return  (byArData[offset++]<< 8)&0x0000ff00|
                (byArData[offset]<< 0)&0x000000ff;
    }


    public static String Bytes2String(byte[] byArData)
    {
        StringBuilder cBuilder=new StringBuilder();
        for(int n=0;n<byArData.length;n++)
        {
            if(n>0)
                cBuilder.append(", ");
            cBuilder.append(String.format("0x%02x", byArData[n]));
        }
        return cBuilder.toString();
    }

    public static byte[] Int32ToByte(int nValue)
    {
        byte[] byArRes = new byte[4];
        byArRes[0] = (byte) ((nValue & 0xFF000000) >> 24);
        byArRes[1] = (byte) ((nValue & 0x00FF0000) >> 16);
        byArRes[2] = (byte) ((nValue & 0x0000FF00) >> 8);
        byArRes[3] = (byte) ((nValue & 0x000000FF) >> 0);
        return byArRes;
    }

    public static byte[] Int16ToByte(int nValue)
    {
        byte[] byArRes = new byte[2];
        byArRes[0] = (byte) ((nValue & 0x0000FF00) >> 8);
        byArRes[1] = (byte) (nValue & 0x000000FF) ;
        return byArRes;
    }

    public static byte[] StringToBytes(String str)
    {
        try
        {
            return str.getBytes("ASCII");
        }
        catch (Exception ex){
            return null;
        }
    }
}
