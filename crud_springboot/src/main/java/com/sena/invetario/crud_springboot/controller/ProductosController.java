package com.sena.invetario.crud_springboot.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sena.invetario.crud_springboot.models.Producto;
import com.sena.invetario.crud_springboot.models.ProductoDto;
import com.sena.invetario.crud_springboot.services.ProductosRepository;

import javax.validation.Valid;

@Controller
@RequestMapping("/productos")
public class ProductosController {

    @Autowired
    private ProductosRepository repo;

    @GetMapping({ "", "/" })
    public String mostrarListaProductos(Model model) {
        List<Producto> productos = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("productos", productos);
        return "producto/index"; // Ajusta la vista según tu estructura
    }

    @GetMapping("/crear")
    public String showCreatePage(Model model) {
        ProductoDto productoDto = new ProductoDto();
        model.addAttribute("productoDto", productoDto);
        return "producto/crearproducto"; // Ajusta la vista según tu estructura
    }

    @PostMapping("/crear")
    public String crearProducto(@Valid @ModelAttribute ProductoDto productoDto, BindingResult resultado) {

        if (productoDto.getArchivoImagen().isEmpty()) {
            resultado.addError(
                    new FieldError("productoDto", "archivoImagen", "El archivo para la imagen es obligatorio"));
        }

        if (resultado.hasErrors()) {
            return "producto/crearproducto"; // Ajusta la vista según tu estructura
        }

        // Grabar Archivo de Imagen
        MultipartFile image = productoDto.getArchivoImagen();
        Date fechaCreacion = new Date(System.currentTimeMillis());
        String storageFileName = fechaCreacion.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception ex) {
            System.out.println("Excepción al grabar: " + ex.getMessage());
        }

        // Registro en base de datos del nuevo producto
        Producto producto = new Producto();
        producto.setNombre(productoDto.getNombre());
        producto.setCategoria(productoDto.getCategoria());
        producto.setPrecio(productoDto.getPrecio());
        producto.setDescripcion(productoDto.getDescripcion());
        producto.setFechaCreacion((java.sql.Date) fechaCreacion);
        producto.setNombreArchivoImagen(storageFileName);

        repo.save(producto);

        return "redirect:/producto";
    }

    @GetMapping("/editar")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Producto producto = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            model.addAttribute("producto", producto);

            ProductoDto productoDto = new ProductoDto();
            productoDto.setNombre(producto.getNombre());
            productoDto.setCategoria(producto.getCategoria());
            productoDto.setPrecio(producto.getPrecio());
            productoDto.setDescripcion(producto.getDescripcion());

            model.addAttribute("productoDto", productoDto);
        } catch (Exception ex) {
            System.out.println("Excepción al Editar: " + ex.getMessage());
        }

        return "producto/editarproducto"; // Ajusta la vista según tu estructura
    }

    @PostMapping("/editar")
    public String actualizarProducto(Model model, @RequestParam int id, @Valid @ModelAttribute ProductoDto productoDto,
                                     BindingResult resultado) {

        try {
            Producto producto = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            model.addAttribute("producto", producto);

            if (resultado.hasErrors()) {
                return "producto/editarproducto"; // Ajusta la vista según tu estructura
            }

            if (!productoDto.getArchivoImagen().isEmpty()) {
                // Eliminamos la imagen antigua
                String dirDeImagenes = "public/images/";
                Path rutaAntiguaImagen = Paths.get(dirDeImagenes + producto.getNombreArchivoImagen());

                try {
                    Files.delete(rutaAntiguaImagen);
                } catch (Exception ex) {
                    System.out.println("Excepción al eliminar imagen antigua: " + ex.getMessage());
                }

                // Grabar el archivo de la nueva imagen
                MultipartFile image = productoDto.getArchivoImagen();
                Date fechaCreacion = new Date(System.currentTimeMillis());
                String storageFileName = fechaCreacion.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(dirDeImagenes + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                producto.setNombreArchivoImagen(storageFileName);
            }

            producto.setNombre(productoDto.getNombre());
            producto.setCategoria(productoDto.getCategoria());
            producto.setPrecio(productoDto.getPrecio());
            producto.setDescripcion(productoDto.getDescripcion());

            repo.save(producto);

        } catch (Exception ex) {
            System.out.println("Excepción al grabar la edición: " + ex.getMessage());
        }

        return "redirect:/producto";
    }

    @GetMapping("/eliminar")
    public String eliminarProducto(@RequestParam int id) {

        try {
            Producto producto = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            // Eliminamos la imagen del producto
            Path rutaImagen = Paths.get("public/images/" + producto.getNombreArchivoImagen());
            try {
                Files.delete(rutaImagen);
            } catch (Exception ex) {
                System.out.println("Excepción al eliminar imagen: " + ex.getMessage());
            }

            // Eliminar el producto de la base de datos
            repo.delete(producto);

        } catch (Exception ex) {
            System.out.println("Excepción al eliminar producto: " + ex.getMessage());
        }

        return "redirect:/producto";
    }

    @GetMapping("/detalle")
    public String mostrarDetalleProducto(Model model, @RequestParam int id) {
        try {
            Producto producto = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            model.addAttribute("producto", producto);
        } catch (Exception ex) {
            System.out.println("Excepción al mostrar detalle: " + ex.getMessage());
        }

        return "producto/detalleproducto"; // Ajusta la vista según tu estructura
    }
}
