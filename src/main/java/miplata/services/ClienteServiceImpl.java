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

        // ERROR #1: usar validatePassword en lugar de validateString
        cliente.setPassword(FormValidation.validatePassword("Ingrese una contraseña (mínimo 4 caracteres)"));

        cliente.setNombre(FormValidation.validateString("Ingrese su nombre completo"));
        cliente.setIdentificacion(FormValidation.validateNumerico("Ingrese su número de identificación"));
        cliente.setCelular(FormValidation.validateNumerico("Ingrese su número de celular"));

        Cliente guardado = clienteRepository.guardarCliente(cliente);

        clienteRepositoryMemoria.guardarCliente(cliente);

        clienteRepositoryMemoria.findCuentaAhorros(guardado.getUsuario())
                .ifPresent(c -> {
                    c.setSaldo(saldoInicial);
                    c.guardarMovimiento("Saldo inicial: $" + c.formatPesos(saldoInicial));
                    cuentaAhorrosRepository.guardarCuenta(c);
                });

        clienteRepositoryMemoria.findCuentaCorriente(guardado.getUsuario())
                .ifPresent(cuentaCorrienteRepository::guardarCuenta);

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

            cuentaAhorrosRepository.findByUsuario(c.getUsuario())
                    .ifPresent(cuentaBD -> clienteRepositoryMemoria.findCuentaAhorros(c.getUsuario())
                            .ifPresent(cuentaMem -> cuentaMem.setSaldo(cuentaBD.getSaldo())));

            cuentaCorrienteRepository.findByUsuario(c.getUsuario())
                    .ifPresent(cuentaBD -> clienteRepositoryMemoria.findCuentaCorriente(c.getUsuario())
                            .ifPresent(cuentaMem -> {
                                cuentaMem.setSaldo(cuentaBD.getSaldo());
                                cuentaMem.setEstado(cuentaBD.getEstado());
                            }));

            tarjetaCreditoRepository.findByUsuario(c.getUsuario())
                    .ifPresent(tarjetaBD -> clienteRepositoryMemoria.findTarjetaCredito(c.getUsuario())
                            .ifPresent(tarjetaMem -> {
                                tarjetaMem.setCupo(tarjetaBD.getCupo());
                                tarjetaMem.setCupoDisponible(tarjetaBD.getCupoDisponible());
                                tarjetaMem.setEstado(tarjetaBD.getEstado());
                            }));

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

        // ERROR #5: bucle para quedarse en perfil hasta que el usuario elija Cancelar
        boolean enPerfil = true;
        while (enPerfil) {
            System.out.println("\n=== Mi Perfil ===");
            System.out.println("  Usuario:        @" + cliente.getUsuario());
            System.out.println("  Nombre:         " + cliente.getNombre());
            System.out.println("  Identificación: " + cliente.getIdentificacion());
            System.out.println("  Celular:        " + cliente.getCelular());

            System.out.println("\n¿Qué desea actualizar?");
            System.out.println("1. Nombre  2. Identificación  3. Celular  4. Contraseña  5. Cancelar");
            int opcion = FormValidation.validateIntRange("Opción", 1, 5);

            switch (opcion) {
                case 1 -> {
                    cliente.setNombre(FormValidation.validateString("Nuevo nombre completo"));
                    clienteRepository.actualizarCliente(cliente);
                    System.out.println("  Nombre actualizado correctamente.");
                }
                case 2 -> {
                    cliente.setIdentificacion(FormValidation.validateNumerico("Nueva identificación"));
                    clienteRepository.actualizarCliente(cliente);
                    System.out.println("  Identificación actualizada correctamente.");
                }
                case 3 -> {
                    cliente.setCelular(FormValidation.validateNumerico("Nuevo celular"));
                    clienteRepository.actualizarCliente(cliente);
                    System.out.println("  Celular actualizado correctamente.");
                }
                case 4 -> {
                    String actual = FormValidation.validateString("Contraseña actual");
                    if (!cliente.verificarPassword(actual)) {
                        System.out.println("  Contraseña actual incorrecta.");
                    } else {
                        // ERROR #1: también aquí usar validatePassword
                        cliente.setPassword(FormValidation.validatePassword("Nueva contraseña (mínimo 4 caracteres)"));
                        clienteRepository.actualizarCliente(cliente);
                        System.out.println("  Contraseña actualizada correctamente.");
                    }
                }
                case 5 -> enPerfil = false;
            }
        }

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