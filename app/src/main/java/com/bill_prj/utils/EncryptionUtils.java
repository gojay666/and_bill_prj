package com.bill_prj.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密工具类
 * 提供密码加密、验证、AES加解密等功能
 */
public class EncryptionUtils {

    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final String ALGORITHM_MD5 = "MD5";
    private static final String ALGORITHM_AES = "AES";
    private static final String TRANSFORMATION_AES = "AES/CBC/PKCS5Padding";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int PBKDF2_KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    private static final int SALT_LENGTH = 32;

    /**
     * 使用 SHA-256 对密码进行哈希加密
     *
     * @param password 原始密码
     * @return 十六进制字符串形式的哈希值
     */
    public static String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_SHA256);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用 MD5 对密码进行哈希加密
     *
     * @param password 原始密码
     * @return 十六进制字符串形式的 MD5 值
     */
    public static String encryptPasswordMd5(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_MD5);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用 PBKDF2WithHmacSHA256 加盐哈希密码（推荐使用）
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return 十六进制字符串形式的哈希值
     */
    public static String encryptPasswordWithSalt(String password, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    hexToBytes(salt),
                    PBKDF2_ITERATIONS,
                    PBKDF2_KEY_LENGTH
            );
            SecretKey key = factory.generateSecret(spec);
            return bytesToHex(key.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证输入的密码是否与存储的哈希值匹配
     *
     * @param input       用户输入的密码
     * @param storedHash  存储的哈希值
     * @return 是否匹配
     */
    public static boolean verifyPassword(String input, String storedHash) {
        if (input == null || storedHash == null) {
            return false;
        }
        String inputHash = encryptPassword(input);
        return inputHash != null && inputHash.equalsIgnoreCase(storedHash);
    }

    /**
     * 验证加盐哈希密码
     *
     * @param input      用户输入的密码
     * @param storedHash 存储的哈希值
     * @param salt       盐值
     * @return 是否匹配
     */
    public static boolean verifyPasswordWithSalt(String input, String storedHash, String salt) {
        if (input == null || storedHash == null || salt == null) {
            return false;
        }
        String inputHash = encryptPasswordWithSalt(input, salt);
        return inputHash != null && inputHash.equalsIgnoreCase(storedHash);
    }

    /**
     * 生成随机盐值
     *
     * @return 十六进制字符串形式的盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * 使用 AES 加密文本
     *
     * @param text 待加密的明文
     * @param key  密钥（至少16个字符）
     * @return Base64 编码的密文字符串（包含IV），格式：iv:encrypted
     */
    public static String encrypt(String text, String key) {
        try {
            // 生成IV
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 生成密钥
            SecretKeySpec secretKey = generateAESKey(key);

            // 初始化Cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // 执行加密
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            // 返回 iv:encrypted 格式的 Base64 字符串
            String ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP);
            String encryptedBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP);
            return ivBase64 + ":" + encryptedBase64;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用 AES 解密文本
     *
     * @param encryptedText 密文字符串（格式：iv:encrypted）
     * @param key           密钥（至少16个字符）
     * @return 解密后的明文字符串
     */
    public static String decrypt(String encryptedText, String key) {
        try {
            // 解析 iv:encrypted 格式
            String[] parts = encryptedText.split(":");
            if (parts.length != 2) {
                return null;
            }

            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKey = generateAESKey(key);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用 PBKDF2 从密钥字符串生成 AES 密钥
     */
    private static SecretKeySpec generateAESKey(String key) throws Exception {
        // 使用 PBKDF2 从密钥字符串派生 AES 密钥
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        // 使用密钥本身作为盐值，固定迭代次数
        KeySpec spec = new PBEKeySpec(
                key.toCharArray(),
                key.getBytes(StandardCharsets.UTF_8),
                PBKDF2_ITERATIONS,
                PBKDF2_KEY_LENGTH
        );
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM_AES);
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     */
    public static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * 简单 SHA-256 哈希
     */
    public static String sha256(String text) {
        return encryptPassword(text);
    }

    /**
     * 简单 MD5 哈希
     */
    public static String md5(String text) {
        return encryptPasswordMd5(text);
    }
}
