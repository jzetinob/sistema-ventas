package com.josue.ventas.dao;

import com.josue.ventas.modelo.Telefono;
import java.util.List;

/**
 * Operaciones para guardar y consultar los telefonos de un cliente.
 *
 * @author josue zetino
 */
public interface TelefonoDAO {

    List<Telefono> listarPorCliente(int clienteId);

    boolean reemplazar(int clienteId, List<Telefono> telefonos);
}
