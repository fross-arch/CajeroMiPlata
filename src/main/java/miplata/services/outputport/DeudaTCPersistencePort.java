package miplata.services.outputport;

import miplata.domain.DeudaTC;

import java.util.List;

public interface DeudaTCPersistencePort {

    DeudaTC guardarDeuda(String usuario, DeudaTC deuda);
    List<DeudaTC> findByUsuario(String usuario);
    DeudaTC actualizarDeuda(String usuario, DeudaTC deuda);
}