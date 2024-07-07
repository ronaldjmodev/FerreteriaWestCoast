package com.sena.invetario.crud_springboot.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sena.invetario.crud_springboot.models.Producto;

public interface ProductosRepository extends JpaRepository<Producto, Integer> {

}