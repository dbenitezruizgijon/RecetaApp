package clases;

import com.example.demo.RecetaInvalidaException;

public class PlatoPrincipal extends Receta {
	private int raciones;

	public PlatoPrincipal(String nombre, String autor, String pais, int tiempoMinutos, int calorias, String pasos,
			int raciones) throws RecetaInvalidaException {
		super(nombre, autor, pais, tiempoMinutos, calorias, pasos);
		setRaciones(raciones);
	}

	@Override
	public String getTipo() {
		return "Plato principal";
	}

	@Override
	public String getDetalleExtra() {
		return raciones + " raciones";
	}

	public int getCaloriasPorRacion() {
		return getCalorias() / raciones;
	}

	public int getRaciones() {
		return raciones;
	}

	public void setRaciones(int raciones) throws RecetaInvalidaException {
		if (raciones <= 0) {
			throw new RecetaInvalidaException("Debe haber al menos 1 ración");
		}
		this.raciones = raciones;
	}
}
