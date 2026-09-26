/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Calcula y verifica hashes de contrasenas. Nunca se guarda la contrasena.
 *
 * - Formato actual: bcrypt (costo 10). La sal va dentro del mismo hash y
 *   PostgreSQL puede verificarlo con pgcrypto, asi la app movil inicia
 *   sesion con los mismos usuarios.
 * - Formato anterior (Fase 12): PBKDF2WithHmacSHA256 con la sal aparte.
 *   Se sigue aceptando y se convierte a bcrypt al iniciar sesion.
 *
 * @author josue zetino
 */
public final class Contrasenas {

    public static final int LONGITUD_MINIMA = 6;
    /** Valor que se guarda en la columna salt cuando el hash es bcrypt. */
    public static final String SAL_BCRYPT = "bcrypt";

    private static final int COSTO_BCRYPT = 10;
    private static final String ALGORITMO_ANTERIOR = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES_ANTERIOR = 120_000;
    private static final int BITS_ANTERIOR = 256;

    private Contrasenas() {
    }

    public static String calcularHash(char[] clave) {
        return BCrypt.hashpw(new String(clave), BCrypt.gensalt(COSTO_BCRYPT));
    }

    public static boolean verificar(char[] clave, String sal, String hashGuardado) {
        if (hashGuardado == null) {
            return false;
        }
        if (esBcrypt(hashGuardado)) {
            try {
                return BCrypt.checkpw(new String(clave), hashGuardado);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        byte[] calculado = hashAnterior(clave, sal).getBytes(StandardCharsets.US_ASCII);
        // comparacion en tiempo constante: no revela cuantos caracteres coinciden
        return MessageDigest.isEqual(calculado, hashGuardado.getBytes(StandardCharsets.US_ASCII));
    }

    /** true si el hash esta en el formato anterior y conviene convertirlo a bcrypt. */
    public static boolean necesitaActualizar(String hashGuardado) {
        return hashGuardado != null && !esBcrypt(hashGuardado);
    }

    private static boolean esBcrypt(String hash) {
        return hash.startsWith("$2");
    }

    private static String hashAnterior(char[] clave, String sal) {
        PBEKeySpec spec = new PBEKeySpec(clave, Base64.getDecoder().decode(sal), ITERACIONES_ANTERIOR, BITS_ANTERIOR);
        try {
            byte[] hash = SecretKeyFactory.getInstance(ALGORITMO_ANTERIOR).generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("PBKDF2 no esta disponible en esta JVM", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
