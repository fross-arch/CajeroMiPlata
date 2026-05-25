package miplata.services.outputport;

import miplata.domain.CuentaAhorros;
import java.util.Optional;

public interface CuentaAhorrosPersistencePort {
    CuentaAhorros guardarCuenta(CuentaAhorros cuenta);
    Optional<CuentaAhorros> findByUsuario(String usuario);
    CuentaAhorros actualizarSaldo(CuentaAhorros cuenta);
}