/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.UsuarioDAO;
import com.josue.ventas.dao.UsuarioDAOSQLite;
import com.josue.ventas.modelo.Usuario;
import java.util.Arrays;
import java.util.List;

/**
 * Reglas de negocio de los usuarios: inicio de sesion, altas con
 * contrasena cifrada y protecciones para no dejar el sistema sin
 * administrador. Los metodos que validan devuelven el mensaje de error,
 * o null si todo salio bien.
 *
 * @author josue zetino
 */
public class UsuarioController {

    UsuarioDAO dao;

    public UsuarioController() {
        dao = UsuarioDAOSQLite.getInstancia();
    }

    public boolean HayUsuarios() {
        return dao.contar() > 0;
    }

    /**
     * Verifica usuario y contrasena. Si son correctos, abre la sesion.
     * El mensaje de error es el mismo para usuario inexistente y contrasena
     * incorrecta, para no revelar que usuarios existen.
     */
    public String IniciarSesion(String nombreUsuario, char[] clave) {
        try {
            Usuario usuario = dao.buscarPorUsuario(nombreUsuario.trim());
            if (usuario == null || !Contrasenas.verificar(clave, usuario.getSalt(), usuario.getClaveHash())) {
                return "Usuario o contraseña incorrectos.";
            }
            if (!usuario.isActivo()) {
                return "El usuario está desactivado. Consulte al administrador.";
            }
            if (Contrasenas.necesitaActualizar(usuario.getClaveHash())) {
                // la contrasena es correcta: se aprovecha para pasarla al formato actual (bcrypt)
                usuario.setClaveHash(Contrasenas.calcularHash(clave));
                usuario.setSalt(Contrasenas.SAL_BCRYPT);
                dao.cambiarClave(usuario.getId(), usuario.getClaveHash(), usuario.getSalt());
            }
            Sesion.getInstancia().iniciar(usuario);
            return null;
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    public List<Usuario> GetUsuarios() {
        return dao.listar();
    }

    public String Crear(String nombreUsuario, String nombre, char[] clave, Usuario.Rol rol) {
        try {
            String error = validarDatos(nombreUsuario, nombre, rol);
            if (error == null) {
                error = validarClave(clave);
            }
            if (error == null && dao.existeUsuario(nombreUsuario)) {
                error = "Ya existe un usuario con ese nombre de usuario.";
            }
            if (error != null) {
                return error;
            }
            Usuario usuario = new Usuario();
            usuario.setUsuario(nombreUsuario.trim());
            usuario.setNombre(nombre.trim());
            usuario.setRol(rol);
            usuario.setActivo(true);
            usuario.setSalt(Contrasenas.SAL_BCRYPT);
            usuario.setClaveHash(Contrasenas.calcularHash(clave));
            dao.guardar(usuario);
            return null;
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    public String Actualizar(Usuario usuario) {
        String error = validarDatos(usuario.getUsuario(), usuario.getNombre(), usuario.getRol());
        if (error != null) {
            return error;
        }
        Usuario existente = dao.buscarPorUsuario(usuario.getUsuario());
        if (existente != null && existente.getId() != usuario.getId()) {
            return "Ya existe un usuario con ese nombre de usuario.";
        }
        Usuario actual = buscarPorId(usuario.getId());
        boolean dejaDeSerAdminActivo = actual != null && actual.esAdministrador() && actual.isActivo()
                && (!usuario.esAdministrador() || !usuario.isActivo());
        if (dejaDeSerAdminActivo && dao.contarAdministradoresActivos() <= 1) {
            return "Debe quedar al menos un administrador activo.";
        }
        if (esUsuarioDeLaSesion(usuario.getId()) && !usuario.isActivo()) {
            return "No puede desactivar su propio usuario.";
        }
        usuario.setUsuario(usuario.getUsuario().trim());
        usuario.setNombre(usuario.getNombre().trim());
        dao.actualizar(usuario);
        return null;
    }

    public String CambiarClave(int idUsuario, char[] nuevaClave) {
        try {
            String error = validarClave(nuevaClave);
            if (error != null) {
                return error;
            }
            dao.cambiarClave(idUsuario, Contrasenas.calcularHash(nuevaClave), Contrasenas.SAL_BCRYPT);
            return null;
        } finally {
            Arrays.fill(nuevaClave, '\0');
        }
    }

    /** Cambio de contrasena del propio usuario: exige la contrasena actual. */
    public String CambiarMiClave(char[] claveActual, char[] nuevaClave) {
        Usuario usuario = Sesion.getInstancia().getUsuario();
        Usuario guardado = usuario != null ? dao.buscarPorUsuario(usuario.getUsuario()) : null;
        try {
            if (guardado == null || !Contrasenas.verificar(claveActual, guardado.getSalt(), guardado.getClaveHash())) {
                Arrays.fill(nuevaClave, '\0');
                return "La contraseña actual no es correcta.";
            }
        } finally {
            Arrays.fill(claveActual, '\0');
        }
        return CambiarClave(guardado.getId(), nuevaClave);
    }

    public String Eliminar(int id) {
        if (esUsuarioDeLaSesion(id)) {
            return "No puede eliminar su propio usuario.";
        }
        Usuario usuario = buscarPorId(id);
        if (usuario != null && usuario.esAdministrador() && usuario.isActivo()
                && dao.contarAdministradoresActivos() <= 1) {
            return "Debe quedar al menos un administrador activo.";
        }
        dao.eliminar(id);
        return null;
    }

    private Usuario buscarPorId(int id) {
        for (Usuario u : dao.listar()) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    private boolean esUsuarioDeLaSesion(int id) {
        Usuario actual = Sesion.getInstancia().getUsuario();
        return actual != null && actual.getId() == id;
    }

    private String validarDatos(String nombreUsuario, String nombre, Usuario.Rol rol) {
        if (nombreUsuario == null || !nombreUsuario.trim().matches("[A-Za-z0-9._-]{3,30}")) {
            return "El usuario debe tener de 3 a 30 caracteres: letras, números, punto, guion o guion bajo.";
        }
        if (nombre == null || nombre.isBlank()) {
            return "Escriba el nombre de la persona.";
        }
        if (rol == null) {
            return "Seleccione un rol.";
        }
        return null;
    }

    private String validarClave(char[] clave) {
        if (clave == null || clave.length < Contrasenas.LONGITUD_MINIMA) {
            return "La contraseña debe tener al menos " + Contrasenas.LONGITUD_MINIMA + " caracteres.";
        }
        return null;
    }
}
