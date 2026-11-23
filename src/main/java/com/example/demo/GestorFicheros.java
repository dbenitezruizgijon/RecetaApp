package com.example.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import clases.Desayuno;
import clases.Ingrediente;
import clases.PlatoPrincipal;
import clases.Postre;
import clases.Receta;
import clases.Usuario;

public class GestorFicheros {

	public static ArrayList<Receta> leerRecetas() {
		ArrayList<Receta> recetas = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("datos/recetas.txt"));
			String linea = br.readLine();
			while (linea != null) {
				if (!linea.trim().isEmpty()) {
					try {
						recetas.add(lineaAReceta(linea));
					} catch (Exception e) {
						System.out.println("Receta ignorada: " + e.getMessage());
					}
				}
				linea = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("No se ha podido leer recetas.txt");
		}
		return recetas;
	}

	public static void guardarRecetas(ArrayList<Receta> recetas) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("datos/recetas.txt"));
			for (Receta r : recetas) {
				bw.write(recetaALinea(r));
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("No se ha podido guardar recetas.txt");
		}
	}

	private static Receta lineaAReceta(String linea) throws Exception {
		String[] p = linea.split(";");

		String tipo = p[0];
		String nombre = p[1];
		String autor = p[2];
		String pais = p[3];
		int minutos = Integer.parseInt(p[4]);
		int calorias = Integer.parseInt(p[5]);
		String extra = p[6];
		String imagen = p[7];
		String descripcion = p[8];
		String ingredientes = p[9];
		String pasos = p[10];

		Receta receta;
		switch (tipo) {
		case "DESAYUNO":
			receta = new Desayuno(nombre, autor, pais, minutos, calorias, pasos, Boolean.parseBoolean(extra));
			break;
		case "PRINCIPAL":
			receta = new PlatoPrincipal(nombre, autor, pais, minutos, calorias, pasos, Integer.parseInt(extra));
			break;
		case "POSTRE":
			receta = new Postre(nombre, autor, pais, minutos, calorias, pasos, Boolean.parseBoolean(extra));
			break;
		default:
			throw new RecetaInvalidaException("Tipo de receta desconocido: " + tipo);
		}
		receta.setImagen(imagen);
		receta.setDescripcion(descripcion);

		String[] lista = ingredientes.split(",");
		for (String texto : lista) {
			receta.anadirIngrediente(Ingrediente.desdeTexto(texto));
		}
		return receta;
	}

	private static String recetaALinea(Receta r) {
		String tipo;
		String extra;
		if (r instanceof Desayuno) {
			tipo = "DESAYUNO";
			Desayuno d = (Desayuno) r;
			extra = "" + d.isDulce();
		} else if (r instanceof PlatoPrincipal) {
			tipo = "PRINCIPAL";
			PlatoPrincipal pp = (PlatoPrincipal) r;
			extra = "" + pp.getRaciones();
		} else {
			tipo = "POSTRE";
			Postre po = (Postre) r;
			extra = "" + po.isNecesitaHorno();
		}

		String ingredientes = "";
		for (Ingrediente i : r.getIngredientes()) {
			if (!ingredientes.isEmpty()) {
				ingredientes += ",";
			}
			ingredientes += i.aTexto();
		}

		return tipo + ";" + limpiar(r.getNombre()) + ";" + limpiar(r.getAutor()) + ";" + limpiar(r.getPais())
				+ ";" + r.getTiempoMinutos() + ";" + r.getCalorias() + ";" + extra + ";" + r.getImagen()
				+ ";" + limpiar(r.getDescripcion()) + ";" + ingredientes + ";" + limpiar(r.getPasos());
	}

	private static String limpiar(String texto) {
		return texto.replace(";", ",").replace("\n", " ").replace("\r", "");
	}

	public static ArrayList<Usuario> leerUsuarios() {
		ArrayList<Usuario> usuarios = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("datos/usuarios.txt"));
			String linea = br.readLine();
			while (linea != null) {
				if (!linea.trim().isEmpty()) {
					try {
						String[] p = linea.split(";");
						usuarios.add(new Usuario(p[0], p[1]));
					} catch (Exception e) {
						System.out.println("Usuario ignorado: " + linea);
					}
				}
				linea = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("No se ha podido leer usuarios.txt");
		}
		return usuarios;
	}

	public static void guardarUsuarios(ArrayList<Usuario> usuarios) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("datos/usuarios.txt"));
			for (Usuario u : usuarios) {
				bw.write(u.getNombre() + ";" + u.getContrasena());
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("No se ha podido guardar usuarios.txt");
		}
	}

	public static String[][] leerMundo() {
		ArrayList<String> lineas = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("datos/mundo.txt"));
			String linea = br.readLine();
			while (linea != null) {
				if (linea.split(";").length == 8) {
					lineas.add(linea);
				}
				linea = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("No se ha podido leer mundo.txt");
		}

		String[][] mundo = new String[lineas.size()][8];
		for (int i = 0; i < lineas.size(); i++) {
			String[] partes = lineas.get(i).split(";");
			for (int j = 0; j < 8; j++) {
				mundo[i][j] = partes[j];
			}
		}
		return mundo;
	}

	public static Receta[][] leerPlan(String usuario, ArrayList<Receta> recetas) {
		Receta[][] plan = new Receta[3][7];
		File fichero = new File("datos/planes/" + usuario + ".txt");

		if (fichero.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(fichero));
				for (int fila = 0; fila < 3; fila++) {
					String[] nombres = br.readLine().split(";");
					for (int dia = 0; dia < 7; dia++) {
						plan[fila][dia] = buscarPorNombre(nombres[dia], recetas);
					}
				}
				br.close();
			} catch (Exception e) {
				System.out.println("No se ha podido leer el plan de " + usuario);
			}
		}
		return plan;
	}

	public static void guardarPlan(String usuario, Receta[][] plan) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("datos/planes/" + usuario + ".txt"));
			for (int fila = 0; fila < 3; fila++) {
				String linea = "";
				for (int dia = 0; dia < 7; dia++) {
					if (dia > 0) {
						linea += ";";
					}
					if (plan[fila][dia] == null) {
						linea += "-";
					} else {
						linea += plan[fila][dia].getNombre();
					}
				}
				bw.write(linea);
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("No se ha podido guardar el plan de " + usuario);
		}
	}

	private static Receta buscarPorNombre(String nombre, ArrayList<Receta> recetas) {
		Receta encontrada = null;
		for (Receta r : recetas) {
			if (r.getNombre().equals(nombre)) {
				encontrada = r;
			}
		}
		return encontrada;
	}

	public static HashMap<String, Integer> leerLista(String usuario) {
		HashMap<String, Integer> lista = new HashMap<>();
		File fichero = new File("datos/listas/" + usuario + ".txt");

		if (fichero.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(fichero));
				String linea = br.readLine();
				while (linea != null) {
					String[] p = linea.split(";");
					lista.put(p[0], Integer.parseInt(p[1]));
					linea = br.readLine();
				}
				br.close();
			} catch (Exception e) {
				System.out.println("No se ha podido leer la lista de " + usuario);
			}
		}
		return lista;
	}

	public static void guardarLista(String usuario, HashMap<String, Integer> lista) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("datos/listas/" + usuario + ".txt"));
			for (String producto : lista.keySet()) {
				bw.write(producto + ";" + lista.get(producto));
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("No se ha podido guardar la lista de " + usuario);
		}
	}
}
