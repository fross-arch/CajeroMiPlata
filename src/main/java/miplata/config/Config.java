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
import java.sql.Connection;
import miplata.services.outputport.DeudaTCPersistencePort;
import miplata.persistence.mapper.DeudaTCRowMapper;
import miplata.persistence.repository.DeudaTCRepositoryMySql;
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

        // 4. Repositorio en memoria para las cuentas
        ClienteRepository clienteRepositoryMemoria = new ClienteRepository();

        // 5. Servicios
        ClienteService clienteService = new ClienteServiceImpl(clienteRepository, clienteRepositoryMemoria, cuentaAhorrosRepository, cuentaCorrienteRepository, tarjetaCreditoRepository, deudaTCRepository);
        CuentaService cuentaService = new CuentaServiceImpl(clienteRepositoryMemoria, clienteRepository, cuentaAhorrosRepository, cuentaCorrienteRepository, tarjetaCreditoRepository, movimientoRepository, deudaTCRepository);

        // 6. Vistas
        ClienteView clienteView = new ClienteView(clienteService);
        CuentaView cuentaView = new CuentaView(cuentaService, clienteRepositoryMemoria);

        // 7. Menú principal
        return new MenuApp(clienteView, cuentaView, clienteService, clienteRepositoryMemoria);
    }
}