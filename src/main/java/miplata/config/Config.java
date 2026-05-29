package miplata.config;

import miplata.persistence.mapper.*;
import miplata.persistence.repository.*;
import miplata.repository.ClienteRepository;
import miplata.services.ClienteService;
import miplata.services.ClienteServiceImpl;
import miplata.services.CuentaService;
import miplata.services.CuentaServiceImpl;
import miplata.services.outputport.ClientePersistencePort;
import miplata.userinterface.MenuApp;
import miplata.view.ClienteView;
import miplata.view.CuentaView;
import miplata.services.outputport.CuentaAhorrosPersistencePort;
import miplata.services.outputport.CuentaCorrientePersistencePort;
import miplata.services.outputport.TarjetaCreditoPersistencePort;
import miplata.services.outputport.MovimientoPersistencePort;
import miplata.services.outputport.DeudaTCPersistencePort;
import miplata.services.admin.AdminService;
import miplata.services.admin.AdminServiceImpl;
import miplata.view.admin.AdminView;

import java.sql.Connection;

public class Config {

    public static MenuApp createMenuApp() {

        // 1. Conexión a la base de datos (Singleton)
        Connection connection = DataBaseConnectionMySql.getInstance().getConnection();

        // 2. Mappers
        ClienteRowMapper clienteRowMapper = new ClienteRowMapper();
        CuentaAhorrosRowMapper cuentaAhorrosRowMapper = new CuentaAhorrosRowMapper();
        CuentaCorrienteRowMapper cuentaCorrienteRowMapper = new CuentaCorrienteRowMapper();
        TarjetaCreditoRowMapper tarjetaCreditoRowMapper = new TarjetaCreditoRowMapper();
        MovimientoRowMapper movimientoRowMapper = new MovimientoRowMapper();
        DeudaTCRowMapper deudaTCRowMapper = new DeudaTCRowMapper();

        // 3. Repositorios MySQL
        ClientePersistencePort clienteRepository = new ClienteRepositoryMySql(connection, clienteRowMapper);
        CuentaAhorrosPersistencePort cuentaAhorrosRepository = new CuentaAhorrosRepositoryMySql(connection, cuentaAhorrosRowMapper);
        CuentaCorrientePersistencePort cuentaCorrienteRepository = new CuentaCorrienteRepositoryMySql(connection, cuentaCorrienteRowMapper);
        TarjetaCreditoPersistencePort tarjetaCreditoRepository = new TarjetaCreditoRepositoryMySql(connection, tarjetaCreditoRowMapper);
        MovimientoPersistencePort movimientoRepository = new MovimientoRepositoryMySql(connection, movimientoRowMapper);
        DeudaTCPersistencePort deudaTCRepository = new DeudaTCRepositoryMySql(connection, deudaTCRowMapper);

        // 4. Repositorio en memoria para las cuentas de sesión
        ClienteRepository clienteRepositoryMemoria = new ClienteRepository();

        // 5. Servicios de negocio
        ClienteService clienteService = new ClienteServiceImpl(
                clienteRepository, clienteRepositoryMemoria,
                cuentaAhorrosRepository, cuentaCorrienteRepository,
                tarjetaCreditoRepository, deudaTCRepository);

        CuentaService cuentaService = new CuentaServiceImpl(
                clienteRepositoryMemoria, clienteRepository,
                cuentaAhorrosRepository, cuentaCorrienteRepository,
                tarjetaCreditoRepository, movimientoRepository, deudaTCRepository);

        // 6. Servicio y vista de administración
        AdminService adminService = new AdminServiceImpl(
                clienteRepository, cuentaAhorrosRepository, cuentaCorrienteRepository,
                tarjetaCreditoRepository, deudaTCRepository, movimientoRepository);
        AdminView adminView = new AdminView(adminService);

        // 7. Vistas del cliente
        ClienteView clienteView = new ClienteView(clienteService);
        CuentaView cuentaView = new CuentaView(cuentaService, clienteRepositoryMemoria);

        // 8. Menú principal
        return new MenuApp(clienteView, cuentaView, clienteService, clienteRepositoryMemoria, adminView);
    }
}