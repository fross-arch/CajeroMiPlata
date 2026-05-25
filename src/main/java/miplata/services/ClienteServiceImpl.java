package miplata.services;

import miplata.domain.Cliente;
import miplata.domain.CuentaAhorros;
import miplata.services.outputport.CuentaAhorrosPersistencePort;
import miplata.services.outputport.CuentaCorrientePersistencePort;
import miplata.services.outputport.TarjetaCreditoPersistencePort;
import miplata.repository.ClienteRepository;
import miplata.services.outputport.ClientePersistencePort;
import miplata.utils.FormValidation;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import miplata.domain.DeudaTC;
import miplata.services.outputport.DeudaTCPersistencePort;
public class ClienteServiceImpl implements ClienteService {



    private final ClientePersistencePort clienteRepository;
    private final ClienteRepository clienteRepositoryMemoria;
    private final CuentaAhorrosPersistencePort cuentaAhorrosRepository;
    private final CuentaCorrientePersistencePort cuentaCorrienteRepository;
    private final TarjetaCreditoPersistencePort tarjetaCreditoRepository;
    private final DeudaTCPersistencePort deudaTCRepository;

    public ClienteServiceImpl(ClientePersistencePort clienteRepository,
                              ClienteRepository clienteRepositoryMemoria,
                              CuentaAhorrosPersistencePort cuentaAhorrosRepository,
                              CuentaCorrientePersistencePort cuentaCorrienteRepository,
                              TarjetaCreditoPersistencePort tarjetaCreditoRepository,
                              DeudaTCPersistencePort deudaTCRepository) {
        this.clienteRepository = clienteRepository;
        this.clienteRepositoryMemoria = clienteRepositoryMemoria;
        this.cuentaAhorrosRepository = cuentaAhorrosRepository;
        this.cuentaCorrienteRepository = cuentaCorrienteRepository;
        this.tarjetaCreditoRepository = tarjetaCreditoRepository;
        this.deudaTCRepository = deudaTCRepository;
    }
    @Override
    public Cliente registrarCliente(double saldoInicial) {
        Cliente cliente = new Cliente();

        System.out.println("=== Registro de Nuevo Cliente ===");
        while (true) {
            cliente.setUsuario(FormValidation.validateString("Ingrese un nombre de usuario"));
            if (!clienteRepository.existeUsuario(cliente.getUsuario())) break;
            System.out.println("  El usuario ya existe. Intente con otro nombre de usuario.");
        }
        cliente.setPassword(FormValidation.validateString("Ingrese una contraseña (mínimo 4 caracteres)"));
        cliente.setNombre(FormValidation.validateString("Ingrese su nombre completo"));
        cliente.setIdentificacion(FormValidation.validateNumerico("Ingrese su número de identificación"));
        cliente.setCelular(FormValidation.validateNumerico("Ingrese su número de celular"));


        Cliente guardado = clienteRepository.guardarCliente(cliente);

// Crear cuentas en memoria
        clienteRepositoryMemoria.guardarCliente(cliente);

// Asignar saldo inicial a cuenta de ahorros y guardar en BD
        clienteRepositoryMemoria.findCuentaAhorros(guardado.getUsuario())
                .ifPresent(c -> {
                    c.setSaldo(saldoInicial);
                    c.guardarMovimiento("Saldo inicial: $" + c.formatPesos(saldoInicial));
                    cuentaAhorrosRepository.guardarCuenta(c);
                });

// Guardar cuenta corriente en BD (inactiva)
        clienteRepositoryMemoria.findCuentaCorriente(guardado.getUsuario())
                .ifPresent(cuentaCorrienteRepository::guardarCuenta);

// Guardar tarjeta de crédito en BD (inactiva)
        clienteRepositoryMemoria.findTarjetaCredito(guardado.getUsuario())
                .ifPresent(tarjetaCreditoRepository::guardarTarjeta);
        System.out.println("Cliente registrado exitosamente. Bienvenido, " + cliente.getNombre() + "!");
        return guardado;
    }



