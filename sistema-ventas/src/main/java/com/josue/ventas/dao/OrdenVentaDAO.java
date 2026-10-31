package com.josue.ventas.dao;

import com.josue.ventas.modelo.OrdenVenta;
import com.josue.ventas.modelo.OrdenVentaDetalle;
import java.util.List;

/**
 * Consulta de las ordenes de venta que llegan desde la app movil y cambio
 * de su estado. Las ordenes se crean por la API REST (app_registrar_orden_venta).
 *
 * @author josue zetino
 */
public interface OrdenVentaDAO {

    List<OrdenVenta> listar();

    List<OrdenVentaDetalle> listarDetalles(int ordenId);

    boolean cambiarEstado(int id, OrdenVenta.Estado nuevo);
}
