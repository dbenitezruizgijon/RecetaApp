package clases;

import java.util.ArrayList;

import com.example.demo.RecetaInvalidaException;

public abstract class Receta implements Comparable<Receta> {
	private static int contadorId = 1;

	private static String criterioOrden = "nombre";

	private int id;
	private String nombre;
	private String autor;
	private String pais;
	private int tiempoMinutos;
	private int calorias;
	private String pasos;
	private String descripcion = "";
	private String imagen = "";
	private ArrayList<Ingrediente> ingredientes = new ArrayList<>();

	public Receta(String nombre, String autor, String pais, int tiempoMinutos, int calorias, String pasos)
			throws RecetaInvalidaException {
		this.id = contadorId;
		contadorId++;
		setNombre(nombre);
		setAutor(autor);
		setPais(pais);
		setTiempoMinutos(tiempoMinutos);
		setCalorias(calorias);
		setPasos(pasos);
	}

	//metodos abstractos: cada hija los hace a su manera
	public abstract String getTipo();

	public abstract String getDetalleExtra();

	//ingredientes
	public void anadirIngrediente(Ingrediente ingrediente) {
		ingredientes.add(ingrediente);
	}

	public ArrayList<Ingrediente> getIngredientes() {
		return ingredientes;
	}

	//filtrar y ordenar en explorar
	public boolean contieneIngrediente(String buscado) {//para ver si el ingrediente de explorar existe o no en alguna receta
		if (buscado == null || buscado.trim().isEmpty()) {
			return false;
		}
		String limpio = buscado.trim().toLowerCase();
		for (Ingrediente i : ingredientes) {
			if (i.getNombre().contains(limpio)) {
				return true;
			}
		}
		return false;
	}

	public static void setCriterioOrden(String criterio) {
		criterioOrden = criterio;
	}

	@Override
	public int compareTo(Receta otra) {
		int resultado;
		if (criterioOrden.equals("tiempo")) {
			resultado = this.tiempoMinutos - otra.tiempoMinutos;
		} else if (criterioOrden.equals("calorias")) {
			resultado = this.calorias - otra.calorias;
		} else {
			resultado = this.nombre.compareToIgnoreCase(otra.nombre);
		}
		return resultado;
	}

	//informacion que se muestra en la pagina de la receta
	public ArrayList<String> getPasosLista() {
		ArrayList<String> lista = new ArrayList<>();
		String[] frases = pasos.split("\\.");//para que al poner los pasos al añadir ingrediente, pase al siguiente paso
		for (String frase : frases) {
			if (!frase.trim().isEmpty()) {
				lista.add(frase.trim() + ".");
			}
		}
		return lista;
	}

	public String getOrigen() {
		String origen;
		if (pais.isEmpty()) {
			origen = "Receta casera";
		} else {
			origen = "Cocina de " + pais;
		}
		return origen;
	}

	public String getInicialAutor() {
		return autor.substring(0, 1).toUpperCase();
	}

	//getters y setters
	public int getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) throws RecetaInvalidaException {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new RecetaInvalidaException("La receta necesita un nombre");
		}
		String limpio = nombre.trim();
		if (limpio.length() > 50) {
			throw new RecetaInvalidaException("El nombre no puede pasar de 50 caracteres");
		}
		this.nombre = limpio.substring(0, 1).toUpperCase() + limpio.substring(1);
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) throws RecetaInvalidaException {
		if (autor == null || autor.trim().isEmpty()) {
			throw new RecetaInvalidaException("La receta necesita un autor");
		}
		this.autor = autor.trim();
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		if (pais == null) {
			this.pais = "";
		} else {
			this.pais = pais.trim();
		}
	}

	public int getTiempoMinutos() {
		return tiempoMinutos;
	}

	public void setTiempoMinutos(int tiempoMinutos) throws RecetaInvalidaException {
		if (tiempoMinutos <= 0) {
			throw new RecetaInvalidaException("El tiempo debe ser mayor que 0 minutos");
		}
		this.tiempoMinutos = tiempoMinutos;
	}

	public int getCalorias() {
		return calorias;
	}

	public void setCalorias(int calorias) throws RecetaInvalidaException {
		if (calorias < 0) {
			throw new RecetaInvalidaException("Las calorías no pueden ser negativas");
		}
		this.calorias = calorias;
	}

	public String getPasos() {
		return pasos;
	}

	public void setPasos(String pasos) {
		if (pasos == null) {
			this.pasos = "";
		} else {
			this.pasos = pasos.trim();
		}
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		if (descripcion == null) {
			this.descripcion = "";
		} else {
			this.descripcion = descripcion.trim();
		}
	}

	public String getImagen() {
		String archivo;
		if (imagen.isEmpty()) {
			archivo = "sin-foto.jpg";
		} else {
			archivo = imagen;
		}
		return archivo;
	}

	public void setImagen(String imagen) {
		if (imagen == null) {
			this.imagen = "";
		} else {
			this.imagen = imagen.trim();
		}
	}
}
