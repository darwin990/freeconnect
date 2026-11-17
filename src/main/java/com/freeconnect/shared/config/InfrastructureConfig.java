package com.freeconnect.shared.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.Clock;
import java.util.concurrent.Executor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuracion transversal de infraestructura.
 *
 * <p><b>Patron: Singleton.</b> Spring registra cada {@code @Bean} una sola vez por
 * contenedor; estos objetos (Clock, ObjectMapper, executor, OpenAPI) se comparten
 * de forma segura por todos los modulos, evitando estado global mutable.</p>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({SecurityProperties.class, BootstrapProperties.class, StorageProperties.class})
public class InfrastructureConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /** Executor dedicado para notificaciones asincronas (no bloquea la operacion principal). */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("fc-notify-");
        executor.initialize();
        return executor;
    }

    @Bean
    public OpenAPI freeConnectOpenApi() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Token de acceso JWT de corta duracion");
        return new OpenAPI()
                .info(new Info()
                        .title("FreeConnect API")
                        .version("v1")
                        .description("Plataforma de contratacion freelance. API REST versionada /api/v1")
                        .contact(new Contact().name("FreeConnect Team")))
                .components(new Components().addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
