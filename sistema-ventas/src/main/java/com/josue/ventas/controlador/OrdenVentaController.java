package com.josue.ventas.controlador;

import com.josue.ventas.dao.OrdenVentaDAO;
import com.josue.ventas.dao.OrdenVentaDAOSQLite;
import com.josue.ventas.modelo.OrdenVenta;
import com.josue.ventas.modelo.OrdenVentaDetalle;
import java.util.List;

/**
 * Ordenes de venta que llegan desde la app movil. El escritorio las consulta
 * y decide: confirmarlas o anularlas. Solo una orden PENDIENTE puede cambiar.
 *
 * @author josue zetino
 */
public class OrdenVentaController {

    OrdenVentaDAO dao;

    public OrdenVentaController() {
        dao = OrdenVentaDAOSQLite.getInstancia();
    }

    public List<OrdenVenta> GetOrdenes() {
        return dao.listar();
    }

    public List<OrdenVentaDetalle> GetDetalles(int ordenId) {
        return dao.listarDetalles(ordenId);
    }

    /** Devuelve null si se confirmo, o el mensaje de error. */
    public String Confirmar(OrdenVenta orden) {
        return cambiarEstado(orden, OrdenVenta.Estado.CONFIRMADA);
    }

    /** Devuelve null si se anulo, o el mensaje de error. */
    public String Anular(OrdenVenta orden) {
        return cambiarEstado(orden, OrdenVenta.Estado.ANULADA);
    }

    private String cambiarEstado(OrdenVenta orden, OrdenVenta.Estado nuevo) {
        if (orden == null) {
            return "Seleccione una orden de venta.";
        }
        if (!orden.estaPendiente()) {
            return "La orden " + orden.getNumeroOrden() + " ya está " + orden.getEstado().name().toLowerCase() + ".";
        }
        if (!dao.cambiarEstado(orden.getId(), nuevo)) {
            return "No se pudo cambiar la orden " + orden.getNumeroOrden() + ". Actualice la lista e intente de nuevo.";
        }
        orden.setEstado(nuevo);
        return null;
    }
}
