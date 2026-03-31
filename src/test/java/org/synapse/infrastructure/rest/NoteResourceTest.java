package org.synapse.infrastructure.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.synapse.application.dto.CreateNoteCommand;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class NoteResourceTest {

    @Test
    void testCreateAndGetNote() {
        CreateNoteCommand cmd = new CreateNoteCommand("Test REST Title", "Content REST");

        String id = given()
            .contentType(ContentType.JSON)
            .body(cmd)
        .when()
            .post("/api/v1/notes")
        .then()
            .statusCode(201)
            .body("title", equalTo("Test REST Title"))
            .body("content", equalTo("# Test REST Title\n\nContent REST"))
            .extract().path("id");

        given()
        .when()
            .get("/api/v1/notes/" + id)
        .then()
            .statusCode(200)
            .body("title", equalTo("Test REST Title"));
    }
}
