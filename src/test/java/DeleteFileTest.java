import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import groovyjarjarantlr4.v4.parse.ANTLRParser.finallyClause_return;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteFileTest extends BaseTest {
    private static String testRoot;
    private static final String LARGE_FILE_URL = "http://speedtest.tele2.net/20MB.zip";

    @BeforeAll
    static void createTestEnvironment() {
        testRoot = "disk:/test_DeleteFile_" + System.currentTimeMillis();
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnvironment() {
        deleteFolder(testRoot);
    }

    @Test
    public void testDeleteLargeFileInRecucleBinPositive() {
        String filePath = testRoot + "/test_20mb_async.zip";

        Response upload = given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("url", LARGE_FILE_URL)
                .queryParam("path", filePath)
                .log().all()
                .when()
                .post("/disk/resources/upload")
                .then()
                .log().ifValidationFails()
                .statusCode(202)
                .extract().response();

        String uploadHref = upload.path("href");
        waitForOperationSuccess(uploadHref);

        Response delete = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .log().all()
                .when()
                .delete("/disk/resources")
                .then()
                .log().all()
                .extract().response();

        if (delete.statusCode() == 202) {
            String deleteHref = delete.path("href");
            waitForOperationSuccess(deleteHref);
        } else if (delete.statusCode() != 204) {
            fail("Неожиданный статус при удалении: " + delete.statusCode());
        }

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(404);
    }

}