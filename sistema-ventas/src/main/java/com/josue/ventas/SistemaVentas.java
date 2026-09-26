/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.josue.ventas;

import com.josue.ventas.vista.DlgLogin;
import com.josue.ventas.vista.FrmPrincipal;

/**
 * Punto de entrada: primero se inicia sesion y, si es correcta, se abre la
 * ventana principal.
 *
 * @author josue zetino
 */
public class SistemaVentas {

    public static void main(String[] args) {
        iniciar();
    }

    /** Muestra el inicio de sesion; tambien se usa al cerrar sesion. */
    public static void iniciar() {
        java.awt.EventQueue.invokeLater(() -> {
            if (!new DlgLogin().mostrar()) {
                System.exit(0);
            }
            FrmPrincipal ventana = new FrmPrincipal();
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}
