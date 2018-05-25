package ru.threedisevenzeror.marionette.utils;

import io.netty.buffer.ByteBuf;

import java.io.*;

/**
 * Various utils, useful for data processing
 */
public class StreamUtils {

    /**
     * Reads null terminated UTF-8 string
     */
    public static String readNullTerminatedString(ByteBuf stream) {

        ByteArrayOutputStream stringStream = new ByteArrayOutputStream();

        while (true) {
            int c = stream.readByte();
            if(c != 0) {
                stringStream.write(c);
            } else {
                break;
            }
        }

        try {
            return new String(stringStream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Writes null terminated UTF-8 string
     */
    public static void writeNullTerminatedString(ByteBuf stream, String value) {
        try {
            stream.writeBytes(value.getBytes("UTF-8"));
            stream.writeByte(0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
