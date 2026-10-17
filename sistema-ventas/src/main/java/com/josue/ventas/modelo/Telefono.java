package com.josue.ventas.modelo;

/**
 * Telefono de un cliente. Un cliente tiene uno o varios telefonos
 * (relacion 1..N): cada telefono guarda el id del cliente al que pertenece.
 *
 * @author josue zetino
 */
public class Telefono {

    public enum Tipo {
        PERSONAL, TRABAJO, CASA, OTRO
    }

    private int id;
    private int clienteId;
    private String numero;
    private Tipo tipo;

    public Telefono() {
    }

    public Telefono(String numero, Tipo tipo) {
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
}
