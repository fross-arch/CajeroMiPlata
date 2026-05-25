package miplata.services.outputport;

import miplata.domain.Movimiento;
import java.util.List;

public interface MovimientoPersistencePort {
    Movimiento guardarMovimiento(Movimiento movimiento);
    List<Movimiento> findByUsuario(String usuario);
    List<Movimiento> findByUsuarioYTipoCuenta(String usuario, String tipoCuenta);
}