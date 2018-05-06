package ru.threedisevenzeror.marionette.utils;

import java.io.DataInputStream;
import java.io.IOException;

public class DataUtils {

    /**
     * Reads string untill '\0' character
     */
    public static String readNullTerminatedString(DataInputStream stream) throws IOException {

        StringBuilder builder = new StringBuilder();

        while (true) {
            byte c = stream.readByte();

            if(c != '\0') {
                builder.append((char) c);
            } else {
                break;
            }
        }

        return builder.toString();
    }
}
