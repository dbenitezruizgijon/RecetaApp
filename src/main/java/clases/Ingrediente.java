package clases;

import com.example.demo.RecetaInvalidaException;

public class Ingrediente {
	private String nombre;
	private int cantidad;

	public Ingrediente(String nombre, int cantidad) throws RecetaInvalidaException {
		setNombre(nombre);
		setCantidad(cantidad);
	}

	public static Ingrediente desdeTexto(String texto) throws RecetaInvalidaException {
		if (texto == null || texto.trim().isEmpty()) {
			throw new RecetaInvalidaException("El ingrediente está vacío");
		}
		String[] partes = texto.split(":");
		String nombre = partes[0];
		int cantidad = 1;
		if (partes.length > 1) {
			try {
				cantidad = Integer.parseInt(partes[1].trim());//ej, si es sal:mucha, da error
			} catch (Exception e) {
				throw new RecetaInvalidaException("Cantidad no válida en '" + texto + "'");
			}
		}
		return new Ingrediente(nombre, cantidad);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) throws RecetaInvalidaException {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new RecetaInvalidaException("El ingrediente necesita un nombre");
		}
		this.nombre = nombre.trim().toLowerCase();
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) throws RecetaInvalidaException {
		if (cantidad <= 0) {
			throw new RecetaInvalidaException("La cantidad de '" + nombre + "' debe ser mayor que 0");
		}
		this.cantidad = cantidad;
	}

	public String aTexto() {
		return nombre + ":" + cantidad;
	}

	@Override
	public String toString() {
		return nombre + " x" + cantidad;
	}
}
