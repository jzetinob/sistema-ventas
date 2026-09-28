/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Apariencia del sistema (FlatLaf) con tema claro y oscuro.
 *
 * En modo AUTOMATICO el tema sigue la hora de la computadora, igual que la
 * app movil: claro de 06:00 a 17:59 y oscuro de 18:00 a 05:59. Un Timer
 * revisa la hora cada minuto y cambia el tema sin cerrar las ventanas.
 * El modo elegido en el menu Ventana > Tema se recuerda (Preferences).
 *
 * Tambien da los colores de los componentes dibujados a mano (graficos,
 * tarjetas del Resumen) para que respeten el tema.
 *
 * @author josue zetino
 */
public final class Tema {

    public enum Modo {
        AUTOMATICO("Automático (por hora)"), CLARO("Claro"), OSCURO("Oscuro");

        private final String texto;

        Modo(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    static final int HORA_INICIO_DIA = 6;
    static final int HORA_INICIO_NOCHE = 18;

    private static final Preferences preferencias = Preferences.userNodeForPackage(Tema.class);
    private static final List<Runnable> oyentes = new ArrayList<>();
    private static Modo modo = Modo.AUTOMATICO;
    private static boolean oscuro = false;
    private static Timer reloj;

    private Tema() {
    }

    /** Se llama una vez al iniciar el programa, antes de crear cualquier ventana. */
    public static void instalar() {
        try {
            modo = Modo.valueOf(preferencias.get("modo", Modo.AUTOMATICO.name()));
        } catch (IllegalArgumentException ex) {
            modo = Modo.AUTOMATICO;
        }
        // color de acento igual al de la app movil y esquinas redondeadas
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#3C7DBA"));
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Table.rowHeight", 24);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("InternalFrame.borderMargins", new java.awt.Insets(1, 1, 1, 1));
        aplicar(debeSerOscuro(), false);

        reloj = new Timer(60_000, e -> {
            if (modo == Modo.AUTOMATICO && debeSerOscuro() != oscuro) {
                aplicar(!oscuro, true);
            }
        });
        reloj.start();
    }

    public static Modo getModo() {
        return modo;
    }

    /** Cambia el modo desde el menu y lo aplica de inmediato. */
    public static void setModo(Modo nuevo) {
        modo = nuevo;
        preferencias.put("modo", nuevo.name());
        if (debeSerOscuro() != oscuro) {
            aplicar(!oscuro, true);
        }
    }

    public static boolean esOscuro() {
        return oscuro;
    }

    /** Para componentes con colores propios: se avisa despues de cada cambio de tema. */
    public static void alCambiar(Runnable oyente) {
        oyentes.add(oyente);
    }

    public static void quitar(Runnable oyente) {
        oyentes.remove(oyente);
    }

    static boolean esDeNoche(LocalTime hora) {
        return hora.getHour() >= HORA_INICIO_NOCHE || hora.getHour() < HORA_INICIO_DIA;
    }

    private static boolean debeSerOscuro() {
        return switch (modo) {
            case CLARO -> false;
            case OSCURO -> true;
            case AUTOMATICO -> esDeNoche(LocalTime.now());
        };
    }

    private static void aplicar(boolean deNoche, boolean actualizarVentanas) {
        oscuro = deNoche;
        if (deNoche) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        if (actualizarVentanas) {
            FlatLaf.updateUI(); // repinta todas las ventanas abiertas con el tema nuevo
            for (Runnable oyente : new ArrayList<>(oyentes)) {
                oyente.run();
            }
        }
    }

    // ---------------------------------------------------------------- colores

    public static Color fondoTarjeta() {
        return oscuro ? new Color(0x2B2D30) : Color.WHITE;
    }

    public static Color borde() {
        return oscuro ? new Color(0x43454A) : new Color(0xDDE3EA);
    }

    public static Color texto() {
        return oscuro ? new Color(0xDFE1E5) : new Color(0x1D2733);
    }

    public static Color textoSuave() {
        return oscuro ? new Color(0x9DA3AB) : new Color(0x5F6B78);
    }

    public static Color primario() {
        return oscuro ? new Color(0x8FBBE3) : new Color(0x2F4F6F);
    }

    public static Color rejilla() {
        return oscuro ? new Color(0x3A3D42) : new Color(0xE6EAF0);
    }

    public static Color alerta() {
        return oscuro ? new Color(0xF1948A) : new Color(0xC0392B);
    }

    public static Color exito() {
        return oscuro ? new Color(0x7DCEA0) : new Color(0x1E8449);
    }

    public static Color escritorio() {
        return oscuro ? new Color(0x1E1F22) : new Color(0xE9ECF1);
    }

    /** Colores de las series de los graficos (los mismos de la app movil). */
    public static Color serie(int i) {
        Color[] claro = {new Color(0x3C7DBA), new Color(0xE08E2B), new Color(0x2E9E6B), new Color(0x9B59B6), new Color(0xD4526E)};
        Color[] noche = {new Color(0x6FA8DC), new Color(0xF0B066), new Color(0x6CCB9C), new Color(0xC39BD3), new Color(0xEC8FA4)};
        return (oscuro ? noche : claro)[i % claro.length];
    }
}
