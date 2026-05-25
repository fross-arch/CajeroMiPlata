package miplata.services.outputport;

import miplata.domain.CuentaCorriente;
import java.util.Optional;

public interface CuentaCorrientePersistencePort {
    CuentaCorriente guardarCuenta(CuentaCorriente cuenta);
    Optional<CuentaCorriente> findByUsuario(String usuario);
    CuentaCorriente actualizarSaldo(CuentaCorriente cuenta);
}