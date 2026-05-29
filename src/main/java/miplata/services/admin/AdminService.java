package miplata.services.admin;

import miplata.domain.*;

import java.util.List;
import java.util.Map;

/**
 * Servicio de administración — consultas globales y reportes del sistema.
 * Solo lectura: no modifica datos, solo los agrega y presenta.
 */
public interface AdminService {

    // ── Clientes ─────────────────────────────────────────────────────────────

    List<Cliente> listarTodosLosClientes();
    Cliente buscarClientePorUsuario(String usuario);

    // ── Cuentas ──────────────────────────────────────────────────────────────

    ResumenCuentas getResumenCuentas(String usuario);

    // ── Tarjeta de crédito ───────────────────────────────────────────────────

    TarjetaCredito getTarjetaCredito(String usuario);
    List<DeudaTC> getDeudas(String usuario);

    /** Devuelve la tarjeta de crédito de todos los clientes que tienen una. */
    List<TarjetaCredito> getAllTarjetas();

    /** Devuelve todas las deudas de todos los clientes. */
    List<DeudaTC> getAllDeudas();

    // ── Movimientos ──────────────────────────────────────────────────────────

    List<Movimiento> getMovimientosPorUsuario(String usuario);
    List<Movimiento> getTodosLosMovimientos();

    // ── Reportes ─────────────────────────────────────────────────────────────

    double getTotalDineroEnAhorros();
    double getTotalDineroEnCorriente();
    double getTotalDeudaTCPendiente();
    long getClientesConTCActiva();
    long getClientesConCorrienteActiva();
    Map<String, Long> getDistribucionOperaciones();
    List<ResumenCuentas> getTopClientesPorSaldoAhorros(int n);
}