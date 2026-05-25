package miplata.services.outputport;

import miplata.domain.TarjetaCredito;
import java.util.Optional;

public interface TarjetaCreditoPersistencePort {
    TarjetaCredito guardarTarjeta(TarjetaCredito tarjeta);
    Optional<TarjetaCredito> findByUsuario(String usuario);
    TarjetaCredito actualizarTarjeta(TarjetaCredito tarjeta);
}