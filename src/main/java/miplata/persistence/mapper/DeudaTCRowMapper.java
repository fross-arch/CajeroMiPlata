package miplata.persistence.mapper;

import miplata.domain.DeudaTC;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeudaTCRowMapper implements RowMapper<DeudaTC> {

    @Override
    public DeudaTC mapRow(ResultSet rs) throws SQLException {
        DeudaTC deuda = new DeudaTC(
                rs.getDouble("capital"),
                rs.getInt("cuotas"),
                rs.getDouble("cuota_mensual"),
                rs.getDouble("tasa")
        );
        deuda.setFecha(rs.getString("fecha"));
        deuda.setCuotasPagadas(rs.getInt("cuotas_pagadas"));
        deuda.setPagado(rs.getDouble("pagado"));
        deuda.setSaldoPendiente(rs.getDouble("saldo_pendiente"));
        return deuda;
    }}