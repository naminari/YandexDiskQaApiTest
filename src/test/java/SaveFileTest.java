import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class SaveFileTest extends BaseTest {
    private static String testRoot;
    private static final String TEST_FILE_URL = "http://speedtest.tele2.net/1MB.zip";

    @BeforeAll
    static void createTestEnvironment() {
        testRoot = "disk:/test_SaveFile_" + System.currentTimeMillis();
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnvironment() {
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
        System.out.println("Операция загрузки: " + operationHref);

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
}