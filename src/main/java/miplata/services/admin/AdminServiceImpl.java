package miplata.services.admin;

import miplata.domain.*;
import miplata.domain.enums.AccountState;
import miplata.services.outputport.*;

import java.util.*;
import java.util.stream.Collectors;

public class AdminServiceImpl implements AdminService {

    private final ClientePersistencePort         clienteRepo;
    private final CuentaAhorrosPersistencePort   ahorrosRepo;
    private final CuentaCorrientePersistencePort corrienteRepo;
    private final TarjetaCreditoPersistencePort  tarjetaRepo;
    private final DeudaTCPersistencePort         deudaRepo;
    private final MovimientoPersistencePort      movimientoRepo;

    public AdminServiceImpl(ClientePersistencePort clienteRepo,
                            CuentaAhorrosPersistencePort ahorrosRepo,
                            CuentaCorrientePersistencePort corrienteRepo,
                            TarjetaCreditoPersistencePort tarjetaRepo,
                            DeudaTCPersistencePort deudaRepo,
                            MovimientoPersistencePort movimientoRepo) {
        this.clienteRepo    = clienteRepo;
        this.ahorrosRepo    = ahorrosRepo;
        this.corrienteRepo  = corrienteRepo;
        this.tarjetaRepo    = tarjetaRepo;
        this.deudaRepo      = deudaRepo;
        this.movimientoRepo = movimientoRepo;
    }

    // ── Clientes ─────────────────────────────────────────────────────────────

    @Override
    public List<Cliente> listarTodosLosClientes() {
        return clienteRepo.findAllClientes();
    }

    @Override
    public Cliente buscarClientePorUsuario(String usuario) {
        return clienteRepo.findByUsuario(usuario).orElse(null);
    }

    // ── Cuentas ──────────────────────────────────────────────────────────────

    @Override
    public ResumenCuentas getResumenCuentas(String usuario) {
        Cliente cliente = buscarClientePorUsuario(usuario);
        if (cliente == null) return null;

        CuentaAhorros ahorros     = ahorrosRepo.findByUsuario(usuario).orElse(null);
        CuentaCorriente corriente = corrienteRepo.findByUsuario(usuario).orElse(null);
        TarjetaCredito tc         = tarjetaRepo.findByUsuario(usuario).orElse(null);

        double saldoAhorros     = ahorros != null ? ahorros.getSaldo() : 0;
        boolean corrienteActiva = corriente != null && corriente.getEstado() == AccountState.ACTIVA;
        double saldoCorriente   = corrienteActiva ? corriente.getSaldo() : 0;
        boolean tcActiva        = tc != null && tc.getEstado() == AccountState.ACTIVA;
        double cupoTC           = tcActiva ? tc.getCupo() : 0;
        double cupoDisponible   = tcActiva ? tc.getCupoDisponible() : 0;
        String tierTC           = (tc != null && tc.getTier() != null) ? tc.getTier().getDescription() : "—";

        return new ResumenCuentas(
                usuario, cliente.getNombre(),
                saldoAhorros,
                corrienteActiva, saldoCorriente,
                tcActiva, cupoTC, cupoDisponible, tierTC
        );
    }

    // ── Tarjeta de crédito ───────────────────────────────────────────────────

    @Override
    public TarjetaCredito getTarjetaCredito(String usuario) {
        return tarjetaRepo.findByUsuario(usuario).orElse(null);
    }

    @Override
    public List<DeudaTC> getDeudas(String usuario) {
        return deudaRepo.findByUsuario(usuario);
    }

    @Override
    public List<TarjetaCredito> getAllTarjetas() {
        return clienteRepo.findAllClientes().stream()
                .map(c -> tarjetaRepo.findByUsuario(c.getUsuario()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeudaTC> getAllDeudas() {
        return clienteRepo.findAllClientes().stream()
                .flatMap(c -> deudaRepo.findByUsuario(c.getUsuario()).stream())
                .collect(Collectors.toList());
    }

    // ── Movimientos ──────────────────────────────────────────────────────────

    @Override
    public List<Movimiento> getMovimientosPorUsuario(String usuario) {
        return movimientoRepo.findByUsuario(usuario);
    }

    @Override
    public List<Movimiento> getTodosLosMovimientos() {
        return clienteRepo.findAllClientes().stream()
                .flatMap(c -> movimientoRepo.findByUsuario(c.getUsuario()).stream())
                .sorted(Comparator.comparing(
                        m -> m.getFecha() != null ? m.getFecha() : java.time.LocalDateTime.MIN,
                        Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());
    }

    // ── Reportes ─────────────────────────────────────────────────────────────

    @Override
    public double getTotalDineroEnAhorros() {
        return clienteRepo.findAllClientes().stream()
                .mapToDouble(c -> ahorrosRepo.findByUsuario(c.getUsuario())
                        .map(CuentaAhorros::getSaldo).orElse(0.0))
                .sum();
    }

    @Override
    public double getTotalDineroEnCorriente() {
        return clienteRepo.findAllClientes().stream()
                .mapToDouble(c -> corrienteRepo.findByUsuario(c.getUsuario())
                        .filter(cc -> cc.getEstado() == AccountState.ACTIVA)
                        .map(CuentaCorriente::getSaldo).orElse(0.0))
                .sum();
    }

    @Override
    public double getTotalDeudaTCPendiente() {
        return clienteRepo.findAllClientes().stream()
                .flatMap(c -> deudaRepo.findByUsuario(c.getUsuario()).stream())
                .filter(d -> !d.isPagada())
                .mapToDouble(DeudaTC::getSaldoPendiente)
                .sum();
    }

    @Override
    public long getClientesConTCActiva() {
        return clienteRepo.findAllClientes().stream()
                .filter(c -> tarjetaRepo.findByUsuario(c.getUsuario())
                        .map(t -> t.getEstado() == AccountState.ACTIVA)
                        .orElse(false))
                .count();
    }

    @Override
    public long getClientesConCorrienteActiva() {
        return clienteRepo.findAllClientes().stream()
                .filter(c -> corrienteRepo.findByUsuario(c.getUsuario())
                        .map(cc -> cc.getEstado() == AccountState.ACTIVA)
                        .orElse(false))
                .count();
    }

    @Override
    public Map<String, Long> getDistribucionOperaciones() {
        return getTodosLosMovimientos().stream()
                .collect(Collectors.groupingBy(
                        Movimiento::getTipoOperacion,
                        Collectors.counting()
                ));
    }

    @Override
    public List<ResumenCuentas> getTopClientesPorSaldoAhorros(int n) {
        return clienteRepo.findAllClientes().stream()
                .map(c -> getResumenCuentas(c.getUsuario()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(ResumenCuentas::getSaldoAhorros).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }
}