package miplata.services.admin;

import miplata.domain.enums.AccountState;

/**
 * DTO de solo lectura con el resumen de todas las cuentas de un cliente.
 * Usado en reportes y listados del panel de administración.
 */
public class ResumenCuentas {

    private final String usuario;
    private final String nombre;

    private final double saldoAhorros;

    private final boolean corrienteActiva;
    private final double saldoCorriente;

    private final boolean tcActiva;
    private final double cupoTC;
    private final double cupoDisponibleTC;
    private final String tierTC;

    public ResumenCuentas(String usuario, String nombre,
                          double saldoAhorros,
                          boolean corrienteActiva, double saldoCorriente,
                          boolean tcActiva, double cupoTC,
                          double cupoDisponibleTC, String tierTC) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.saldoAhorros = saldoAhorros;
        this.corrienteActiva = corrienteActiva;
        this.saldoCorriente = saldoCorriente;
        this.tcActiva = tcActiva;
        this.cupoTC = cupoTC;
        this.cupoDisponibleTC = cupoDisponibleTC;
        this.tierTC = tierTC;
    }

    public String getUsuario()          { return usuario; }
    public String getNombre()           { return nombre; }
    public double getSaldoAhorros()     { return saldoAhorros; }
    public boolean isCorrienteActiva()  { return corrienteActiva; }
    public double getSaldoCorriente()   { return saldoCorriente; }
    public boolean isTcActiva()         { return tcActiva; }
    public double getCupoTC()           { return cupoTC; }
    public double getCupoDisponibleTC() { return cupoDisponibleTC; }
    public String getTierTC()           { return tierTC; }

    private String fmt(double valor) {
        return String.format("%,.0f", valor).replace(",", ".");
    }

    @Override
    public String toString() {
        return String.format(
                "  @%-15s | %-25s | Ahorros: $%-12s | Corriente: %-8s | TC: %-8s",
                usuario, nombre,
                fmt(saldoAhorros),
                corrienteActiva ? "$" + fmt(saldoCorriente) : "Inactiva",
                tcActiva ? tierTC : "Inactiva"
        );
    }
}