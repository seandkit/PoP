package com.example.pop.helper;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HashingFunctions {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "F0C101355CD00EF098BD78C3D85E141C";

    public static String encrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        return encryptedValue64;
    }

    private static Key generateKey() {
        Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        return key;
    }
}
