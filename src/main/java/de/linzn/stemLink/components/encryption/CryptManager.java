/*
 * Copyright (C) 2021. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.stemLink.components.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CryptManager {
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    /**
     * Constructor for CryptManager
     *
     * @param cryptContainer CryptContainer with encryption key and vector
     */
    public CryptManager(CryptContainer cryptContainer) {
        setEncryption(cryptContainer);
    }

    /**
     * Encrypt input byte array with AES
     *
     * @param bytes Unencrypted bytes as array
     * @return Encrypted bytes as array
     */
    public byte[] encryptFinal(byte[] bytes) {
        try {
            return Base64.getEncoder().encode(encryptCipher.doFinal(bytes));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt AES encrypted input byte array
     *
     * @param bytes Encrypted bytes as array
     * @return Decrypted bytes as array
     */
    public byte[] decryptFinal(byte[] bytes) throws IllegalBlockSizeException, BadPaddingException {
        return decryptCipher.doFinal(Base64.getDecoder().decode(bytes));
    }


    /**
     * Set the AES encryption parameter
     *
     * @param cryptContainer Key and vector as CryptContainer
     */
    private void setEncryption(CryptContainer cryptContainer) {
        try {
            IvParameterSpec iv = new IvParameterSpec(cryptContainer.getVectorB16());
            SecretKeySpec skeySpec = new SecretKeySpec(cryptContainer.getKey(), "AES");

            this.decryptCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            this.decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            this.encryptCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            this.encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
}
