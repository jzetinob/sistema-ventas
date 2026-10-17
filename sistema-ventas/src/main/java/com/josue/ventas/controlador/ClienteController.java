/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.josue.ventas.controlador;

import com.josue.ventas.dao.ClienteDAO;
import com.josue.ventas.dao.ClienteDAOSQLite;
import com.josue.ventas.dao.TelefonoDAO;
import com.josue.ventas.dao.TelefonoDAOSQLite;
import com.josue.ventas.modelo.Cliente;
import com.josue.ventas.modelo.Telefono;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author josue zetino
 */
public class ClienteController {

    ClienteDAO dao;
    TelefonoDAO telefonoDao;

    public ClienteController() {
        dao = ClienteDAOSQLite.getInstancia();
        telefonoDao = TelefonoDAOSQLite.getInstancia();
    }

    /** Guarda el cliente y sus telefonos. Devuelve null si todo salio bien, o el mensaje de error. */
    public String Guardar(Cliente cliente) {
        String error = validarTelefonos(cliente.getTelefonos());
        if (error != null) {
            return error;
        }
        cliente.setTelefono(telefonoPrincipal(cliente));
        dao.guardar(cliente);
        if (cliente.getId() == 0 || !telefonoDao.reemplazar(cliente.getId(), cliente.getTelefonos())) {
            return "No se pudieron guardar los teléfonos del cliente.";
        }
        return null;
    }

    public List<Cliente> GetClientes() {
        return dao.listar();
    }

    /** Actualiza el cliente y reemplaza su lista de telefonos. Devuelve null o el mensaje de error. */
    public String Actualizar(Cliente cliente) {
        String error = validarTelefonos(cliente.getTelefonos());
        if (error != null) {
            return error;
        }
        cliente.setTelefono(telefonoPrincipal(cliente));
        dao.actualizar(cliente);
        if (!telefonoDao.reemplazar(cliente.getId(), cliente.getTelefonos())) {
            return "No se pudieron guardar los teléfonos del cliente.";
        }
        return null;
    }

    public List<Telefono> GetTelefonos(int clienteId) {
        return telefonoDao.listarPorCliente(clienteId);
    }

    public void Eliminar(int id) {
        dao.eliminar(id);
    }

    public boolean ExisteNit(String nit) {
        return dao.existeNit(nit);
    }

    /** Reglas de negocio: cada telefono con 8 a 15 digitos, con tipo y sin repetirse. */
    private String validarTelefonos(List<Telefono> telefonos) {
        Set<String> numeros = new HashSet<>();
        for (Telefono telefono : telefonos) {
            String numero = telefono.getNumero() == null ? "" : telefono.getNumero().trim();
            String soloDigitos = numero.replaceAll("[+\\- ]", "");
            if (!soloDigitos.matches("\\d{8,15}")) {
                return "El teléfono \"" + numero + "\" no es válido: debe tener de 8 a 15 dígitos.";
            }
            if (telefono.getTipo() == null) {
                return "Seleccione el tipo del teléfono " + numero + ".";
            }
            if (!numeros.add(soloDigitos)) {
                return "El teléfono " + numero + " está repetido.";
            }
            telefono.setNumero(numero);
        }
        return null;
    }

    /** El primer telefono se copia a la columna clientes.telefono, que usan la app movil y los reportes. */
    private String telefonoPrincipal(Cliente cliente) {
        List<Telefono> telefonos = cliente.getTelefonos();
        return telefonos.isEmpty() ? "" : telefonos.get(0).getNumero();
    }
}
