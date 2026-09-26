/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Calcula y verifica hashes de contrasenas con PBKDF2 (API de seguridad
 * incluida en Java). Cada contrasena lleva su propia sal aleatoria, asi
 * dos usuarios con la misma contrasena tienen hashes distintos.
 *
 * @author josue zetino
 */
public final class Contrasenas {

    public static final int LONGITUD_MINIMA = 6;

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int BITS_HASH = 256;
    private static final int BYTES_SAL = 16;
    private static final SecureRandom aleatorio = new SecureRandom();

    private Contrasenas() {
    }

    public static String generarSal() {
        byte[] sal = new byte[BYTES_SAL];
        aleatorio.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal);
    }

    public static String calcularHash(char[] clave, String sal) {
        PBEKeySpec spec = new PBEKeySpec(clave, Base64.getDecoder().decode(sal), ITERACIONES, BITS_HASH);
        try {
            byte[] hash = SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("PBKDF2 no esta disponible en esta JVM", ex);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verificar(char[] clave, String sal, String hashGuardado) {
        byte[] calculado = calcularHash(clave, sal).getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] guardado = hashGuardado.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        // comparacion en tiempo constante: no revela cuantos caracteres coinciden
        return MessageDigest.isEqual(calculado, guardado);
    }
}
