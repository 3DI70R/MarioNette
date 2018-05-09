package ru.threedisevenzeror.marionette.utils;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;

/**
 * Various utils, useful for data processing
 */
public class StreamUtils {

    /**
     * Reads null terminated UTF-8 string
     */
    public static String readNullTerminatedString(Input stream) {

        ByteArrayOutputStream stringStream = new ByteArrayOutputStream();

        while (true) {
            byte c = stream.readByte();
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
    public static void writeNullTerminatedString(Output stream, String value) {
        try {
            stream.writeBytes(value.getBytes("UTF-8"));
            stream.write(0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
