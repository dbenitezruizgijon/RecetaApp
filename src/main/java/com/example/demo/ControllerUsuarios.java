package com.example.demo;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import clases.Usuario;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class ControllerUsuarios {
	private ArrayList<Usuario> usuarios = GestorFicheros.leerUsuarios();

	@GetMapping("/login")
	public String mostrarLogin(@CookieValue(value = "recordarNombre", defaultValue = "") String nombreRecordado,
			Model model) {
		model.addAttribute("nombreRecordado", nombreRecordado);
		return "login";
	}

	@PostMapping("/login")
	public String procesarLogin(@RequestParam(name = "nombre") String nombre,
			@RequestParam(name = "contrasena") String contrasena,
			@RequestParam(name = "recordar", defaultValue = "no") String recordar,
			HttpSession session, HttpServletResponse response, Model model) {
		String vista;
		Usuario usuario = buscarUsuario(nombre);

		if (usuario == null || !usuario.comprobarContrasena(contrasena)) {
			model.addAttribute("error", "Usuario o contraseña incorrectos");
			model.addAttribute("nombreRecordado", nombre);
			vista = "login";
		} else {
			session.setAttribute("usuario", usuario.getNombre());
			session.removeAttribute("plan");

			Cookie cookie = new Cookie("recordarNombre", usuario.getNombre());
			if (recordar.equals("si")) {
				cookie.setMaxAge(30 * 24 * 60 * 60);
			} else {
				cookie.setMaxAge(0);
			}
			response.addCookie(cookie);

			vista = "redirect:/";
		}
		return vista;
	}

	@GetMapping("/registro")
	public String mostrarRegistro() {
		return "registro";
	}

	@PostMapping("/registro")
	public String procesarRegistro(@RequestParam(name = "nombre") String nombre,
			@RequestParam(name = "contrasena") String contrasena,
			HttpSession session, Model model) {
		String vista;

		try {
			Usuario nuevo = new Usuario(nombre, contrasena);

			if (buscarUsuario(nuevo.getNombre()) != null) {
				throw new Exception("Ese nombre de usuario ya existe");
			}

			usuarios.add(nuevo);
			GestorFicheros.guardarUsuarios(usuarios);

			session.setAttribute("usuario", nuevo.getNombre());
			session.removeAttribute("plan");
			vista = "redirect:/";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("nombre", nombre);
			vista = "registro";
		}
		return vista;
	}

	@GetMapping("/salir")
	public String salir(HttpSession session) {
		session.removeAttribute("usuario");
		session.removeAttribute("plan");
		return "redirect:/";
	}

	private Usuario buscarUsuario(String nombre) {
		Usuario encontrado = null;
		String buscado = nombre.trim().toLowerCase();
		int i = 0;
		while (i < usuarios.size() && encontrado == null) {
			if (usuarios.get(i).getNombre().equals(buscado)) {
				encontrado = usuarios.get(i);
			}
			i++;
		}
		return encontrado;
	}
}
