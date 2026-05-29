package miplata.view.admin;

import miplata.domain.*;
import miplata.services.admin.AdminService;
import miplata.services.admin.ResumenCuentas;
import miplata.utils.FormValidation;

import java.util.List;
import java.util.Map;

/**
 * Vista del panel de administración — consola.
 * Presenta menús, listados y reportes globales del sistema.
 * Solo lectura: nunca modifica datos.
 */
public class AdminView {

    private static final String SEP  = "─".repeat(80);
    private static final String SEP2 = "═".repeat(80);

    private final AdminService adminService;

    public AdminView(AdminService adminService) {
        this.adminService = adminService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Menú principal del panel admin
    // ══════════════════════════════════════════════════════════════════════════

    public void mostrarMenuAdmin() {
        boolean activo = true;
        while (activo) {
            System.out.println("\n" + SEP2);
            System.out.println("  PANEL DE ADMINISTRACIÓN — Mi Plata 2.0");
            System.out.println(SEP2);
            System.out.println("  1.  Clientes");
            System.out.println("  2.  Cuentas y saldos");
            System.out.println("  3.  Tarjetas de crédito y deudas");
            System.out.println("  4.  Movimientos");
            System.out.println("  0.  Salir del panel admin");
            System.out.println(SEP2);

            int opcion = FormValidation.validateInt("Opción");
            switch (opcion) {
                case 1 -> menuClientes();
                case 2 -> menuCuentas();
                case 3 -> menuTarjetas();
                case 4 -> menuMovimientos();
                case 0 -> activo = false;
                default -> System.out.println("  Opción no válida.");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 1. CLIENTES
    // ══════════════════════════════════════════════════════════════════════════

    private void menuClientes() {
        boolean activo = true;
        while (activo) {
            System.out.println("\n  --- Clientes ---");
            System.out.println("  1. Listar todos los clientes");
            System.out.println("  2. Buscar cliente por usuario");
            System.out.println("  0. Volver");

            int opcion = FormValidation.validateInt("Opción");
            switch (opcion) {
                case 1 -> listarTodosLosClientes();
                case 2 -> buscarCliente();
                case 0 -> activo = false;
                default -> System.out.println("  Opción no válida.");
            }
        }
    }

    private void listarTodosLosClientes() {
        List<Cliente> clientes = adminService.listarTodosLosClientes();
        System.out.println("\n" + SEP);
        System.out.printf("  %-15s | %-25s | %-14s | %-12s%n",
                "Usuario", "Nombre", "Identificación", "Celular");
        System.out.println(SEP);

        if (clientes.isEmpty()) {
            System.out.println("  No hay clientes registrados.");
        } else {
            clientes.forEach(c -> System.out.printf(
                    "  %-15s | %-25s | %-14s | %-12s%n",
                    "@" + c.getUsuario(), c.getNombre(),
                    c.getIdentificacion(), c.getCelular()
            ));
        }
        System.out.println(SEP);
        System.out.println("  Total: " + clientes.size() + " cliente(s)");
        FormValidation.pausar();
    }

    private void buscarCliente() {
        String usuario = FormValidation.validateString("Ingrese el usuario a buscar");
        Cliente c = adminService.buscarClientePorUsuario(usuario);
        System.out.println("\n" + SEP);
        if (c == null) {
            System.out.println("  Cliente '@" + usuario + "' no encontrado.");
        } else {
            System.out.println("  Usuario:        @" + c.getUsuario());
            System.out.println("  Nombre:         " + c.getNombre());
            System.out.println("  Identificación: " + c.getIdentificacion());
            System.out.println("  Celular:        " + c.getCelular());
        }
        System.out.println(SEP);
        FormValidation.pausar();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 2. CUENTAS Y SALDOS
    // ══════════════════════════════════════════════════════════════════════════

    private void menuCuentas() {
        boolean activo = true;
        while (activo) {
            System.out.println("\n  --- Cuentas y Saldos ---");
            System.out.println("  1. Ver resumen de cuentas de un cliente");
            System.out.println("  2. Listar resumen de todos los clientes");
            System.out.println("  0. Volver");

            int opcion = FormValidation.validateInt("Opción");
            switch (opcion) {
                case 1 -> verResumenCliente();
                case 2 -> listarResumenTodos();
                case 0 -> activo = false;
                default -> System.out.println("  Opción no válida.");
            }
        }
    }

    private void verResumenCliente() {
        String usuario = FormValidation.validateString("Usuario");
        ResumenCuentas r = adminService.getResumenCuentas(usuario);
        System.out.println("\n" + SEP);
        if (r == null) {
            System.out.println("  Cliente no encontrado.");
        } else {
            System.out.println("  Cliente:          @" + r.getUsuario() + " — " + r.getNombre());
            System.out.println(SEP);
            System.out.println("  CUENTA DE AHORROS");
            System.out.println("    Saldo:          $" + fmt(r.getSaldoAhorros()));
            System.out.println();
            System.out.println("  CUENTA CORRIENTE");
            System.out.println("    Estado:         " + (r.isCorrienteActiva() ? "Activa" : "Inactiva"));
            if (r.isCorrienteActiva())
                System.out.println("    Saldo:          $" + fmt(r.getSaldoCorriente()));
            System.out.println();
            System.out.println("  TARJETA DE CRÉDITO");
            System.out.println("    Estado:         " + (r.isTcActiva() ? "Activa" : "Inactiva"));
            if (r.isTcActiva()) {
                System.out.println("    Tier:           " + r.getTierTC());
                System.out.println("    Cupo total:     $" + fmt(r.getCupoTC()));
                System.out.println("    Cupo disponible:$" + fmt(r.getCupoDisponibleTC()));
                System.out.println("    Cupo usado:     $" + fmt(r.getCupoTC() - r.getCupoDisponibleTC()));
            }
        }
        System.out.println(SEP);
        FormValidation.pausar();
    }

    private void listarResumenTodos() {
        List<Cliente> clientes = adminService.listarTodosLosClientes();
        System.out.println("\n" + SEP);
        System.out.printf("  %-15s | %-20s | %-13s | %-12s | %-8s%n",
                "Usuario", "Nombre", "Ahorros", "Corriente", "TC");
        System.out.println(SEP);

        if (clientes.isEmpty()) {
            System.out.println("  Sin clientes registrados.");
        } else {
            clientes.forEach(c -> {
                ResumenCuentas r = adminService.getResumenCuentas(c.getUsuario());
                if (r != null) System.out.println(r);
            });
        }
        System.out.println(SEP);
        FormValidation.pausar();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 3. TARJETAS DE CRÉDITO Y DEUDAS
    // ══════════════════════════════════════════════════════════════════════════

    private void menuTarjetas() {
        boolean activo = true;
        while (activo) {
            System.out.println("\n  --- Tarjetas de Crédito y Deudas ---");
            System.out.println("  1. Ver tarjeta de un cliente");
            System.out.println("  2. Ver deudas de un cliente");
            System.out.println("  3. Listar todas las tarjetas");
            System.out.println("  4. Listar todas las deudas");
            System.out.println("  0. Volver");

            int opcion = FormValidation.validateInt("Opción");
            switch (opcion) {
                case 1 -> verTarjeta();
                case 2 -> verDeudas();
                case 3 -> listarTodasLasTarjetas();
                case 4 -> listarTodasLasDeudas();
                case 0 -> activo = false;
                default -> System.out.println("  Opción no válida.");
            }
        }
    }

    private void verTarjeta() {
        String usuario = FormValidation.validateString("Usuario");
        TarjetaCredito tc = adminService.getTarjetaCredito(usuario);
        System.out.println("\n" + SEP);
        if (tc == null) {
            System.out.println("  El cliente no tiene tarjeta de crédito registrada.");
        } else {
            System.out.println("  Tarjeta de: @" + usuario);
            System.out.println("  Estado:         " + tc.getEstado().name());
            System.out.println("  Tier:           " + tc.getTier().getDescription());
            System.out.println("  Cupo total:     $" + fmt(tc.getCupo()));
            System.out.println("  Cupo disponible:$" + fmt(tc.getCupoDisponible()));
            System.out.println("  Cupo usado:     $" + fmt(tc.getCupo() - tc.getCupoDisponible()));

            List<DeudaTC> deudas = adminService.getDeudas(usuario);
            long activas = deudas.stream().filter(d -> !d.isPagada()).count();
            System.out.println("  Deudas activas: " + activas + " / " + deudas.size() + " total");
        }
        System.out.println(SEP);
        FormValidation.pausar();
    }

    private void verDeudas() {
        String usuario = FormValidation.validateString("Usuario");
        List<DeudaTC> deudas = adminService.getDeudas(usuario);
        System.out.println("\n" + SEP);
        System.out.println("  Deudas TC de @" + usuario + ":");
        System.out.println(SEP);

        if (deudas.isEmpty()) {
            System.out.println("  No hay deudas registradas.");
        } else {
            for (int i = 0; i < deudas.size(); i++) {
                DeudaTC d = deudas.get(i);
                String estado = d.isPagada() ? "[PAGADA]" : "[ACTIVA] ";
                System.out.printf("  %d. %s %s%n", i + 1, estado, d);
            }
            double totalPendiente = deudas.stream()
                    .filter(d -> !d.isPagada())
                    .mapToDouble(DeudaTC::getSaldoPendiente).sum();
            System.out.println(SEP);
            System.out.println("  Total deuda pendiente: $" + fmt(totalPendiente));
        }
        System.out.println(SEP);
        FormValidation.pausar();
    }
    private void listarTodasLasTarjetas() {
        List<TarjetaCredito> tarjetas = adminService.getAllTarjetas();
        System.out.println("\n" + SEP);
        System.out.printf("  %-15s | %-12s | %-10s | %-13s | %-13s%n",
                "Usuario", "Estado", "Tier", "Cupo total", "Cupo disponible");
        System.out.println(SEP);

        if (tarjetas.isEmpty()) {
            System.out.println("  No hay tarjetas registradas.");
        } else {
            tarjetas.forEach(tc -> System.out.printf(
                    "  %-15s | %-12s | %-10s | $%-12s | $%-13s%n",
                    "@" + tc.getUsuario(),
                    tc.getEstado().name(),
                    tc.getTier().getDescription(),
                    fmt(tc.getCupo()),
                    fmt(tc.getCupoDisponible())
            ));
            long activas = tarjetas.stream()
                    .filter(tc -> tc.getEstado().name().equals("ACTIVA")).count();
            System.out.println(SEP);
            System.out.println("  Total: " + tarjetas.size() + " tarjeta(s) | Activas: " + activas);
        }
        FormValidation.pausar();
    }

    private void listarTodasLasDeudas() {
        List<Cliente> clientes = adminService.listarTodosLosClientes();
        System.out.println("\n" + SEP);
        System.out.println("  TODAS LAS DEUDAS DEL SISTEMA");
        System.out.println(SEP);

        boolean hayDeudas = false;
        for (Cliente c : clientes) {
            List<DeudaTC> deudas = adminService.getDeudas(c.getUsuario());
            if (deudas.isEmpty()) continue;

            hayDeudas = true;
            System.out.println("  @" + c.getUsuario() + " — " + c.getNombre());
            for (int i = 0; i < deudas.size(); i++) {
                DeudaTC d = deudas.get(i);
                String estado = d.isPagada() ? "[PAGADA] " : "[ACTIVA] ";
                System.out.printf("    %d. %s %s%n", i + 1, estado, d);
            }
            double pendiente = deudas.stream()
                    .filter(d -> !d.isPagada())
                    .mapToDouble(DeudaTC::getSaldoPendiente).sum();
            System.out.println("    Pendiente total: $" + fmt(pendiente));
            System.out.println();
        }

        if (!hayDeudas) System.out.println("  No hay deudas registradas en el sistema.");

        double grandTotal = adminService.getTotalDeudaTCPendiente();
        System.out.println(SEP);
        System.out.println("  DEUDA PENDIENTE TOTAL DEL SISTEMA: $" + fmt(grandTotal));
        FormValidation.pausar();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 4. MOVIMIENTOS
    // ══════════════════════════════════════════════════════════════════════════

    private void menuMovimientos() {
        boolean activo = true;
        while (activo) {
            System.out.println("\n  --- Movimientos ---");
            System.out.println("  1. Ver movimientos de un cliente");
            System.out.println("  2. Ver todos los movimientos del sistema");
            System.out.println("  0. Volver");

            int opcion = FormValidation.validateInt("Opción");
            switch (opcion) {
                case 1 -> verMovimientosCliente();
                case 2 -> verTodosLosMovimientos();
                case 0 -> activo = false;
                default -> System.out.println("  Opción no válida.");
            }
        }
    }

    private void verMovimientosCliente() {
        String usuario = FormValidation.validateString("Usuario");
        List<Movimiento> movimientos = adminService.getMovimientosPorUsuario(usuario);
        System.out.println("\n" + SEP);
        System.out.println("  Movimientos de @" + usuario + ":");
        System.out.println(SEP);

        if (movimientos.isEmpty()) {
            System.out.println("  Sin movimientos registrados.");
        } else {
            movimientos.forEach(m -> System.out.println("  " + m));
        }
        System.out.println(SEP);
        System.out.println("  Total: " + movimientos.size() + " movimiento(s)");
        FormValidation.pausar();
    }

    private void verTodosLosMovimientos() {
        List<Movimiento> movimientos = adminService.getTodosLosMovimientos();
        System.out.println("\n" + SEP);
        System.out.println("  Todos los movimientos del sistema (más recientes primero):");
        System.out.println(SEP);

        if (movimientos.isEmpty()) {
            System.out.println("  Sin movimientos en el sistema.");
        } else {
            int total = movimientos.size();
            int pagina = 0;
            int porPagina = 20;
            boolean viendo = true;

            while (viendo) {
                int desde = pagina * porPagina;
                int hasta = Math.min(desde + porPagina, total);
                for (int i = desde; i < hasta; i++) {
                    System.out.println("  " + movimientos.get(i));
                }
                System.out.println(SEP);
                System.out.println("  Mostrando " + (desde + 1) + "-" + hasta + " de " + total);

                if (hasta >= total) {
                    System.out.println("  (Fin de los movimientos)");
                    FormValidation.pausar();
                    viendo = false;
                } else {
                    System.out.println("  1. Ver más   0. Volver");
                    int op = FormValidation.validateInt("Opción");
                    if (op == 1) pagina++;
                    else viendo = false;
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String fmt(double valor) {
        return String.format("%,.0f", valor).replace(",", ".");
    }
}