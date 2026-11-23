package com.example.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import clases.Desayuno;
import clases.Ingrediente;
import clases.PlatoPrincipal;
import clases.Postre;
import clases.Receta;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class ControllerPrincipal {
	private ArrayList<Receta> recetas = GestorFicheros.leerRecetas();

	private String[][] mundo = GestorFicheros.leerMundo();

	@GetMapping("/")
	public String inicio(Model model) {
		model.addAttribute("numRecetas", recetas.size());
		model.addAttribute("numCocineros", contarCocineros());
		model.addAttribute("ultimas", ultimasRecetas(3));
		return "index";
	}

	@GetMapping("/explorar")
	public String explorar(@RequestParam(name = "buscar", defaultValue = "") String buscar,
			@RequestParam(name = "tipo", defaultValue = "") String tipo,
			@RequestParam(name = "orden", defaultValue = "") String orden,
			Model model) {
		ArrayList<Receta> resultado = new ArrayList<>();
		for (Receta r : recetas) {
			boolean cumple = true;
			if (!buscar.trim().isEmpty() && !r.contieneIngrediente(buscar)) {
				cumple = false;
			}
			if (!tipo.isEmpty() && !r.getTipo().equals(tipo)) {
				cumple = false;
			}
			if (cumple) {
				resultado.add(r);
			}
		}

		if (!orden.isEmpty()) {
			Receta.setCriterioOrden(orden);
			Collections.sort(resultado);
		}

		String mensaje = "";
		if (resultado.isEmpty()) {
			mensaje = "No hay recetas con esos filtros. Prueba con otro ingrediente.";
		}

		model.addAttribute("recetas", resultado);
		model.addAttribute("buscar", buscar);
		model.addAttribute("tipo", tipo);
		model.addAttribute("orden", orden);
		model.addAttribute("mensaje", mensaje);
		return "explorar";
	}

	@GetMapping("/receta")
	public String verReceta(@RequestParam(name = "id") int id, Model model) {
		String vista;
		Receta receta = buscarPorId(id);

		if (receta == null) {
			vista = "redirect:/explorar";
		} else {
			model.addAttribute("receta", receta);
			model.addAttribute("recetasAutor", contarRecetasDe(receta.getAutor()));
			vista = "receta";
		}
		return vista;
	}

	@GetMapping("/nueva-receta")
	public String mostrarNuevaReceta(HttpSession session) {
		String vista;
		if (session.getAttribute("usuario") == null) {
			vista = "redirect:/login";
		} else {
			vista = "nueva-receta";
		}
		return vista;
	}

	@PostMapping("/nueva-receta")
	public String guardarNuevaReceta(@RequestParam(name = "nombre") String nombre,
			@RequestParam(name = "tipo") String tipo,
			@RequestParam(name = "tiempo") int tiempo,
			@RequestParam(name = "calorias") int calorias,
			@RequestParam(name = "pais") String pais,
			@RequestParam(name = "descripcion") String descripcion,
			@RequestParam(name = "ingredientes") String ingredientes,
			@RequestParam(name = "pasos") String pasos,
			@RequestParam(name = "raciones", defaultValue = "2") int raciones,
			@RequestParam(name = "dulce", defaultValue = "no") String dulce,
			@RequestParam(name = "horno", defaultValue = "no") String horno,
			@RequestParam(name = "foto") MultipartFile foto, // la foto subida desde el formulario (no visto en clase)
			HttpSession session, Model model) {
		String vista;
		String usuario = (String) session.getAttribute("usuario");

		if (usuario == null) {
			vista = "redirect:/login";
		} else {
			try {
				Receta receta = crearReceta(tipo, nombre, usuario, pais, tiempo, calorias, pasos,
						raciones, dulce.equals("si"), horno.equals("si"));
				receta.setDescripcion(descripcion);

				if (ingredientes.trim().isEmpty()) {
					throw new Exception("Añade al menos un ingrediente");
				}
				String[] lista = ingredientes.split(",");
				for (String texto : lista) {
					receta.anadirIngrediente(Ingrediente.desdeTexto(texto));
				}

				if (!foto.isEmpty()) {
					receta.setImagen(guardarFoto(foto));
				}

				recetas.add(receta);
				GestorFicheros.guardarRecetas(recetas);

				vista = "redirect:/receta?id=" + receta.getId();
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				model.addAttribute("nombre", nombre);
				model.addAttribute("tiempo", tiempo);
				model.addAttribute("calorias", calorias);
				model.addAttribute("pais", pais);
				model.addAttribute("descripcion", descripcion);
				model.addAttribute("ingredientes", ingredientes);
				model.addAttribute("pasos", pasos);
				vista = "nueva-receta";
			}
		}
		return vista;
	}

	@GetMapping("/mundo")
	public String cocinaDelMundo(@RequestParam(name = "pais", defaultValue = "") String pais,
			@CookieValue(value = "ultimoPais", defaultValue = "0") String ultimoPais,
			HttpServletResponse response, Model model) {
		int fila;
		if (!pais.isEmpty()) {
			fila = buscarPais(pais);
		} else {
			try {
				fila = Integer.parseInt(ultimoPais);
			} catch (Exception e) {
				fila = 0;
			}
		}

		prepararMundo(fila, "", model, response);
		model.addAttribute("mensajeSorpresa", "");
		return "mundo";
	}

	@GetMapping("/sorprendeme")
	public String sorprendeme(HttpServletResponse response, Model model) {
		String mensaje = "";
		String sorpresa = "";
		int fila = 0;

		if (mundo.length > 0) {
			fila = (int) (Math.random() * mundo.length);
			int columna = 1 + (int) (Math.random() * 7);
			sorpresa = mundo[fila][columna];
			mensaje = "Hoy te toca: " + sorpresa + ", de " + mundo[fila][0];
		}

		prepararMundo(fila, sorpresa, model, response);
		model.addAttribute("mensajeSorpresa", mensaje);
		return "mundo";
	}

	private String[] dias = { "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo" };
	private String[] comidas = { "Desayuno", "Comida", "Cena" };

	@GetMapping("/planificador")
	public String planificador(@RequestParam(name = "aviso", defaultValue = "") String aviso,
			HttpSession session, Model model) {
		String vista;
		String usuario = (String) session.getAttribute("usuario");

		if (usuario == null) {
			vista = "redirect:/login";
		} else {
			model.addAttribute("plan", obtenerPlan(session, usuario));
			model.addAttribute("dias", dias);
			model.addAttribute("comidas", comidas);
			model.addAttribute("recetas", recetas);

			String mensaje = "";
			if (aviso.equals("guardado")) {
				mensaje = "Plan guardado. La próxima vez que entres lo tendrás aquí.";
			}
			model.addAttribute("mensaje", mensaje);
			vista = "planificador";
		}
		return vista;
	}

	@PostMapping("/planificador/colocar")
	public String colocarEnPlan(@RequestParam(name = "id") int id, @RequestParam(name = "dia") int dia,
			@RequestParam(name = "comida") int comida, HttpSession session) {
		String usuario = (String) session.getAttribute("usuario");
		if (usuario != null && dia >= 0 && dia < 7 && comida >= 0 && comida < 3) {
			Receta[][] plan = obtenerPlan(session, usuario);
			plan[comida][dia] = buscarPorId(id);
		}
		return "redirect:/planificador";
	}

	@PostMapping("/planificador/azar")
	public String rellenarAlAzar(HttpSession session) {
		String usuario = (String) session.getAttribute("usuario");
		if (usuario != null) {
			Receta[][] plan = obtenerPlan(session, usuario);

			ArrayList<Receta> desayunos = new ArrayList<>();
			ArrayList<Receta> principales = new ArrayList<>();
			for (Receta r : recetas) {
				if (r instanceof Desayuno) {
					desayunos.add(r);
				} else if (r instanceof PlatoPrincipal) {
					principales.add(r);
				}
			}

			for (int fila = 0; fila < 3; fila++) {
				for (int dia = 0; dia < 7; dia++) {
					if (plan[fila][dia] == null) {
						if (fila == 0 && !desayunos.isEmpty()) {
							plan[fila][dia] = desayunos.get((int) (Math.random() * desayunos.size()));
						} else if (fila > 0 && !principales.isEmpty()) {
							plan[fila][dia] = principales.get((int) (Math.random() * principales.size()));
						}
					}
				}
			}
		}
		return "redirect:/planificador";
	}

	@PostMapping("/planificador/vaciar")
	public String vaciarPlan(HttpSession session) {
		if (session.getAttribute("usuario") != null) {
			session.setAttribute("plan", new Receta[3][7]);
		}
		return "redirect:/planificador";
	}

	@PostMapping("/planificador/guardar")
	public String guardarPlan(HttpSession session) {
		String vista = "redirect:/login";
		String usuario = (String) session.getAttribute("usuario");
		if (usuario != null) {
			GestorFicheros.guardarPlan(usuario, obtenerPlan(session, usuario));
			vista = "redirect:/planificador?aviso=guardado";
		}
		return vista;
	}

	@GetMapping("/lista")
	public String listaCompra(@RequestParam(name = "aviso", defaultValue = "") String aviso,
			HttpSession session, Model model) {
		String vista;
		String usuario = (String) session.getAttribute("usuario");

		if (usuario == null) {
			vista = "redirect:/login";
		} else {
			HashMap<String, Integer> lista = GestorFicheros.leerLista(usuario);

			String mensaje = "";
			if (aviso.equals("generada")) {
				mensaje = "Añadidos los ingredientes de tu plan semanal.";
			} else if (aviso.equals("error")) {
				mensaje = "Escribe un producto y una cantidad mayor que 0.";
			}

			String vacia = "";
			if (lista.isEmpty()) {
				vacia = "Tu lista está vacía. Añade productos o genérala desde tu plan semanal.";
			}

			model.addAttribute("lista", lista);
			model.addAttribute("mensaje", mensaje);
			model.addAttribute("vacia", vacia);
			vista = "lista";
		}
		return vista;
	}

	@PostMapping("/lista/anadir")
	public String anadirALista(@RequestParam(name = "producto") String producto,
			@RequestParam(name = "cantidad", defaultValue = "1") int cantidad, HttpSession session) {
		String vista = "redirect:/lista";
		String usuario = (String) session.getAttribute("usuario");

		if (usuario == null) {
			vista = "redirect:/login";
		} else {
			try {
				Ingrediente nuevo = new Ingrediente(producto, cantidad);
				HashMap<String, Integer> lista = GestorFicheros.leerLista(usuario);
				sumarALista(lista, nuevo.getNombre(), nuevo.getCantidad());
				GestorFicheros.guardarLista(usuario, lista);
			} catch (Exception e) {
				vista = "redirect:/lista?aviso=error";
			}
		}
		return vista;
	}

	@PostMapping("/lista/quitar")
	public String quitarDeLista(@RequestParam(name = "producto") String producto, HttpSession session) {
		String usuario = (String) session.getAttribute("usuario");
		if (usuario != null) {
			HashMap<String, Integer> lista = GestorFicheros.leerLista(usuario);
			lista.remove(producto);
			GestorFicheros.guardarLista(usuario, lista);
		}
		return "redirect:/lista";
	}

	@PostMapping("/lista/generar")
	public String generarLista(HttpSession session) {
		String vista = "redirect:/login";
		String usuario = (String) session.getAttribute("usuario");

		if (usuario != null) {
			Receta[][] plan = obtenerPlan(session, usuario);
			HashMap<String, Integer> lista = GestorFicheros.leerLista(usuario);

			for (int fila = 0; fila < 3; fila++) {
				for (int dia = 0; dia < 7; dia++) {
					if (plan[fila][dia] != null) {
						for (Ingrediente i : plan[fila][dia].getIngredientes()) {
							sumarALista(lista, i.getNombre(), i.getCantidad());
						}
					}
				}
			}

			GestorFicheros.guardarLista(usuario, lista);
			vista = "redirect:/lista?aviso=generada";
		}
		return vista;
	}

	@PostMapping("/lista/vaciar")
	public String vaciarLista(HttpSession session) {
		String usuario = (String) session.getAttribute("usuario");
		if (usuario != null) {
			GestorFicheros.guardarLista(usuario, new HashMap<>());
		}
		return "redirect:/lista";
	}

	@GetMapping("/plato")
	public String verPlatoDelMundo(@RequestParam(name = "nombre") String nombre,
			@RequestParam(name = "pais", defaultValue = "") String pais,
			HttpSession session, Model model) {
		String vista;
		Receta receta = buscarPorNombre(nombre);

		if (receta != null) {
			vista = "redirect:/receta?id=" + receta.getId();
		} else if (session.getAttribute("usuario") == null) {
			vista = "redirect:/login";
		} else {
			model.addAttribute("nombre", nombre);
			model.addAttribute("pais", pais);
			model.addAttribute("aviso", "Nadie ha subido todavía " + nombre + ". ¡Sé el primero en compartirla!");
			vista = "nueva-receta";
		}
		return vista;
	}

	@GetMapping("/mis-recetas")
	public String misRecetas(HttpSession session, Model model) {
		String vista;
		String usuario = (String) session.getAttribute("usuario");

		if (usuario == null) {
			vista = "redirect:/login";
		} else {
			ArrayList<Receta> mias = new ArrayList<>();
			for (Receta r : recetas) {
				if (r.getAutor().equals(usuario)) {
					mias.add(r);
				}
			}

			String mensaje = "";
			if (mias.isEmpty()) {
				mensaje = "Todavía no has subido ninguna receta. ¡Anímate con la primera!";
			}

			model.addAttribute("recetas", mias);
			model.addAttribute("mensaje", mensaje);
			vista = "mis-recetas";
		}
		return vista;
	}

	@PostMapping("/borrar-receta")
	public String borrarReceta(@RequestParam(name = "id") int id, HttpSession session) {
		String usuario = (String) session.getAttribute("usuario");
		Receta receta = buscarPorId(id);

		if (usuario != null && receta != null && receta.getAutor().equals(usuario)) {
			recetas.remove(receta);
			GestorFicheros.guardarRecetas(recetas);
		}
		return "redirect:/mis-recetas";
	}

	private Receta crearReceta(String tipo, String nombre, String autor, String pais, int tiempo, int calorias,
			String pasos, int raciones, boolean dulce, boolean horno) throws Exception {
		Receta receta;
		switch (tipo) {
		case "DESAYUNO":
			receta = new Desayuno(nombre, autor, pais, tiempo, calorias, pasos, dulce);
			break;
		case "PRINCIPAL":
			receta = new PlatoPrincipal(nombre, autor, pais, tiempo, calorias, pasos, raciones);
			break;
		case "POSTRE":
			receta = new Postre(nombre, autor, pais, tiempo, calorias, pasos, horno);
			break;
		default:
			throw new Exception("Elige un tipo de receta");
		}
		return receta;
	}

	// Mejora no vista en clase: subida de archivos con MultipartFile.
	// La foto se guarda en datos/subidas/img y application.properties hace que se vea en /img/...
	private String guardarFoto(MultipartFile foto) throws Exception {
		String original = foto.getOriginalFilename();
		String extension = original.substring(original.lastIndexOf(".") + 1).toLowerCase();

		if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")
				&& !extension.equals("webp")) {
			throw new Exception("La foto tiene que ser jpg, png o webp");
		}

		String nombreArchivo = "receta-" + System.currentTimeMillis() + "." + extension;

		foto.transferTo(new File("datos/subidas/img/" + nombreArchivo).getAbsoluteFile());

		return nombreArchivo;
	}

	private Receta[][] obtenerPlan(HttpSession session, String usuario) {
		Receta[][] plan = (Receta[][]) session.getAttribute("plan");
		if (plan == null) {
			plan = GestorFicheros.leerPlan(usuario, recetas);
			session.setAttribute("plan", plan);
		}
		return plan;
	}

	private void sumarALista(HashMap<String, Integer> lista, String producto, int cantidad) {
		if (lista.containsKey(producto)) {
			lista.put(producto, lista.get(producto) + cantidad);
		} else {
			lista.put(producto, cantidad);
		}
	}

	private int buscarPais(String pais) {
		int encontrada = 0;
		for (int i = 0; i < mundo.length; i++) {
			if (mundo[i][0].equalsIgnoreCase(pais.trim())) {
				encontrada = i;
			}
		}
		return encontrada;
	}

	private void prepararMundo(int fila, String sorpresa, Model model, HttpServletResponse response) {
		if (fila < 0 || fila >= mundo.length) {
			fila = 0;
		}

		String[] paises = new String[mundo.length];
		for (int i = 0; i < mundo.length; i++) {
			paises[i] = mundo[i][0];
		}

		String[] platos = new String[7];
		String paisElegido = "";
		if (mundo.length > 0) {
			for (int j = 0; j < 7; j++) {
				platos[j] = mundo[fila][j + 1];
			}
			paisElegido = mundo[fila][0];
		}

		Cookie cookie = new Cookie("ultimoPais", "" + fila);
		cookie.setMaxAge(30 * 24 * 60 * 60);
		response.addCookie(cookie);

		model.addAttribute("paises", paises);
		model.addAttribute("platos", platos);
		model.addAttribute("paisElegido", paisElegido);
		model.addAttribute("sorpresa", sorpresa);
	}

	private Receta buscarPorNombre(String nombre) {
		Receta encontrada = null;
		for (Receta r : recetas) {
			if (r.getNombre().equalsIgnoreCase(nombre.trim())) {
				encontrada = r;
			}
		}
		return encontrada;
	}

	private Receta buscarPorId(int id) {
		Receta encontrada = null;
		int i = 0;
		while (i < recetas.size() && encontrada == null) {
			if (recetas.get(i).getId() == id) {
				encontrada = recetas.get(i);
			}
			i++;
		}
		return encontrada;
	}

	private int contarRecetasDe(String autor) {
		int total = 0;
		for (Receta r : recetas) {
			if (r.getAutor().equals(autor)) {
				total++;
			}
		}
		return total;
	}

	private int contarCocineros() {
		ArrayList<String> autores = new ArrayList<>();
		for (Receta r : recetas) {
			if (!autores.contains(r.getAutor())) {
				autores.add(r.getAutor());
			}
		}
		return autores.size();
	}

	private ArrayList<Receta> ultimasRecetas(int cantidad) {
		ArrayList<Receta> ultimas = new ArrayList<>();
		int i = recetas.size() - 1;
		while (i >= 0 && ultimas.size() < cantidad) {
			ultimas.add(recetas.get(i));
			i--;
		}
		return ultimas;
	}
}
