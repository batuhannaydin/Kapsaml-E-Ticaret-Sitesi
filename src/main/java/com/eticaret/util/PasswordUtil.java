package com.eticaret.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Sifre hash islemleri icin yardimci sinif (SHA-256 algoritmasi).
public final class PasswordUtil {
    private PasswordUtil() {}

    // Verilen duz metin sifreyi SHA-256 ile hashleyip hex string olarak döndürür
    public static String hash(String plain) {
        if (plain == null) return null;
        try {
            // SHA-256 algoritmasını hazırlamaa
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(plain.getBytes("UTF-8"));
            // Byte dizisini hex stringe cevirme
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("SHA-256 hash hatasi", e);
        }
    }

    // Kullanıcının girdiği şifreyi, veritabanında kayıtlı hash ile karsılastırır
    // Eşleşirse true döner; login doğrulamasında bu metot kullanılır
    public static boolean matches(String plain, String hashed) {
        return hashed != null && hashed.equals(hash(plain));
    }
}
