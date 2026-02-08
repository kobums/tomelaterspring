package com.gowoobro.tomelater

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class TomelaterApplication


fun main(args: Array<String>) {
	val dotenv = dotenv {
		directory = if (java.io.File("./tomelater/.env").exists()) "./tomelater/" else "./"
		ignoreIfMalformed = true
		ignoreIfMissing = true
	}
	dotenv.entries().forEach { entry ->
		System.setProperty(entry.key, entry.value)
		println("Loaded env: ${entry.key}")
	}
	
	runApplication<TomelaterApplication>(*args)
}


