/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.josue.ventas.vista;

import com.josue.ventas.controlador.RespaldoController;
import com.josue.ventas.controlador.Sesion;
import com.josue.ventas.controlador.UsuarioController;
import com.josue.ventas.modelo.Usuario;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Ventana principal que actúa como contenedor MDI (Multiple Document
 * Interface): mantiene un JDesktopPane donde se abren los formularios
 * hijos (JInternalFrame) desde el menú.
 *
 * @author josue zetino
 */
public class FrmPrincipal extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmPrincipal.class.getName());

    FrmFactura facturaVentana;
    FrmListaFacturas listaVentana;
    FrmProductos productosVentana;
    FrmClientes clientesVentana;
    FrmEmpleados empleadosVentana;
    FrmCategorias categoriasVentana;
    FrmProveedores proveedoresVentana;
    FrmCompra compraVentana;
    FrmListaCompras listaComprasVentana;
    FrmUsuarios usuariosVentana;
    FrmResumen resumenVentana;

    public FrmPrincipal() {
        initComponents();
        aplicarSesion();
        setLocationRelativeTo(null);
    }

    /**
     * Muestra el usuario en el titulo y oculta lo que su rol no puede usar:
     * el vendedor solo factura y atiende clientes.
     */
    private void aplicarSesion() {
        Usuario usuario = Sesion.getInstancia().getUsuario();
        if (usuario == null) {
            return;
        }
        boolean nube = com.josue.ventas.dao.ConexionBD.getInstancia().getMotor() == com.josue.ventas.dao.ConexionBD.Motor.POSTGRESQL;
        setTitle("Sistema de Ventas - " + usuario.getNombre() + " (" + usuario.getRol() + ")"
                + (nube ? " - Base en la nube" : " - Base local"));
        boolean admin = usuario.esAdministrador();
        miProductos.setVisible(admin);
        miEmpleados.setVisible(admin);
        miCategorias.setVisible(admin);
        miProveedores.setVisible(admin);
        mnCompras.setVisible(admin);
        mnAdministracion.setVisible(admin);
        mnReportes.setVisible(admin);
    }

    private void abrirCategorias() {
        if (categoriasVentana == null || !categoriasVentana.isDisplayable()) {
            categoriasVentana = new FrmCategorias();
        }
        abrirFormulario(categoriasVentana);
    }

    private void abrirProveedores() {
        if (proveedoresVentana == null || !proveedoresVentana.isDisplayable()) {
            proveedoresVentana = new FrmProveedores();
        }
        abrirFormulario(proveedoresVentana);
    }

    private void abrirCompra() {
        if (compraVentana == null || !compraVentana.isDisplayable()) {
            compraVentana = new FrmCompra();
        }
        abrirFormulario(compraVentana);
    }

    private void abrirListaCompras() {
        if (listaComprasVentana == null || !listaComprasVentana.isDisplayable()) {
            listaComprasVentana = new FrmListaCompras();
        }
        abrirFormulario(listaComprasVentana);
    }

    private void abrirResumen() {
        // se crea de nuevo cada vez: asi siempre muestra los datos del momento
        if (resumenVentana != null && resumenVentana.isDisplayable()) {
            resumenVentana.dispose();
        }
        resumenVentana = new FrmResumen();
        abrirFormulario(resumenVentana);
    }

    private void abrirUsuarios() {
        if (usuariosVentana == null || !usuariosVentana.isDisplayable()) {
            usuariosVentana = new FrmUsuarios();
        }
        abrirFormulario(usuariosVentana);
    }

    private void cambiarMiClave() {
        javax.swing.JPasswordField actual = new javax.swing.JPasswordField(15);
        javax.swing.JPasswordField nueva = new javax.swing.JPasswordField(15);
        javax.swing.JPasswordField confirmar = new javax.swing.JPasswordField(15);
        Object[] campos = {"Contraseña actual:", actual, "Nueva contraseña:", nueva, "Confirmar nueva:", confirmar};
        int respuesta = JOptionPane.showConfirmDialog(this, campos, "Cambiar contraseña",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        if (!java.util.Arrays.equals(nueva.getPassword(), confirmar.getPassword())) {
            JOptionPane.showMessageDialog(this, "La nueva contraseña y su confirmación no coinciden.");
            return;
        }
        String error = new UsuarioController().CambiarMiClave(actual.getPassword(), nueva.getPassword());
        JOptionPane.showMessageDialog(this, error != null ? error : "Contraseña actualizada.");
    }

    private void agregarReporte(String texto, Runnable accion) {
        javax.swing.JMenuItem item = new javax.swing.JMenuItem(texto);
        item.addActionListener(evt -> accion.run());
        mnReportes.add(item);
    }

    private void respaldarBaseDatos() {
        javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
        selector.setDialogTitle("Guardar respaldo de la base de datos");
        selector.setSelectedFile(new java.io.File("respaldo-sistema-ventas-"
                + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".db"));
        if (selector.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        String error = new RespaldoController().Respaldar(selector.getSelectedFile());
        JOptionPane.showMessageDialog(this, error != null ? error
                : "Respaldo creado correctamente.\nPara restaurarlo, cierre el sistema y reemplace datos/sistema_ventas.db por ese archivo.");
    }

    private void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?", "Cerrar sesión",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            Sesion.getInstancia().cerrar();
            dispose();
            com.josue.ventas.SistemaVentas.iniciar();
        }
    }

    private void abrirFactura() {
        if (facturaVentana == null || !facturaVentana.isDisplayable()) {
            facturaVentana = new FrmFactura();
        }
        abrirFormulario(facturaVentana);
    }

    private void abrirListaFacturas() {
        if (listaVentana == null || !listaVentana.isDisplayable()) {
            listaVentana = new FrmListaFacturas(jDesktopPane);
        }
        abrirFormulario(listaVentana);
    }

    private void abrirProductos() {
        if (productosVentana == null || !productosVentana.isDisplayable()) {
            productosVentana = new FrmProductos();
        }
        abrirFormulario(productosVentana);
    }

    private void abrirClientes() {
        if (clientesVentana == null || !clientesVentana.isDisplayable()) {
            clientesVentana = new FrmClientes();
        }
        abrirFormulario(clientesVentana);
    }

    private void abrirEmpleados() {
        if (empleadosVentana == null || !empleadosVentana.isDisplayable()) {
            empleadosVentana = new FrmEmpleados();
        }
        abrirFormulario(empleadosVentana);
    }

    private void abrirFormulario(JInternalFrame frame) {
        if (!frame.isVisible()) {
            jDesktopPane.add(frame);
            frame.setVisible(true);
            Dimension escritorio = jDesktopPane.getSize();
            frame.setLocation(Math.max(0, (escritorio.width - frame.getWidth()) / 2),
                    Math.max(0, (escritorio.height - frame.getHeight()) / 2));
        }
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException ex) {
            logger.log(java.util.logging.Level.WARNING, "No se pudo seleccionar la ventana hija.", ex);
        }
        frame.toFront();
    }

    private void cascada() {
        int x = 0;
        int y = 0;
        for (JInternalFrame frame : jDesktopPane.getAllFrames()) {
            if (!frame.isIcon()) {
                frame.setLocation(x, y);
                x += 30;
                y += 30;
            }
        }
    }

    private void mosaico() {
        java.util.List<JInternalFrame> visibles = new java.util.ArrayList<>();
        for (JInternalFrame frame : jDesktopPane.getAllFrames()) {
            if (!frame.isIcon()) {
                visibles.add(frame);
            }
        }
        if (visibles.isEmpty()) {
            return;
        }
        int n = visibles.size();
        int columnas = (int) Math.ceil(Math.sqrt(n));
        int filas = (int) Math.ceil((double) n / columnas);
        int ancho = jDesktopPane.getWidth() / columnas;
        int alto = jDesktopPane.getHeight() / filas;
        for (int i = 0; i < n; i++) {
            visibles.get(i).setBounds((i % columnas) * ancho, (i / columnas) * alto, ancho, alto);
        }
    }

    private void minimizarTodo() {
        for (JInternalFrame frame : jDesktopPane.getAllFrames()) {
            try {
                frame.setIcon(true);
            } catch (java.beans.PropertyVetoException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo minimizar la ventana hija.", ex);
            }
        }
    }

    private void restaurarTodo() {
        for (JInternalFrame frame : jDesktopPane.getAllFrames()) {
            try {
                frame.setIcon(false);
            } catch (java.beans.PropertyVetoException ex) {
                logger.log(java.util.logging.Level.WARNING, "No se pudo restaurar la ventana hija.", ex);
            }
        }
    }

    private void limpiarFactura() {
        if (facturaVentana != null && facturaVentana.isDisplayable()) {
            facturaVentana.limpiarFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "No hay ninguna factura abierta.");
        }
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?", "Salir",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void acercaDe() {
        JOptionPane.showMessageDialog(this,
                "Sistema de Ventas\nVersión 1.1 (MDI)\n\nJosue Zetino",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        mnArchivo = new javax.swing.JMenu();
        miNuevaFactura = new javax.swing.JMenuItem();
        miVerFacturas = new javax.swing.JMenuItem();
        miSalir = new javax.swing.JMenuItem();
        mnCatalogos = new javax.swing.JMenu();
        miProductos = new javax.swing.JMenuItem();
        miClientes = new javax.swing.JMenuItem();
        miEmpleados = new javax.swing.JMenuItem();
        mnEdicion = new javax.swing.JMenu();
        miLimpiar = new javax.swing.JMenuItem();
        mnVentana = new javax.swing.JMenu();
        miCascada = new javax.swing.JMenuItem();
        miMosaico = new javax.swing.JMenuItem();
        miMinimizar = new javax.swing.JMenuItem();
        miRestaurar = new javax.swing.JMenuItem();
        mnAyuda = new javax.swing.JMenu();
        miAcercaDe = new javax.swing.JMenuItem();
        jDesktopPane = new javax.swing.JDesktopPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Ventas");
        setMinimumSize(new java.awt.Dimension(800, 600));

        mnArchivo.setText("Archivo");
        mnArchivo.setMnemonic('A');

        miNuevaFactura.setText("Nueva Factura");
        miNuevaFactura.setMnemonic('N');
        miNuevaFactura.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        miNuevaFactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miNuevaFacturaActionPerformed(evt);
            }
        });
        mnArchivo.add(miNuevaFactura);

        miVerFacturas.setText("Ver Facturas");
        miVerFacturas.setMnemonic('V');
        miVerFacturas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miVerFacturasActionPerformed(evt);
            }
        });
        mnArchivo.add(miVerFacturas);

        mnArchivo.addSeparator();

        javax.swing.JMenuItem miCambiarClave = new javax.swing.JMenuItem("Cambiar mi contraseña");
        miCambiarClave.addActionListener(evt -> cambiarMiClave());
        mnArchivo.add(miCambiarClave);

        javax.swing.JMenuItem miCerrarSesion = new javax.swing.JMenuItem("Cerrar sesión");
        miCerrarSesion.addActionListener(evt -> cerrarSesion());
        mnArchivo.add(miCerrarSesion);

        mnArchivo.addSeparator();

        miSalir.setText("Salir");
        miSalir.setMnemonic('S');
        miSalir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        miSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSalirActionPerformed(evt);
            }
        });
        mnArchivo.add(miSalir);

        jMenuBar1.add(mnArchivo);

        mnCatalogos.setText("Catálogos");
        mnCatalogos.setMnemonic('C');

        miProductos.setText("Productos");
        miProductos.setMnemonic('P');
        miProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miProductosActionPerformed(evt);
            }
        });
        mnCatalogos.add(miProductos);

        miEmpleados.setText("Empleados");
        miEmpleados.setMnemonic('E');
        miEmpleados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miEmpleadosActionPerformed(evt);
            }
        });
        mnCatalogos.add(miEmpleados);

        miClientes.setText("Clientes");
        miClientes.setMnemonic('C');
        miClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miClientesActionPerformed(evt);
            }
        });
        mnCatalogos.add(miClientes);

        miCategorias = new javax.swing.JMenuItem("Categorías");
        miCategorias.addActionListener(evt -> abrirCategorias());
        mnCatalogos.add(miCategorias);

        miProveedores = new javax.swing.JMenuItem("Proveedores");
        miProveedores.setMnemonic('r');
        miProveedores.addActionListener(evt -> abrirProveedores());
        mnCatalogos.add(miProveedores);

        jMenuBar1.add(mnCatalogos);

        mnCompras = new javax.swing.JMenu("Compras");
        mnCompras.setMnemonic('o');
        javax.swing.JMenuItem miNuevaCompra = new javax.swing.JMenuItem("Nueva Compra");
        miNuevaCompra.addActionListener(evt -> abrirCompra());
        mnCompras.add(miNuevaCompra);
        javax.swing.JMenuItem miVerCompras = new javax.swing.JMenuItem("Ver Compras");
        miVerCompras.addActionListener(evt -> abrirListaCompras());
        mnCompras.add(miVerCompras);
        jMenuBar1.add(mnCompras);

        mnEdicion.setText("Edición");
        mnEdicion.setMnemonic('E');

        miLimpiar.setText("Limpiar Formulario");
        miLimpiar.setMnemonic('L');
        miLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLimpiarActionPerformed(evt);
            }
        });
        mnEdicion.add(miLimpiar);

        jMenuBar1.add(mnEdicion);

        mnVentana.setText("Ventana");
        mnVentana.setMnemonic('V');

        miCascada.setText("Cascada");
        miCascada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCascadaActionPerformed(evt);
            }
        });
        mnVentana.add(miCascada);

        miMosaico.setText("Mosaico");
        miMosaico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miMosaicoActionPerformed(evt);
            }
        });
        mnVentana.add(miMosaico);

        mnVentana.addSeparator();

        miMinimizar.setText("Minimizar todo");
        miMinimizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miMinimizarActionPerformed(evt);
            }
        });
        mnVentana.add(miMinimizar);

        miRestaurar.setText("Restaurar todo");
        miRestaurar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miRestaurarActionPerformed(evt);
            }
        });
        mnVentana.add(miRestaurar);

        jMenuBar1.add(mnVentana);

        mnAdministracion = new javax.swing.JMenu("Administración");
        mnAdministracion.setMnemonic('d');
        javax.swing.JMenuItem miUsuarios = new javax.swing.JMenuItem("Usuarios");
        miUsuarios.addActionListener(evt -> abrirUsuarios());
        mnAdministracion.add(miUsuarios);
        javax.swing.JMenuItem miRespaldo = new javax.swing.JMenuItem("Respaldar base de datos");
        miRespaldo.addActionListener(evt -> respaldarBaseDatos());
        mnAdministracion.add(miRespaldo);
        jMenuBar1.add(mnAdministracion);

        mnReportes = new javax.swing.JMenu("Reportes");
        mnReportes.setMnemonic('R');
        agregarReporte("Resumen con gráficos", this::abrirResumen);
        mnReportes.addSeparator();
        agregarReporte("Inventario de productos", () -> Reportes.inventario(this));
        agregarReporte("Productos con existencia baja", () -> Reportes.existenciaBaja(this));
        agregarReporte("Ventas por período", () -> Reportes.ventasPorPeriodo(this));
        agregarReporte("Compras por proveedor", () -> Reportes.comprasPorProveedor(this));
        jMenuBar1.add(mnReportes);

        mnAyuda.setText("Ayuda");
        mnAyuda.setMnemonic('y');

        miAcercaDe.setText("Acerca de");
        miAcercaDe.setMnemonic('c');
        miAcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAcercaDeActionPerformed(evt);
            }
        });
        mnAyuda.add(miAcercaDe);

        jMenuBar1.add(mnAyuda);

        setJMenuBar(jMenuBar1);

        jDesktopPane.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDesktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jDesktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        pack();
        setSize(1000, 700);
    }// </editor-fold>//GEN-END:initComponents

    private void miNuevaFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miNuevaFacturaActionPerformed
        abrirFactura();
    }//GEN-LAST:event_miNuevaFacturaActionPerformed

    private void miSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSalirActionPerformed
        salir();
    }//GEN-LAST:event_miSalirActionPerformed

    private void miVerFacturasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miVerFacturasActionPerformed
        abrirListaFacturas();
    }//GEN-LAST:event_miVerFacturasActionPerformed

    private void miProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miProductosActionPerformed
        abrirProductos();
    }//GEN-LAST:event_miProductosActionPerformed

    private void miClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miClientesActionPerformed
        abrirClientes();
    }//GEN-LAST:event_miClientesActionPerformed

    private void miEmpleadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miEmpleadosActionPerformed
        abrirEmpleados();
    }//GEN-LAST:event_miEmpleadosActionPerformed

    private void miLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLimpiarActionPerformed
        limpiarFactura();
    }//GEN-LAST:event_miLimpiarActionPerformed

    private void miCascadaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCascadaActionPerformed
        cascada();
    }//GEN-LAST:event_miCascadaActionPerformed

    private void miMosaicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miMosaicoActionPerformed
        mosaico();
    }//GEN-LAST:event_miMosaicoActionPerformed

    private void miMinimizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miMinimizarActionPerformed
        minimizarTodo();
    }//GEN-LAST:event_miMinimizarActionPerformed

    private void miRestaurarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRestaurarActionPerformed
        restaurarTodo();
    }//GEN-LAST:event_miRestaurarActionPerformed

    private void miAcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAcercaDeActionPerformed
        acercaDe();
    }//GEN-LAST:event_miAcercaDeActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        // tambien al correr este archivo directamente se pide iniciar sesion
        com.josue.ventas.SistemaVentas.iniciar();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane jDesktopPane;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu mnArchivo;
    private javax.swing.JMenu mnAyuda;
    private javax.swing.JMenu mnCatalogos;
    private javax.swing.JMenu mnEdicion;
    private javax.swing.JMenu mnVentana;
    private javax.swing.JMenuItem miAcercaDe;
    private javax.swing.JMenuItem miCascada;
    private javax.swing.JMenuItem miClientes;
    private javax.swing.JMenuItem miEmpleados;
    private javax.swing.JMenuItem miLimpiar;
    private javax.swing.JMenuItem miMosaico;
    private javax.swing.JMenuItem miMinimizar;
    private javax.swing.JMenuItem miNuevaFactura;
    private javax.swing.JMenuItem miProductos;
    private javax.swing.JMenuItem miRestaurar;
    private javax.swing.JMenuItem miSalir;
    private javax.swing.JMenuItem miVerFacturas;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JMenuItem miCategorias;
    private javax.swing.JMenuItem miProveedores;
    private javax.swing.JMenu mnCompras;
    private javax.swing.JMenu mnAdministracion;
    private javax.swing.JMenu mnReportes;
}