    @Override
    public Optional<Cliente> login(String usuario, String password) {
        Optional<Cliente> clienteOpt = clienteRepository.findByUsuario(usuario)
                .filter(c -> c.verificarPassword(password));

        clienteOpt.ifPresent(c -> {
            if (!clienteRepositoryMemoria.existeUsuario(c.getUsuario())) {
                clienteRepositoryMemoria.guardarCliente(c);
            }

            // Cargar ahorros desde BD
            cuentaAhorrosRepository.findByUsuario(c.getUsuario())
                    .ifPresent(cuentaBD -> clienteRepositoryMemoria.findCuentaAhorros(c.getUsuario())
                            .ifPresent(cuentaMem -> cuentaMem.setSaldo(cuentaBD.getSaldo())));

            // Cargar cuenta corriente desde BD
            cuentaCorrienteRepository.findByUsuario(c.getUsuario())
                    .ifPresent(cuentaBD -> clienteRepositoryMemoria.findCuentaCorriente(c.getUsuario())
                            .ifPresent(cuentaMem -> {
                                cuentaMem.setSaldo(cuentaBD.getSaldo());
                                cuentaMem.setEstado(cuentaBD.getEstado());
                            }));

            // Cargar tarjeta desde BD
            tarjetaCreditoRepository.findByUsuario(c.getUsuario())
                    .ifPresent(tarjetaBD -> clienteRepositoryMemoria.findTarjetaCredito(c.getUsuario())
                            .ifPresent(tarjetaMem -> {
                                tarjetaMem.setCupo(tarjetaBD.getCupo());
                                tarjetaMem.setCupoDisponible(tarjetaBD.getCupoDisponible());
                                tarjetaMem.setEstado(tarjetaBD.getEstado());
                            }));

            // Cargar deudas TC desde BD
            List<DeudaTC> deudas = deudaTCRepository.findByUsuario(c.getUsuario());
            clienteRepositoryMemoria.findTarjetaCredito(c.getUsuario())
                    .ifPresent(tarjetaMem -> {
                        deudas.forEach(tarjetaMem::agregarDeuda);
                        double usado = deudas.stream()
                                .filter(d -> !d.isPagada())
                                .mapToDouble(DeudaTC::getCapital)
                                .sum();
                        tarjetaMem.setCupoDisponible(tarjetaMem.getCupo() - usado);
                    });
        });

        return clienteOpt;
    }

    @Override
    public Optional<Cliente> findByUsuario(String usuario) {
        return clienteRepository.findByUsuario(usuario);
    }

    @Override
    public Cliente actualizarCliente(String usuario) {
        Cliente cliente = clienteRepository.findByUsuario(usuario).orElse(null);
        if (cliente == null) {
            System.out.println("Cliente no encontrado.");
            return null;
        }

        System.out.println("\n=== Mi Perfil ===");
        System.out.println("  Usuario:        @" + cliente.getUsuario());
        System.out.println("  Nombre:         " + cliente.getNombre());
        System.out.println("  Identificación: " + cliente.getIdentificacion());
        System.out.println("  Celular:        " + cliente.getCelular());

        System.out.println("\n¿Qué desea actualizar?");
        System.out.println("1. Nombre  2. Identificación  3. Celular  4. Contraseña  5. Cancelar");
        int opcion = FormValidation.validateInt("Opción");

        switch (opcion) {
            case 1 -> cliente.setNombre(FormValidation.validateString("Nuevo nombre completo"));
            case 2 -> cliente.setIdentificacion(FormValidation.validateString("Nueva identificación"));
            case 3 -> cliente.setCelular(FormValidation.validateString("Nuevo celular"));
            case 4 -> {
                String actual = FormValidation.validateString("Contraseña actual");
                if (!cliente.verificarPassword(actual)) {
                    System.out.println("Contraseña actual incorrecta.");
                    return cliente;
                }
                String nueva = FormValidation.validateString("Nueva contraseña");
                cliente.setPassword(nueva);
            }
            case 5 -> { return cliente; }
            default -> { System.out.println("Opción no válida."); return cliente; }
        }

        clienteRepository.actualizarCliente(cliente);
        System.out.println("Perfil actualizado correctamente.");
        return cliente;
    }

    @Override
    public void eliminarCliente(String usuario) {
        boolean eliminado = clienteRepository.eliminarCliente(usuario);
        System.out.println(eliminado
                ? "Cliente eliminado correctamente."
                : "Cliente no encontrado.");
    }

    @Override
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAllClientes();
    }

    @Override
    public boolean existeUsuario(String usuario) {
        return clienteRepository.existeUsuario(usuario);
    }
}
