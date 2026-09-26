/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.josue.ventas;

import com.josue.ventas.dao.ConexionBD;
import com.josue.ventas.vista.DlgLogin;
import com.josue.ventas.vista.FrmPrincipal;
import javax.swing.JOptionPane;

/**
 * Punto de entrada: se verifica la conexion a la base de datos, luego se
 * inicia sesion y, si es correcta, se abre la ventana principal.
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
            String error = ConexionBD.getInstancia().getErrorConexion();
            if (error != null) {
                JOptionPane.showMessageDialog(null, error, "Sistema de Ventas - Base de datos", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            if (!new DlgLogin().mostrar()) {
                System.exit(0);
            }
            FrmPrincipal ventana = new FrmPrincipal();
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}
