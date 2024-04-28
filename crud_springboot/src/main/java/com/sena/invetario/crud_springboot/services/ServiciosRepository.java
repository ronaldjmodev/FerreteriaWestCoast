package com.sena.invetario.crud_springboot.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sena.invetario.crud_springboot.models.Servicio;

public interface ServiciosRepository extends JpaRepository<Servicio,Integer> {

}
