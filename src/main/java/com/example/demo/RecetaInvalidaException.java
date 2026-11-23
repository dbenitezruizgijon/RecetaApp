package com.example.demo;

public class RecetaInvalidaException extends Exception {
	private static final long serialVersionUID = 1L;

	public RecetaInvalidaException(String mensaje) {
		super(mensaje);
	}
}
