package com.sena.invetario.crud_springboot.models;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class ProductoDto {
    @NotEmpty(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotEmpty(message = "El nombre de la categoría es obligatorio")
    private String categoria;
    
    @Min(value = 0, message = "El costo no puede ser negativo")
    private double precio;
    
    @Size(min = 10, message = "La descripción debe tener más de 10 caracteres")
    @Size(max = 255, message = "La descripción no debe exceder los 255 caracteres")
    private String descripcion;
    
    private MultipartFile archivoImagen;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public MultipartFile getArchivoImagen() {
        return archivoImagen;
    }

    public void setArchivoImagen(MultipartFile archivoImagen) {
        this.archivoImagen = archivoImagen;
    }
}
