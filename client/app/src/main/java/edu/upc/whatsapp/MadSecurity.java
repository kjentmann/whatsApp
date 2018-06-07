package edu.upc.whatsapp;
import android.util.Log;

import se.simbio.encryption.Encryption;

public class MadSecurity {
    static String key = "MrSnowden";
    static String salt = "MySalt";
    static byte[] iv = {-89, -19, 17, -83, 86, 106, -31, 30, -5, -111, 61, -75, -84, 95, 120, -53};
     static Encryption encryption = Encryption.getDefault(key, salt, iv);

    public MadSecurity() {
/*
            try {
                encryption = new Encryption.Builder()
                        .setKeyLength(128)
                        .setKey("YourKey")
                        .setSalt("YourSalt")
                        .setIv(iv)
                        .setCharsetName("UTF8")
                        .setIterationCount(1)
                        .setDigestAlgorithm("SHA1")
                        .setBase64Mode(Base64.DEFAULT)
                        .setAlgorithm("AES/CBC/PKCS5Padding")
                        .setSecureRandomAlgorithm("SHA1PRNG")
                        .setSecretKeyType("PBKDF2WithHmacSHA1")
                        .build();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                Log.d("DEBUG", "failed encr");
            }
*/
    }

    public static String encrypt(String strClearText) {
        String encrypted = "";
        try {
            encrypted = encryption.encrypt(strClearText);
        } catch (Exception e) {
            encrypted = "** Encryption Error **";
        }
        Log.d("DEBUG", " ClearText:  " + strClearText + " Encrypted:  " + encrypted);

        return encrypted;
    }


    public static String decrypt(String encodedString) {
        String decrypted = "";
        try {
            decrypted = encryption.decrypt(encodedString);
        } catch (Exception e) {
            decrypted = "** Decryption error **";
        }
        return decrypted;
    }
}
