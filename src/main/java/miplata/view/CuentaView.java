package miplata.view;

import miplata.domain.DeudaTC;
import miplata.domain.TarjetaCredito;
import miplata.domain.enums.CreditCardTier;
import miplata.repository.ClienteRepository;
import miplata.services.CreditCardTierSelector;
import miplata.services.CuentaService;
import miplata.utils.FormValidation;

import java.util.List;

public class CuentaView {

    private final CuentaService cuentaService;
    private final ClienteRepository clienteRepository;

    public CuentaView(CuentaService cuentaService, ClienteRepository clienteRepository) {
        this.cuentaService = cuentaService;
        this.clienteRepository = clienteRepository;
    }

    // ── Saldo ────────────────────────────────────────────────────────────────

    public void verSaldo(String usuario) {
        cuentaService.verSaldo(usuario);
        FormValidation.pausar();
    }

    // ── Depósito ─────────────────────────────────────────────────────────────

    public void depositar(String usuario) {
        // ERROR #3: mostrar corriente solo si está activa
        boolean corrienteActiva = clienteRepository.findCuentaCorriente(usuario)
                .map(c -> c.isActiva()).orElse(false);

        int opcion;
        if (corrienteActiva) {
            System.out.println("¿En qué cuenta desea depositar?  1. Ahorros  2. Corriente");
            // ERROR #4: usar validateIntRange para forzar opción válida
            opcion = FormValidation.validateIntRange("Opción", 1, 2);
        } else {
            System.out.println("Depositando en Cuenta de Ahorros.");
            opcion = 1;
        }

        String tipo = (opcion == 2) ? "corriente" : "ahorros";
        double monto = FormValidation.validateDouble("Monto a depositar");
        System.out.println(cuentaService.depositar(usuario, tipo, monto));
        FormValidation.pausar();
    }

    // ── Retiro ───────────────────────────────────────────────────────────────

    public void retirar(String usuario) {
        // ERROR #3: mostrar corriente solo si está activa
        boolean corrienteActiva = clienteRepository.findCuentaCorriente(usuario)
                .map(c -> c.isActiva()).orElse(false);

        int opcion;
        if (corrienteActiva) {
            System.out.println("¿De qué cuenta desea retirar?  1. Ahorros  2. Corriente");
            // ERROR #4: usar validateIntRange para forzar opción válida
            opcion = FormValidation.validateIntRange("Opción", 1, 2);
        } else {
            System.out.println("Retirando de Cuenta de Ahorros.");
            opcion = 1;
        }

        String tipo = (opcion == 2) ? "corriente" : "ahorros";
        double monto = FormValidation.validateDouble("Monto a retirar");
        System.out.println(cuentaService.retirar(usuario, tipo, monto));
        FormValidation.pausar();
    }

    // ── Transferir ───────────────────────────────────────────────────────────

    public void transferir(String usuarioOrigen) {
        // ERROR #3: mostrar corriente solo si está activa
        boolean corrienteActiva = clienteRepository.findCuentaCorriente(usuarioOrigen)
                .map(c -> c.isActiva()).orElse(false);

        int opcion;
        if (corrienteActiva) {
            System.out.println("¿Desde qué cuenta?  1. Ahorros  2. Corriente");
            // ERROR #4: usar validateIntRange para forzar opción válida
            opcion = FormValidation.validateIntRange("Opción", 1, 2);
        } else {
            System.out.println("Transfiriendo desde Cuenta de Ahorros.");
            opcion = 1;
        }

        String tipo = (opcion == 2) ? "corriente" : "ahorros";
        String destinatario = FormValidation.validateString("Usuario destinatario");
        double monto = FormValidation.validateDouble("Monto a transferir");
        System.out.println(cuentaService.transferir(usuarioOrigen, tipo, destinatario, monto));
        FormValidation.pausar();
    }

    // ── Traslado interno ─────────────────────────────────────────────────────

    public void trasladarInterno(String usuario) {
        System.out.println("Desde:  1. Ahorros  2. Corriente");
        int opcionOrigen = FormValidation.validateIntRange("Opción", 1, 2);
        System.out.println("Hacia:  1. Ahorros  2. Corriente");
        int opcionDestino = FormValidation.validateIntRange("Opción", 1, 2);
        String origen = (opcionOrigen == 2) ? "corriente" : "ahorros";
        String destino = (opcionDestino == 2) ? "corriente" : "ahorros";
        double monto = FormValidation.validateDouble("Monto a trasladar");
        System.out.println(cuentaService.trasladarInterno(usuario, origen, destino, monto));
        FormValidation.pausar();
    }

    // ── Movimientos ──────────────────────────────────────────────────────────

    public void verMovimientos(String usuario) {
        cuentaService.verMovimientos(usuario);
        FormValidation.pausar();
    }

