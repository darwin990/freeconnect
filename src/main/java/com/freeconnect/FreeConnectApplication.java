package com.freeconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada del monolito modular FreeConnect.
 *
 * <p>Los modulos (identity, profiles, projects, proposals, contracts, messaging,
 * notifications, reviews, admin, payments, audit) se comunican mediante puertos
 * de aplicacion y eventos de dominio, nunca mediante acceso cruzado a repositorios.</p>
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class FreeConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreeConnectApplication.class, args);
    }
}
