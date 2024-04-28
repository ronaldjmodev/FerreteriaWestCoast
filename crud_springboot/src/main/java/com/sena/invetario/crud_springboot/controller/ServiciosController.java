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

import com.sena.invetario.crud_springboot.models.Servicio;
import com.sena.invetario.crud_springboot.models.ServicioDto;
import com.sena.invetario.crud_springboot.services.ServiciosRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("servicios")
public class ServiciosController {

	@Autowired
	private ServiciosRepository repo;

	@GetMapping({ "", "/" })
	public String mostrarListaServicios(Model model) {
		List<Servicio> servicios = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("servicios", servicios);

		return "servicios/index";
	}

	@GetMapping("/crear")
	public String showCreatePage(Model model) {
		ServicioDto servicioDto = new ServicioDto();
		model.addAttribute("servicioDto", servicioDto);
		return "servicios/crearservicio";
	}

	@PostMapping("/crear")
	public String crearServicio(@Valid @ModelAttribute ServicioDto servicioDto, BindingResult resultado) {

		if (servicioDto.getArchivoImage().isEmpty()) {
			resultado.addError(
					new FieldError("servicioDto", "archivoImage", "El archivo para la imagen es obligatorio"));
		}

		if (resultado.hasErrors()) {
			return "servicios/crearservicio";
		}

		// Grabar Archivo de Imagen
		MultipartFile image = servicioDto.getArchivoImage();
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
			System.out.println("Exepción al grabar: " + ex.getMessage());
		}

		// Registro en base de datos del nuevo registro
		Servicio serv = new Servicio();
		serv.setNombre(servicioDto.getNombre());
		serv.setCategoria(servicioDto.getCategoria());
		serv.setCosto(servicioDto.getCosto());
		serv.setDescripcion(servicioDto.getDescripcion());
		serv.setFechaCreacion((java.sql.Date) fechaCreacion);
		serv.setNombreArchivoImagen(storageFileName);

		repo.save(serv);

		return "redirect:/servicios";
	}

	@GetMapping("/editar")
	public String showEditPag(Model model, @RequestParam int id) {
		try {
			Servicio serv = repo.findById(id).get();
			model.addAttribute("servicio", serv);

			ServicioDto servicioDto = new ServicioDto();
			servicioDto.setNombre(serv.getNombre());
			servicioDto.setCategoria(serv.getCategoria());
			servicioDto.setCosto(serv.getCosto());
			servicioDto.setDescripcion(serv.getDescripcion());

			model.addAttribute("servicioDto", servicioDto);
		} catch (Exception ex) {
			System.out.println("Excepción al Editar: " + ex.getMessage());
		}

		return "/servicios/EditarServicio";
	}

	@PostMapping("/editar")
	public String actualizarServicio(Model model, @RequestParam int id, @Valid @ModelAttribute ServicioDto servicioDto,
			BindingResult resultado) {

		try {
			Servicio servicio = repo.findById(id).get();
			model.addAttribute("servicio", servicio);
			// Si no hay errore
			if (resultado.hasErrors()) {
				return "servicios/EditarServicio";
			}
			//
			if (!servicioDto.getArchivoImage().isEmpty()) {
				// Eliminamos la imagen antigua
				String dirDeImagenes = "public/images/";
				Path rutaAntiguaImagen = Paths.get(dirDeImagenes + servicio.getNombreArchivoImagen());

				try {
					Files.delete(rutaAntiguaImagen);
				} catch (Exception ex) {
					System.out.println("Excepción: " + ex.getMessage());
				}

				// Grabar el archivo de la nueva imagen
				MultipartFile image = servicioDto.getArchivoImage();
				Date fechaCreacion = new Date(System.currentTimeMillis());
				String storageFileName = fechaCreacion.getTime() + "_" + image.getOriginalFilename();

				try (InputStream inputStream = image.getInputStream()) {
					Files.copy(inputStream, Paths.get(dirDeImagenes + storageFileName),
							StandardCopyOption.REPLACE_EXISTING);
				}

				servicio.setNombreArchivoImagen(storageFileName);

			}

			servicio.setNombre(servicioDto.getNombre());
			servicio.setCategoria(servicioDto.getCategoria());
			servicio.setCosto(servicioDto.getCosto());
			servicio.setDescripcion(servicioDto.getDescripcion());

			repo.save(servicio);

		} catch (Exception ex) {
			System.out.println("Excepción al grabar la edicón: " + ex.getMessage());
		}

		return "redirect:/servicios";
	}

	@GetMapping("/eliminar")
	public String eliminarServicio(@RequestParam int id) {

		try {
			Servicio servicio = repo.findById(id).get();
			// Eliminamos la imagen de la lista de producto
			Path rutaImagen = Paths.get("public/images/" + servicio.getNombreArchivoImagen());
			try {
				Files.delete(rutaImagen);
			} catch (Exception ex) {
				System.out.println("Excepcion al Eliminar: " + ex.getMessage());
			}

			// Eliminar el producto de la base de datos
			repo.delete(servicio);

		} catch (Exception ex) {
			System.out.println("Excepcion al Eliminar: " + ex.getMessage());
		}

		return "redirect:/servicios";
	}

}
