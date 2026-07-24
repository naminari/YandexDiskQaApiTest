import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.charset.StandardCharsets;

public class SaveFileTest extends BaseTest {
    private static String testRoot;
    private static final String TEST_FILE_URL = "http://speedtest.tele2.net/1MB.zip";

    @BeforeAll
    static void setupTestEnv() {
        testRoot = "disk:/test_SaveFile";
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnv() {
        deleteFolder(testRoot);
    }

    @Test
    public void testSaveNotEmptyFilePositive() {
        String filePath = testRoot + "/test_1mb.zip";

        Response uploadResponse = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .queryParam("url", TEST_FILE_URL)
                .queryParam("disable_redirects", "false")
                .log().all()
                .when()
                .post("/disk/resources/upload")
                .then()
                .log().ifValidationFails()
                .statusCode(202)
                .body("href", notNullValue())
                .body("method", equalTo("GET"))
                .extract().response();

        String operationHref = uploadResponse.path("href");
        waitForOperationSuccess(operationHref);

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .log().ifValidationFails()
                .when()
                .get("/disk/resources")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("path", equalTo(filePath))
                .body("type", equalTo("file"));
    }

    @Test
    public void testSaveFileFromInvalidUrl() {
        String filePath = testRoot + "/test_invalid_url.zip";
        String brokenUrl = "http://speedtest.tele2.net/not_existed_file_404.zip";

        Response upload = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .queryParam("url", brokenUrl)
                .when()
                .post("/disk/resources/upload")
                .then()
                .log().ifValidationFails()
                .statusCode(202)   // ????
                .extract().response();

        String href = upload.path("href");

        assertThrows(AssertionError.class, () -> waitForOperationSuccess(href));
    }

    @Test
    public void testSaveFileDirectUpload() {
        String filePath = testRoot + "/test_direct_upload.txt";

        Response uploadUrlResponse = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .queryParam("overwrite", "true")
                .when()
                .get("/disk/resources/upload")
                .then()
                .statusCode(200)
                .body("href", notNullValue())
                .extract().response();

        String uploadHref = uploadUrlResponse.path("href");
        byte[] content = "test file content for direct upload".getBytes(StandardCharsets.UTF_8);

        given()
                .contentType(ContentType.BINARY)
                .body(content)
                .when()
                .put(uploadHref)
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(200)
                .body("path", equalTo(filePath))
                .body("type", equalTo("file"))
                .body("size", equalTo(content.length));
    }
}