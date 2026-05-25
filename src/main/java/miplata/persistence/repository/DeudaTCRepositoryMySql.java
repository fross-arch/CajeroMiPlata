package miplata.persistence.repository;

import miplata.domain.DeudaTC;
import miplata.persistence.mapper.DeudaTCRowMapper;
import miplata.services.outputport.DeudaTCPersistencePort;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeudaTCRepositoryMySql implements DeudaTCPersistencePort {

    private final Connection connection;
    private final DeudaTCRowMapper rowMapper;

    public DeudaTCRepositoryMySql(Connection connection, DeudaTCRowMapper rowMapper) {
        this.connection = connection;
        this.rowMapper = rowMapper;
    }

    @Override
    public DeudaTC guardarDeuda(String usuario, DeudaTC deuda) {
        String sql = "INSERT INTO deuda_tc (usuario, fecha, capital, cuotas, cuotas_pagadas, cuota_mensual, total_a_pagar, pagado, tasa, saldo_pendiente) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, deuda.getFecha());
            ps.setDouble(3, deuda.getCapital());
            ps.setInt(4, deuda.getCuotas());
            ps.setInt(5, deuda.getCuotasPagadas());
            ps.setDouble(6, deuda.getCuotaMensual());
            ps.setDouble(7, deuda.getTotalAPagar());
            ps.setDouble(8, deuda.getPagado());
            ps.setDouble(9, deuda.getTasa());
            ps.setDouble(10, deuda.getSaldoPendiente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar deuda TC", e);
        }
        return deuda;
    }

    @Override
    public List<DeudaTC> findByUsuario(String usuario) {
        String sql = "SELECT * FROM deuda_tc WHERE usuario = ?";
        List<DeudaTC> deudas = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                deudas.add(rowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar deudas TC", e);
        }
        return deudas;
    }

    @Override
    public DeudaTC actualizarDeuda(String usuario, DeudaTC deuda) {
        String sql = "UPDATE deuda_tc SET cuotas_pagadas=?, pagado=?, saldo_pendiente=? WHERE usuario=? AND fecha=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, deuda.getCuotasPagadas());
            ps.setDouble(2, deuda.getPagado());
            ps.setDouble(3, deuda.getSaldoPendiente());
            ps.setString(4, usuario);
            ps.setString(5, deuda.getFecha());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar deuda TC", e);
        }
        return deuda;
    }
}