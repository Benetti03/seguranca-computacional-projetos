package com.autorizacoes.service;

import java.time.LocalDateTime;

public class LoggerService {

	public static void log(String message) {
		System.out.println("[" + LocalDateTime.now() + "] " + message);
	}
}


