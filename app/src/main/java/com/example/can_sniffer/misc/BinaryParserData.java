package com.example.can_sniffer.misc;

import com.example.can_sniffer.BuildConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class BinaryParserData {
    private static final int BITS_IN_BYTE = 8;
    private static final int BYTE_MASK = 0xFF;//все единицы для байта

    // парсинг того, что пришло с кан шины, подается полный массив пакета (до 8 байт) и с какого
    // бита начинать импорт и сколько бит импортировать, а также порядок младший-старший байт
    public static long parse(int[] data, int startFromBit, int lengBits, ByteOrder byteOrder) {
        int lengBytes = lengBits / BITS_IN_BYTE;
        if (lengBytes * BITS_IN_BYTE < lengBits)
            lengBytes++;
        int firstByte = startFromBit / BITS_IN_BYTE;//первый байт, с которого начнем импорт
        int shift = startFromBit % BITS_IN_BYTE;//насколько сдвинуты биты (обычно 0)
        long tempRes = 0;//тут будем сохранять результат, у нас входной массив не более 8 значений, потому хватит
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            for (int n = 0; n < lengBytes; n++) {
                tempRes <<= BITS_IN_BYTE;
                tempRes += data[n + firstByte] & BYTE_MASK;
            }
        } else {
            for (int n = lengBytes - 1; n >= 0; n--) {
                tempRes <<= BITS_IN_BYTE;
                tempRes += data[n + firstByte] & BYTE_MASK;
            }
        }
        tempRes <<= shift;
        BitSet bitMask = new BitSet(lengBits);//отрежем лишние биты
        bitMask.set(0, lengBits);
        tempRes &= bitMask.toLongArray()[0];
        return tempRes;
    }

    public static void testParse() {
        if (BuildConfig.DEBUG) {
            if (parse(new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, 32, ByteOrder.BIG_ENDIAN) != 0)
                throw new AssertionError("parse does not work");
            int[] data = new int[]{0x0, 0xC0, 0x0, 0x80, 0x0, 0xC0, 0x0, 0x40};

            if (parse(data, 0, 14, ByteOrder.BIG_ENDIAN) != 0)
                throw new AssertionError("parse does not work");
            if (parse(data, 16, 14, ByteOrder.BIG_ENDIAN) != 0)
                throw new AssertionError("parse does not work");
            if (parse(data, 32, 14, ByteOrder.BIG_ENDIAN) != 0)
                throw new AssertionError("parse does not work");
            if (parse(data, 48, 14, ByteOrder.BIG_ENDIAN) != 0)
                throw new AssertionError("parse does not work");

            data = new int[]{0x2A, 0x05, 0x2C, 0x05, 0x28, 0x45, 0x2B, 0xC5};

            if (parse(data, 0, 14, ByteOrder.BIG_ENDIAN) != 1322)
                throw new AssertionError("parse does not work");
            if (parse(data, 16, 14, ByteOrder.BIG_ENDIAN) != 1324)
                throw new AssertionError("parse does not work");
            if (parse(data, 32, 14, ByteOrder.BIG_ENDIAN) != 1320)
                throw new AssertionError("parse does not work");
            if (parse(data, 48, 14, ByteOrder.BIG_ENDIAN) != 1323)
                throw new AssertionError("parse does not work");
        }
    }
}
