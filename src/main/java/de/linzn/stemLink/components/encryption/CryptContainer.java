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

public class CryptContainer {
    private final byte[] key;
    private final byte[] vectorB16;

    /**
     * Constructor for create a CryptContainer
     * Container for symmetric AES encryption
     *
     * @param key       32 char length key as string for encryption (256 bit)
     * @param vectorB16 16 byte vector as byte array (128 bit)
     */
    public CryptContainer(String key, byte[] vectorB16) {
        this.key = hexStringToByteArray(key);
        this.vectorB16 = vectorB16;
    }

    /**
     * Check and get the vector byte array
     *
     * @param bytes Byte array to check if is valid
     * @return a cloned array of the input bytes
     */
    private static byte[] getVectorB16(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException(
                    "Vector size must be 16");
        }
        return bytes.clone();
    }

    /**
     * Convert hexadecimal string to byte array
     *
     * @param hexString Key as hexadecimal string
     * @return key as byte array
     */
    private static byte[] hexStringToByteArray(String hexString) {
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal String supplied.");
        }

        if (hexString.length() != 64) {
            throw new IllegalArgumentException("AES key must be in hex and a length of 64 chars");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    /**
     * Convert hex chars to byte
     *
     * @param hexString hex part as string to convert
     * @return byte for this hex part
     */
    private static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    /**
     * Convert char to byte
     *
     * @param hexChar char to convert
     * @return converted char as byte
     */
    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if (digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: " + hexChar);
        }
        return digit;
    }

    /**
     * Get the encryption key as byte array
     *
     * @return 32 byte encryption key as array
     */
    byte[] getKey() {
        return key;
    }

    /**
     * Get vector as byte array
     *
     * @return 16 byte vector key as array
     */
    byte[] getVectorB16() {
        return vectorB16;
    }

}
