package com.ceit.management.security;

import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

public final class SecurityEngine
{
    private static final String CIPHER_NAME = "AES/CBC/PKCS5PADDING";
    private static final int CIPHER_KEY_LEN = 16;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String encrypt(String key, String iv, String data)
    {
        try {
            if(key.length() < CIPHER_KEY_LEN)
            {
                int numPad = CIPHER_KEY_LEN - key.length();

                for(int i = 0; i < numPad; i++)
                    key += "0";
            }
            else if(key.length() > CIPHER_KEY_LEN)
                key = key.substring(0, CIPHER_KEY_LEN);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] encryptedBits = cipher.doFinal(data.getBytes());
            String b64EncData = Base64.encodeToString(encryptedBits, Base64.DEFAULT);
            String b64EncIV = Base64.encodeToString(iv.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);

            return (b64EncData + ":" + b64EncIV);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String decrypt(String key, String data)
    {
        try {
            String[] parts = data.split(":");
            String b64EncStr = parts[0];
            String b64EncIV = parts[1];

            IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.decode(b64EncIV, Base64.DEFAULT));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] b64EncData = Base64.decode(b64EncStr, Base64.DEFAULT);
            byte[] aesDecData = cipher.doFinal(b64EncData);

            return (new String(aesDecData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
