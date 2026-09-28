package com.josue.ventas.movil.modelo;

import java.util.List;

/** Datos del tablero: indicadores del dia, ventas de la semana, lo mas vendido y lo que se esta agotando. */
public class Resumen {

    /** Total vendido en un dia (una barra del grafico). */
    public static class VentaDia {
        private final String fecha;
        private final double total;

        public VentaDia(String fecha, double total) {
            this.fecha = fecha;
            this.total = total;
        }

        public String getFecha() {
            return fecha;
        }

        public double getTotal() {
            return total;
        }
    }

    /** Un producto del top de ventas (una porcion de la dona). */
    public static class ProductoVendido {
        private final String producto;
        private final int cantidad;
        private final double total;

        public ProductoVendido(String producto, int cantidad, double total) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.total = total;
        }

        public String getProducto() {
            return producto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getTotal() {
            return total;
        }
    }

    private final double ventasHoy;
    private final int facturasHoy;
    private final double ventasMes;
    private final List<VentaDia> ventasSemana;
    private final List<ProductoVendido> masVendidos;
    private final List<Producto> existenciaBaja;

    public Resumen(double ventasHoy, int facturasHoy, double ventasMes, List<VentaDia> ventasSemana,
            List<ProductoVendido> masVendidos, List<Producto> existenciaBaja) {
        this.ventasHoy = ventasHoy;
        this.facturasHoy = facturasHoy;
        this.ventasMes = ventasMes;
        this.ventasSemana = ventasSemana;
        this.masVendidos = masVendidos;
        this.existenciaBaja = existenciaBaja;
    }

    public double getVentasHoy() {
        return ventasHoy;
    }

    public int getFacturasHoy() {
        return facturasHoy;
    }

    public double getVentasMes() {
        return ventasMes;
    }

    public List<VentaDia> getVentasSemana() {
        return ventasSemana;
    }

    public List<ProductoVendido> getMasVendidos() {
        return masVendidos;
    }

    public List<Producto> getExistenciaBaja() {
        return existenciaBaja;
    }
}
