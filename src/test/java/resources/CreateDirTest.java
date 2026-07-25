package resources;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import base.BaseTest;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("resources")
public class CreateDirTest extends BaseTest {
    private static String testRoot;

    @BeforeAll
    static void setupTestEnv() {
        testRoot = "disk:/test_CreateDir";
        deleteFolderIfExists(testRoot);
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnv() {
        deleteFolder(testRoot);
    }

    private static void deleteFolderIfExists(String path) {
        Response response = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", path)
                .queryParam("permanently", "true")
                .delete("/disk/resources");
        int status = response.getStatusCode();
        if (status != 204 && status != 404) {
            if (status == 202) {
                waitForOperationSuccess(response.path("href"));
            } else {
                fail("Не удалось удалить папку " + path + ", статус: " + status);
            }
        }
    }

    @Test
    public void testCreateDirPositive() {
        String dirPath = testRoot + "/test_dir";

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", dirPath)
                .when()
                .put("/disk/resources")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", dirPath)
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(200)
                .body("type", equalTo("dir"));
    }

    @ParameterizedTest
    @MethodSource("invalidPaths")
    public void testCreateDirInvalidPath(String path, int expectedStatus) {
        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", path)
                .when()
                .put("/disk/resources")
                .then()
                .statusCode(expectedStatus);
    }

    private static Stream<Arguments> invalidPaths() {
        return Stream.of(
                Arguments.of("", 400),
                Arguments.of(testRoot + "/" + "a".repeat(256), 404)
        );
    }

    @Test
    public void testCreateDirNameExactlyMaxLength() {
        String dirName = "a".repeat(255);
        String dirPath = testRoot + "/" + dirName;

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", dirPath)
                .when()
                .put("/disk/resources")
                .then()
                .statusCode(201);
    }

    @Test
    public void testCreateDirNameTooLong() {
        String dirPath = testRoot + "/" + "a".repeat(256);

        Response response = given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", dirPath)
                .log().all()
                .when()
                .put("/disk/resources")
                .then()
                .log().all()
                .extract().response();

        response.then().statusCode(404);
    }

    @Test
    public void testCreateDirAlreadyExists() {
        String dirPath = testRoot + "/existing_dir";
        createFolder(dirPath);

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", dirPath)
                .when()
                .put("/disk/resources")
                .then()
                .statusCode(409);
    }

    @Test
    public void testCreateDirInNonExistentParent() {
        String dirPath = testRoot + "/no_such_parent/new_dir";

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", dirPath)
                .when()
                .put("/disk/resources")
                .then()
                .statusCode(409);
    }

    @Test
    public void testCreateDirWithUnicodeName() {
        String dirPath = testRoot + "/Ťěšť_папка";

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", dirPath)
                .when()
                .put("/disk/resources")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", dirPath)
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(200)
                .body("type", equalTo("dir"))
                .body("name", equalTo("Ťěšť_папка"));
    }
}