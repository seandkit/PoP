package com.example.pop.activity;

public class Utils {

    private static String hex_chars = "0123456789ABCDEF";

    public static byte[] hexStringToByteArray(String s){
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i+=2){
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static char[] hexCharsArray = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexCharsArray[v >>> 4];
            hexChars[j * 2 + 1] = hexCharsArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
