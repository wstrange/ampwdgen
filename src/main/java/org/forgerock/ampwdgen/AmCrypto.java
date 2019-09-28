package org.forgerock.ampwdgen;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AmCrypto {

  //Salt, interaction count etc as defined in OpenAM source, JCEEncryption.java
  private static final byte VERSION = 1;
  private static final int DEFAULT_KEYGEN_ALG_INDEX = 2;
  private static final int DEFAULT_ENC_ALG_INDEX = 2;
  private static final int ITERATION_COUNT = 5;
  private static final byte[] salt = { 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
    0x01, 0x01 };

  public static final String hash(String text) {

    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      return Base64.getEncoder().encodeToString(md.digest(text.getBytes()));

    } catch(NoSuchAlgorithmException x) {
      System.out.println("Hash algorythm not supported.");
    }

    return "";

  }

  //Based on ALMailUtils.java from aipo https://github.com/aipocom
  public static final String encrypt(String password, String clearText) {
    byte[] ciphertext = null;
    PBEKeySpec pbeKeySpec;
    PBEParameterSpec pbeParamSpec;
    SecretKeyFactory keyFac;

    byte[] data = clearText.getBytes();

    // Iteration count
    int count = 5;

    // Create PBE parameter set
    pbeParamSpec = new PBEParameterSpec(salt, count);

    pbeKeySpec = new PBEKeySpec(password.toCharArray());

    try {
      keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

      // Create PBE Cipher
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");

      // Initialize PBE Cipher with key and parameters
      pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

      // Encrypt/Decrypt the cleartext
      ciphertext = pbeCipher.doFinal(data);

      // Get initialization vector
      byte[] iv = pbeCipher.getIV();

      // pad with algorythm type and initialization vector
      byte type[] = new byte[2];
      type[1] = (byte) DEFAULT_ENC_ALG_INDEX;
      type[0] = (byte) DEFAULT_KEYGEN_ALG_INDEX;
      ciphertext = addPrefix(type, iv, ciphertext);

    } catch (Exception e) {
      System.err.println("cipher error");
      return null;
    }

    return Base64.getEncoder().encodeToString(ciphertext);

  }

  //From OpenAM source, JCEEncryption.java
  private static byte[] addPrefix(byte type[], byte iv[], byte share[]) {
    byte data[] = new byte[share.length + 11];

    data[0] = VERSION;
    data[1] = type[0];
    data[2] = type[1];

    for (int i = 0; i < 8; i++) {
      data[3 + i] = iv[i];
    }

    for (int i = 0; i < share.length; i++) {
      data[11 + i] = share[i];
    }

    return data;
  }

}
