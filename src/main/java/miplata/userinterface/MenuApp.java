package miplata.userinterface;

import miplata.domain.Cliente;
import miplata.repository.ClienteRepository;
import miplata.services.ClienteService;
import miplata.utils.FormValidation;
import miplata.view.ClienteView;
import miplata.view.CuentaView;
import miplata.view.admin.AdminView;

import java.util.Optional;
import java.util.Scanner;

/**
 * Menú principal de la aplicación — orquesta los flujos de usuario y admin.
 */
public class MenuApp {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    private final ClienteView clienteView;
    private final CuentaView cuentaView;
    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final AdminView adminView;
    private final Scanner sc = new Scanner(System.in);

    public MenuApp(ClienteView clienteView, CuentaView cuentaView,
                   ClienteService clienteService, ClienteRepository clienteRepository,
                   AdminView adminView) {
        this.clienteView = clienteView;
        this.cuentaView = cuentaView;
        this.clienteService = clienteService;
        this.clienteRepository = clienteRepository;
        this.adminView = adminView;
    }

    public void showMainMenu() {
        System.out.println("╔═══════════════════════════════════╗");
        System.out.println("║   Bienvenido a Mi Plata 2.0       ║");
        System.out.println("║   Tu banco digital.               ║");
        System.out.println("╚═══════════════════════════════════╝");

        boolean running = true;
        while (running) {
            System.out.println("\n  1. Registrarse");
            System.out.println("  2. Iniciar Sesión");
            System.out.println("  3. Panel Administrador");
            System.out.println("  4. Salir");
            int opcion = FormValidation.validateInt("Seleccione una opción");

            switch (opcion) {
                case 1 -> clienteView.registrarCliente();
                case 2 -> iniciarSesion();
                case 3 -> iniciarSesionAdmin();
                case 4 -> {
                    System.out.println("¡Hasta pronto!");
                    running = false;
                }
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    // ── Login cliente ────────────────────────────────────────────────────────

    private void iniciarSesion() {
        int intentos = 0;

        while (intentos < 3) {
            String usuario = FormValidation.validateString("Usuario");
            String password = FormValidation.validateString("Contraseña");
            Optional<Cliente> clienteOpt = clienteService.login(usuario, password);

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                System.out.println("\n¡Bienvenido, " + cliente.getNombre() + "! (@" + cliente.getUsuario() + ")");
                showPanelCajero(cliente.getUsuario());
                return;
            }

            intentos++;
            int restantes = 3 - intentos;
            if (restantes > 0) {
                System.out.println("Usuario o contraseña incorrectos. Te quedan " + restantes + " intento(s).");
            }
        }

        System.out.println("\n Demasiados intentos fallidos. El sistema se cerrará por seguridad.");
        System.exit(0);
    }

    // ── Login admin ──────────────────────────────────────────────────────────

    private void iniciarSesionAdmin() {
        System.out.println("\n  === Acceso Administrador ===");
        int intentos = 0;

        while (intentos < 3) {
            String usuario = FormValidation.validateString("Usuario admin");
            String password = FormValidation.validateString("Contraseña admin");

            if (ADMIN_USER.equals(usuario) && ADMIN_PASS.equals(password)) {
                System.out.println("  Acceso concedido. Bienvenido, Administrador.");
                adminView.mostrarMenuAdmin();
                return;
            }

            intentos++;
            int restantes = 3 - intentos;
            if (restantes > 0) {
                System.out.println("  Credenciales incorrectas. Intentos restantes: " + restantes);
            }
        }

        System.out.println("  Acceso denegado. Demasiados intentos fallidos.");
    }

    // ── Panel cajero (cliente) ───────────────────────────────────────────────

    private void showPanelCajero(String usuario) {
        boolean sesionActiva = true;
        while (sesionActiva) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║        Panel de Cliente       ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.println("  1.  Ver saldo");
            System.out.println("  2.  Consignar dinero");
            System.out.println("  3.  Retirar dinero");
            System.out.println("  4.  Transferir a otro usuario");
            System.out.println("  5.  Trasladar entre mis cuentas");
            System.out.println("  6.  Ver movimientos");
            System.out.println("  7.  Activar Cuenta Corriente");
            System.out.println("  8.  Tarjeta de Crédito");
            System.out.println("  9.  Mi Perfil");
            System.out.println("  0.  Cerrar Sesión");

            int opcion = FormValidation.validateInt("Opción");

            switch (opcion) {
                case 1 -> cuentaView.verSaldo(usuario);
                case 2 -> cuentaView.depositar(usuario);
                case 3 -> cuentaView.retirar(usuario);
                case 4 -> cuentaView.transferir(usuario);
                case 5 -> cuentaView.trasladarInterno(usuario);
                case 6 -> cuentaView.verMovimientos(usuario);
                case 7 -> cuentaView.activarCuentaCorriente(usuario);
                case 8 -> menuTarjetaCredito(usuario);
                case 9 -> clienteView.actualizarCliente(usuario);
                case 0 -> {
                    System.out.println("Sesión cerrada correctamente.");
                    sesionActiva = false;
                }
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void menuTarjetaCredito(String usuario) {
        boolean tcActiva = cuentaView.tieneTarjetaActiva(usuario);

        if (!tcActiva) {
            System.out.println("\nNo tienes una tarjeta de crédito activa.");
            System.out.println("  1. Solicitar tarjeta   2. Volver");
            int op = FormValidation.validateInt("Opción");
            if (op == 1) cuentaView.activarTarjetaCredito(usuario);
            return;
        }

        boolean enMenuTC = true;
        while (enMenuTC) {
            System.out.println("\n=== Tarjeta de Crédito ===");
            System.out.println("  1. Ver saldo y cupo");
            System.out.println("  2. Realizar compra");
            System.out.println("  3. Pagar cuotas");
            System.out.println("  4. Ver movimientos TC");
            System.out.println("  5. Volver al menú principal");

            int opcion = FormValidation.validateInt("Opción");
            switch (opcion) {
                case 1 -> { cuentaView.verSaldoTC(usuario); FormValidation.pausar(); }
                case 2 -> cuentaView.realizarCompra(usuario);
                case 3 -> cuentaView.pagarCuotaTC(usuario);
                case 4 -> { cuentaView.verMovimientosTC(usuario); FormValidation.pausar(); }
                case 5 -> enMenuTC = false;
                default -> System.out.println("Opción no válida.");
            }
        }
    }
}