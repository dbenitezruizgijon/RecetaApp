package clases;

public class Usuario {
	private String nombre;
	private String contrasena;

	public Usuario(String nombre, String contrasena) throws Exception {
		setNombre(nombre);
		setContrasena(contrasena);
	}

	public boolean comprobarContrasena(String escrita) {
		return contrasena.equals(escrita);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) throws Exception {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new Exception("Escribe un nombre de usuario");
		}
		String limpio = nombre.trim().toLowerCase();
		if (limpio.contains(" ") || limpio.contains(";")) {
			throw new Exception("El nombre no puede tener espacios ni punto y coma");
		}
		if (limpio.length() > 20) {
			throw new Exception("El nombre no puede pasar de 20 letras");
		}
		this.nombre = limpio;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) throws Exception {
		if (contrasena == null || contrasena.length() < 4) {
			throw new Exception("La contraseña debe tener al menos 4 caracteres");
		}
		if (contrasena.contains(";")) {
			throw new Exception("La contraseña no puede tener punto y coma");
		}
		this.contrasena = contrasena;
	}
}
