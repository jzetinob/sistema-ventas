/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.UsuarioController;
import com.josue.ventas.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Inicio de sesion. Se muestra antes de la ventana principal.
 *
 * - Si la base de datos no tiene usuarios (primer uso), pide crear el
 *   usuario administrador; asi el sistema no trae una contrasena fija.
 * - Despues de 3 intentos fallidos el programa se cierra.
 *
 * @author josue zetino
 */
public class DlgLogin extends JDialog {

    private static final int INTENTOS_MAXIMOS = 3;

    private final UsuarioController controller = new UsuarioController();
    private final boolean primerUso;
    private final JTextField txtUsuario = new JTextField(18);
    private final JTextField txtNombre = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);
    private final JPasswordField txtConfirmar = new JPasswordField(18);
    private int intentosFallidos = 0;
    private boolean autenticado = false;

    public DlgLogin() {
        super((java.awt.Frame) null, "Sistema de Ventas - Iniciar sesión", true);
        primerUso = !controller.HayUsuarios();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel campos = new JPanel(new GridBagLayout());
        int fila = 0;
        if (primerUso) {
            JLabel aviso = new JLabel("<html><b>Primer uso del sistema.</b><br>Cree el usuario administrador.</html>");
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = 2;
            c.insets = new Insets(0, 4, 10, 4);
            c.anchor = GridBagConstraints.LINE_START;
            campos.add(aviso, c);
            fila++;
        }
        agregar(campos, fila++, "Usuario:", txtUsuario);
        if (primerUso) {
            agregar(campos, fila++, "Nombre:", txtNombre);
        }
        agregar(campos, fila++, "Contraseña:", txtClave);
        if (primerUso) {
            agregar(campos, fila++, "Confirmar:", txtConfirmar);
        }

        JButton btnAceptar = new JButton(primerUso ? "Crear y entrar" : "Entrar");
        JButton btnCancelar = new JButton("Salir");
        btnAceptar.addActionListener(e -> aceptar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnAceptar);

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));
        contenido.add(campos, BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);
        getRootPane().setDefaultButton(btnAceptar);
        pack();
        setLocationRelativeTo(null);
    }

    private void agregar(JPanel panel, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        panel.add(campo, c);
    }

    /** Muestra el dialogo y devuelve true si el usuario inicio sesion. */
    public boolean mostrar() {
        setVisible(true);
        return autenticado;
    }

    private void aceptar() {
        if (primerUso) {
            crearAdministrador();
        } else {
            iniciarSesion();
        }
    }

    private void crearAdministrador() {
        char[] clave = txtClave.getPassword();
        char[] confirmar = txtConfirmar.getPassword();
        boolean coinciden = Arrays.equals(clave, confirmar);
        Arrays.fill(confirmar, '\0');
        if (!coinciden) {
            Arrays.fill(clave, '\0');
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.");
            return;
        }
        char[] copia = clave.clone();
        String error = controller.Crear(txtUsuario.getText(), txtNombre.getText(), clave, Usuario.Rol.ADMINISTRADOR);
        if (error != null) {
            Arrays.fill(copia, '\0');
            JOptionPane.showMessageDialog(this, error);
            return;
        }
        terminar(controller.IniciarSesion(txtUsuario.getText(), copia));
    }

    private void iniciarSesion() {
        String error = controller.IniciarSesion(txtUsuario.getText(), txtClave.getPassword());
        if (error != null) {
            intentosFallidos++;
            if (intentosFallidos >= INTENTOS_MAXIMOS) {
                JOptionPane.showMessageDialog(this, "Demasiados intentos fallidos. El sistema se cerrará.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
            JOptionPane.showMessageDialog(this, error + "\nIntentos restantes: " + (INTENTOS_MAXIMOS - intentosFallidos),
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            txtClave.setText("");
            txtClave.requestFocusInWindow();
            return;
        }
        terminar(null);
    }

    private void terminar(String error) {
        if (error != null) {
            JOptionPane.showMessageDialog(this, error);
            return;
        }
        autenticado = true;
        dispose();
    }
}