    // ── Cuenta Corriente ─────────────────────────────────────────────────────

    public void activarCuentaCorriente(String usuario) {
        // ERROR #6: verificar si ya está activa antes de pedir monto
        boolean yaActiva = clienteRepository.findCuentaCorriente(usuario)
                .map(c -> c.isActiva()).orElse(false);
        if (yaActiva) {
            System.out.println("  Tu cuenta corriente ya se encuentra activa.");
            FormValidation.pausar();
            return;
        }
        double monto = FormValidation.validateDouble("Monto a trasladar desde Cuenta de Ahorros para activar");
        System.out.println(cuentaService.activarCuentaCorriente(usuario, monto));
        FormValidation.pausar();
    }

    // ── Tarjeta de Crédito ───────────────────────────────────────────────────

    public void activarTarjetaCredito(String usuario) {
        CreditCardTier tier = CreditCardTierSelector.selectTier();
        System.out.println(cuentaService.activarTarjetaCredito(usuario, tier));
        FormValidation.pausar();
    }

    public void realizarCompra(String usuario) {
        double monto = FormValidation.validateDouble("Valor de la compra");

        // ERROR #11: verificar cupo antes de pedir cuotas
        TarjetaCredito tc = clienteRepository.findTarjetaCredito(usuario).orElse(null);
        if (tc == null || monto > tc.getCupoDisponible()) {
            System.out.println("  Compra no aprobada. Cupo disponible insuficiente" +
                    (tc != null ? ": $" + tc.formatPesos(tc.getCupoDisponible()) : "."));
            FormValidation.pausar();
            return;
        }

        // ERROR #7: usar validateIntRange para forzar opción de cuotas válida
        System.out.println("Cuotas disponibles:");
        System.out.println("  1. 1 cuota (sin interés)   2. 2 cuotas (sin interés)");
        System.out.println("  3. 3 cuotas (1.9%/mes)     4. 6 cuotas (1.9%/mes)");
        System.out.println("  5. 7 cuotas (2.3%/mes)     6. 12 cuotas (2.3%/mes)   7. 24 cuotas (2.3%/mes)");
        int opcion = FormValidation.validateIntRange("Opción", 1, 7);
        int[] mapaCuotas = {1, 2, 3, 6, 7, 12, 24};
        int cuotas = mapaCuotas[opcion - 1];
        System.out.println(cuentaService.realizarCompra(usuario, monto, cuotas));
        FormValidation.pausar();
    }

    public void pagarCuotaTC(String usuario) {
        TarjetaCredito tc = clienteRepository.findTarjetaCredito(usuario).orElse(null);
        if (tc == null || tc.getDeudasActivas().isEmpty()) {
            System.out.println("No tienes deudas activas.");
            return;
        }

        System.out.println("\n=== Deudas Activas ===");
        List<DeudaTC> todasLasDeudas = tc.getDeudas();
        List<DeudaTC> activas = tc.getDeudasActivas();

        for (int i = 0; i < activas.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + activas.get(i));
        }

        // ERROR #9: bucle hasta elegir un número de deuda válido
        int seleccion;
        while (true) {
            seleccion = FormValidation.validateInt("Número de la deuda a pagar") - 1;
            if (seleccion >= 0 && seleccion < activas.size()) break;
            System.out.println("  Error: número de deuda inválido. Elige entre 1 y " + activas.size() + ".");
        }

        int indexReal = todasLasDeudas.indexOf(activas.get(seleccion));

        // ERROR #10: usar validateIntRange para forzar opción de cuenta válida
        System.out.println("¿Pagar desde?  1. Ahorros  2. Corriente");
        int opcionCuenta = FormValidation.validateIntRange("Opción", 1, 2);
        String cuentaOrigen = (opcionCuenta == 2) ? "corriente" : "ahorros";

        // ERROR #10: usar validateIntRange para forzar opción de tipo de pago válida
        System.out.println("¿Tipo de pago?  1. Pagar una cuota  2. Pagar total");
        int tipoPago = FormValidation.validateIntRange("Opción", 1, 2);

        if (tipoPago == 2) {
            System.out.println(cuentaService.pagarTotal(usuario, indexReal, cuentaOrigen));
        } else {
            System.out.println(cuentaService.pagarCuota(usuario, indexReal, cuentaOrigen));
        }
        FormValidation.pausar();
    }

    public void verMovimientosTC(String usuario) {
        cuentaService.verMovimientosTC(usuario);
    }

    public void verSaldoTC(String usuario) {
        cuentaService.verSaldoTC(usuario);
    }

    public boolean tieneTarjetaActiva(String usuario) {
        return cuentaService.tieneTarjetaActiva(usuario);
    }
}