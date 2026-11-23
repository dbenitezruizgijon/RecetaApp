package clases;

import com.example.demo.RecetaInvalidaException;

public class Postre extends Receta {
	private boolean necesitaHorno;

	public Postre(String nombre, String autor, String pais, int tiempoMinutos, int calorias, String pasos,
			boolean necesitaHorno) throws RecetaInvalidaException {
		super(nombre, autor, pais, tiempoMinutos, calorias, pasos);
		this.necesitaHorno = necesitaHorno;
	}

	@Override
	public String getTipo() {
		return "Postre";
	}

	@Override
	public String getDetalleExtra() {
		if (necesitaHorno) {
			return "Al horno";
		} else {
			return "Sin horno";
		}
	}

	public boolean isNecesitaHorno() {
		return necesitaHorno;
	}

	public void setNecesitaHorno(boolean necesitaHorno) {
		this.necesitaHorno = necesitaHorno;
	}
}
