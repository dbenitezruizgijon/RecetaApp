package clases;

import com.example.demo.RecetaInvalidaException;

public class Desayuno extends Receta {
	private boolean dulce;

	public Desayuno(String nombre, String autor, String pais, int tiempoMinutos, int calorias, String pasos,
			boolean dulce) throws RecetaInvalidaException {
		super(nombre, autor, pais, tiempoMinutos, calorias, pasos);
		this.dulce = dulce;
	}

	@Override
	public String getTipo() {
		return "Desayuno";
	}

	@Override
	public String getDetalleExtra() {
		if (dulce) {
			return "Dulce";
		} else {
			return "Salado";
		}
	}

	public boolean esExpres() {
		return getTiempoMinutos() <= 10;
	}

	public boolean isDulce() {
		return dulce;
	}

	public void setDulce(boolean dulce) {
		this.dulce = dulce;
	}
}
