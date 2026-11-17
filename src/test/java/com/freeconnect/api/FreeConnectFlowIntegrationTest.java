package com.freeconnect.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Prueba de integracion del flujo completo RF-01 a RF-06 mas los modulos
 * transversales (mensajeria, resenas, pagos, notificaciones y administracion).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FreeConnectFlowIntegrationTest {

    private static final String PASSWORD = "FreeConnect#2026Aa";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void completeMarketplaceFlowWorksEndToEnd() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        JsonNode client = postJson("/api/v1/auth/register", null, Map.of(
                "email", "cliente-" + suffix + "@freeconnect.test",
                "password", PASSWORD,
                "displayName", "Cliente Integracion",
                "role", "CLIENT"));
        JsonNode freelancer = postJson("/api/v1/auth/register", null, Map.of(
                "email", "freelancer-" + suffix + "@freeconnect.test",
                "password", PASSWORD,
                "displayName", "Freelancer Integracion",
                "role", "FREELANCER"));

        String clientToken = client.get("accessToken").asText();
        String freelancerToken = freelancer.get("accessToken").asText();
        UUID clientId = UUID.fromString(client.get("user").get("id").asText());
        UUID freelancerId = UUID.fromString(freelancer.get("user").get("id").asText());

        // RF-02: perfil del freelancer
        JsonNode profile = putJson("/api/v1/profiles/me", freelancerToken, Map.of(
                "headline", "Backend Java",
                "bio", "Especialista en Spring Boot",
                "hourlyRate", 45,
                "currency", "USD",
                "availability", "AVAILABLE",
                "skills", List.of("Java", "Spring Boot")));
        UUID profileId = UUID.fromString(profile.get("id").asText());

        // RF-03: publicar y buscar
        JsonNode project = postJson("/api/v1/projects", clientToken, Map.of(
                "title", "API de reservas " + suffix,
                "description", "Construir una API REST con Spring Boot y PostgreSQL",
                "category", "SOFTWARE",
                "budget", 1000,
                "currency", "USD",
                "modality", "REMOTE",
                "deadline", LocalDate.now().plusDays(30).toString(),
                "skills", List.of("Java", "Spring Boot")));
        UUID projectId = UUID.fromString(project.get("id").asText());
        patchJson("/api/v1/projects/" + projectId + "/status", clientToken, Map.of("status", "PUBLISHED"));

        JsonNode found = getJson("/api/v1/projects?q=reservas", null);
        assertTrue(found.get("totalElements").asInt() >= 1);

        // RF-04: propuesta
        JsonNode proposal = postJson("/api/v1/projects/" + projectId + "/proposals", freelancerToken, Map.of(
                "coverLetter", "Tengo 8 anos de experiencia construyendo APIs REST con Spring Boot y PostgreSQL.",
                "amount", 1000,
                "currency", "USD",
                "estimatedDays", 20));
        UUID proposalId = UUID.fromString(proposal.get("id").asText());

        // RF-05: aceptar propuesta y generar contrato con hitos
        JsonNode contract = postJson("/api/v1/proposals/" + proposalId + "/accept", clientToken, Map.of(
                "commissionPlan", "PREMIUM",
                "milestoneType", "EQUAL_SPLIT",
                "parts", 2));
        UUID contractId = UUID.fromString(contract.get("id").asText());
        assertEquals("ACTIVE", contract.get("status").asText());
        assertEquals(2, contract.get("milestones").size());

        // RF-06: entregables, tareas, comentarios y aprobacion
        JsonNode milestoneOne = contract.get("milestones").get(0);
        UUID milestoneOneId = UUID.fromString(milestoneOne.get("id").asText());
        JsonNode withDeliverable = postJson(
                "/api/v1/contracts/" + contractId + "/milestones/" + milestoneOneId + "/deliverables",
                freelancerToken, Map.of("title", "Modulo de autenticacion", "description", "JWT + refresh",
                        "url", "https://example.com/repo/auth"));
        UUID deliverableId = UUID.fromString(withDeliverable.get("milestones").get(0)
                .get("deliverables").get(0).get("id").asText());

        JsonNode withTask = postJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneOneId
                        + "/deliverables/" + deliverableId + "/tasks", freelancerToken,
                Map.of("title", "Implementar login"));
        UUID taskId = UUID.fromString(withTask.get("milestones").get(0).get("deliverables").get(0)
                .get("tasks").get(0).get("id").asText());
        patchJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneOneId + "/deliverables/"
                + deliverableId + "/tasks/" + taskId, freelancerToken, Map.of("done", true));
        postJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneOneId + "/deliverables/"
                + deliverableId + "/comments", clientToken, Map.of("body", "Revisar expiracion del token"));

        postJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneOneId + "/submit",
                freelancerToken, null);
        JsonNode approved = postJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneOneId
                + "/approve", clientToken, null);
        assertEquals("APPROVED", approved.get("milestones").get(0).get("status").asText());

        UUID milestoneTwoId = UUID.fromString(approved.get("milestones").get(1).get("id").asText());
        postJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneTwoId + "/deliverables",
                freelancerToken, Map.of("title", "Modulo de reservas", "description", "CRUD y disponibilidad",
                        "url", "https://example.com/repo/bookings"));
        postJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneTwoId + "/submit",
                freelancerToken, null);
        JsonNode completed = postJson("/api/v1/contracts/" + contractId + "/milestones/" + milestoneTwoId
                + "/approve", clientToken, null);
        assertEquals("COMPLETED", completed.get("status").asText());
        assertEquals(100, completed.get("progress").asInt());

        // Mensajeria
        JsonNode conversation = postJson("/api/v1/contracts/" + contractId + "/conversations", clientToken, null);
        UUID conversationId = UUID.fromString(conversation.get("id").asText());
        postJson("/api/v1/conversations/" + conversationId + "/messages", clientToken,
                Map.of("body", "Buen trabajo, revisemos el cierre."));
        JsonNode replied = postJson("/api/v1/conversations/" + conversationId + "/messages", freelancerToken,
                Map.of("body", "Gracias, quedo atento."));
        assertEquals(2, replied.get("messages").size());
        assertEquals("ARCHIVED", postJson("/api/v1/conversations/" + conversationId + "/archive", clientToken,
                null).get("status").asText());

        // Resenas y reputacion
        JsonNode review = postJson("/api/v1/contracts/" + contractId + "/reviews", clientToken,
                Map.of("rating", 5, "comment", "Entrega puntual"));
        assertEquals(freelancerId.toString(), review.get("targetId").asText());
        JsonNode ratedProfile = getJson("/api/v1/profiles/" + profileId, freelancerToken);
        assertEquals(5, ratedProfile.get("ratingAverage").asInt());
        assertEquals(1, ratedProfile.get("ratingCount").asInt());

        // Pagos
        JsonNode payment = postJson("/api/v1/contracts/" + contractId + "/payments", clientToken,
                Map.of("amount", 1000));
        assertEquals("RELEASED", payment.get("status").asText());
        assertEquals(50.0, payment.get("commission").get("amount").asDouble());
        assertEquals(950.0, payment.get("netAmount").get("amount").asDouble());

        // Notificaciones
        assertTrue(getJson("/api/v1/notifications", clientToken).size() >= 1);
        assertTrue(getJson("/api/v1/notifications", freelancerToken).size() >= 1);

        // Administracion
        JsonNode admin = postJson("/api/v1/auth/login", null,
                Map.of("email", "admin@freeconnect.io", "password", "Admin#FreeConnect2026"));
        String adminToken = admin.get("accessToken").asText();
        assertEquals(3, getJson("/api/v1/admin/reports", adminToken).size());
        assertTrue(getJson("/api/v1/admin/audit", adminToken).size() >= 1);

        JsonNode moderationProject = postJson("/api/v1/projects", clientToken, Map.of(
                "title", "Proyecto para moderar " + suffix,
                "description", "Proyecto temporal",
                "category", "SOFTWARE",
                "budget", 200,
                "currency", "USD",
                "modality", "REMOTE",
                "deadline", LocalDate.now().plusDays(10).toString(),
                "skills", List.of("Java")));
        UUID moderationId = UUID.fromString(moderationProject.get("id").asText());
        patchJson("/api/v1/projects/" + moderationId + "/status", clientToken, Map.of("status", "PUBLISHED"));
        assertEquals("PAUSED", postJson("/api/v1/admin/projects/" + moderationId + "/moderate", adminToken,
                Map.of("status", "PAUSED")).get("status").asText());
        assertEquals("SUSPENDED", postJson("/api/v1/admin/users/" + freelancerId + "/suspend", adminToken,
                null).get("status").asText());

        // El cliente original conserva su identidad
        assertEquals(clientId.toString(), getJson("/api/v1/auth/me", clientToken).get("id").asText());
    }

    @Test
    void proposalLifecycleSupportsEvaluateRejectAndWithdraw() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        JsonNode client = postJson("/api/v1/auth/register", null, Map.of(
                "email", "prop-client-" + suffix + "@freeconnect.test",
                "password", PASSWORD,
                "displayName", "Cliente Propuestas",
                "role", "CLIENT"));
        JsonNode freelancer = postJson("/api/v1/auth/register", null, Map.of(
                "email", "prop-free-" + suffix + "@freeconnect.test",
                "password", PASSWORD,
                "displayName", "Freelancer Propuestas",
                "role", "FREELANCER"));
        String clientToken = client.get("accessToken").asText();
        String freelancerToken = freelancer.get("accessToken").asText();

        putJson("/api/v1/profiles/me", freelancerToken, Map.of(
                "headline", "Backend Java",
                "bio", "Perfil temporal para propuestas",
                "hourlyRate", 40,
                "currency", "USD",
                "availability", "AVAILABLE",
                "skills", List.of("Java")));

        JsonNode project = postJson("/api/v1/projects", clientToken, Map.of(
                "title", "Proyecto de propuestas " + suffix,
                "description", "Validar el ciclo completo de propuestas",
                "category", "SOFTWARE",
                "budget", 600,
                "currency", "USD",
                "modality", "REMOTE",
                "deadline", LocalDate.now().plusDays(15).toString(),
                "skills", List.of("Java")));
        String projectId = project.get("id").asText();
        patchJson("/api/v1/projects/" + projectId + "/status", clientToken, Map.of("status", "PUBLISHED"));

        String proposalBody = "Propuesta de prueba con experiencia suficiente en Spring Boot y PostgreSQL.";
        JsonNode first = postJson("/api/v1/projects/" + projectId + "/proposals", freelancerToken, Map.of(
                "coverLetter", proposalBody, "amount", 600, "currency", "USD", "estimatedDays", 12));
        String firstId = first.get("id").asText();

        JsonNode evaluated = postJson("/api/v1/proposals/" + firstId + "/evaluate", clientToken,
                Map.of("score", 4, "note", "Buen enfoque tecnico"));
        assertEquals(4, evaluated.get("evaluationScore").asInt());

        JsonNode rejected = postJson("/api/v1/proposals/" + firstId + "/reject", clientToken, null);
        assertEquals("REJECTED", rejected.get("status").asText());

        JsonNode second = postJson("/api/v1/projects/" + projectId + "/proposals", freelancerToken, Map.of(
                "coverLetter", proposalBody, "amount", 550, "currency", "USD", "estimatedDays", 10));
        String secondId = second.get("id").asText();
        JsonNode withdrawn = perform(delete("/api/v1/proposals/" + secondId), freelancerToken, null);
        assertEquals("WITHDRAWN", withdrawn.get("status").asText());

        JsonNode list = getJson("/api/v1/projects/" + projectId + "/proposals", clientToken);
        assertTrue(list.size() >= 2);
    }

    @Test
    void protectedEndpointsRejectAnonymousAndNonAdminUsers() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        JsonNode freelancer = postJson("/api/v1/auth/register", null, Map.of(
                "email", "rbac-" + suffix + "@freeconnect.test",
                "password", PASSWORD,
                "displayName", "Sin privilegios",
                "role", "FREELANCER"));
        String token = freelancer.get("accessToken").asText();

        mvc.perform(get("/api/v1/admin/reports").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private JsonNode getJson(String path, String token) throws Exception {
        return perform(get(path), token, null);
    }

    private JsonNode postJson(String path, String token, Object body) throws Exception {
        return perform(post(path), token, body);
    }

    private JsonNode putJson(String path, String token, Object body) throws Exception {
        return perform(put(path), token, body);
    }

    private JsonNode patchJson(String path, String token, Object body) throws Exception {
        return perform(patch(path), token, body);
    }

    private JsonNode perform(MockHttpServletRequestBuilder request, String token, Object body) throws Exception {
        request.contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            request.content(mapper.writeValueAsBytes(body));
        }
        MvcResult result = mvc.perform(request).andExpect(status().is2xxSuccessful()).andReturn();
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return content.isBlank() ? mapper.createObjectNode() : mapper.readTree(content);
    }
}
