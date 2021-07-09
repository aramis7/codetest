package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /service on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void start_http_server_service(Vertx vertx, VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);
        webClient.get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    JsonArray body = response.result().bodyAsJsonArray();
                    assertTrue(body.size()>0);
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /add on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void start_http_server_add(Vertx vertx, VertxTestContext testContext) {
        JsonObject form = createServiceAsJson();

        WebClient webClient = WebClient.create(vertx);
        webClient.post(8080, "::1", "/add")
                .sendJsonObject(form, response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    String body = response.result().bodyAsString();
                    assertEquals("OK", body);
                    testContext.completeNow();
                }));
        webClient.post(8080, "::1", "/add")
                .sendJsonObject(form, response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    String body = response.result().bodyAsString();
                    assertFalse("OK".equals(body));
                    testContext.completeNow();
                }));
    }

    private JsonObject createServiceAsJson() {
        JsonObject form = new JsonObject();
        String alias = "t" + new Random().nextInt();
        form.put("alias", alias);
        form.put("url", "https://www.kry.se");
        return form;
    }


    @Test
    @DisplayName("Start a web server on localhost responding to path /delete on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void start_http_server_delete(Vertx vertx, VertxTestContext testContext) {
        JsonObject form = createServiceAsJson();
        WebClient webClient = WebClient.create(vertx);
        webClient.post(8080, "::1", "/add")
                .sendJsonObject(form, response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    String body = response.result().bodyAsString();
                    assertEquals("OK", body);
                    testContext.completeNow();
                }));
        webClient.post(8080, "::1", "/delete")
                .sendJsonObject(form, response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    String body = response.result().bodyAsString();
                    assertEquals("OK", body);
                    testContext.completeNow();
                }));
    }
}
