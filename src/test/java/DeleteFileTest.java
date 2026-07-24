import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteFileTest extends BaseTest {
    private static String testRoot;
    private static final int LARGE_FILE_SIZE_BYTES = 2 * 1024 * 1024;

    @BeforeAll
    static void setupTestEnv() {
        testRoot = "disk:/test_DeleteFile";
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnv() {
        deleteFolder(testRoot);
    }

    private static void uploadFile(String path, int sizeBytes) {
        Response uploadUrlResponse = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", path)
                .queryParam("overwrite", "true")
                .when()
                .get("/disk/resources/upload")
                .then()
                .statusCode(200)
                .extract().response();

        String uploadHref = uploadUrlResponse.path("href");
        byte[] content = new byte[sizeBytes];

        given()
                .contentType(ContentType.BINARY)
                .body(content)
                .when()
                .put(uploadHref)
                .then()
                .statusCode(201);
    }

    private static void deleteAndVerifyGone(String path) {
        Response delete = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", path)
                .queryParam("permanently", true)
                .when()
                .delete("/disk/resources");

        int status = delete.statusCode();
        if (status == 202) {
            waitForOperationSuccess(delete.path("href"));
        } else if (status != 204) {
            fail("Неожиданный статус при delete: " + status);
        }

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", path)
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteEmptyFile() {
        String filePath = testRoot + "/test_0b.zip";
        uploadFile(filePath, 0);
        deleteAndVerifyGone(filePath);
    }

    @Test
    public void testDeleteLargeFilePositive() {
        String filePath = testRoot + "/test_20mb_async.zip";
        uploadFile(filePath, LARGE_FILE_SIZE_BYTES);
        deleteAndVerifyGone(filePath);
    }

    @Test
    public void testDeleteFileEmptyPath() {
        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", "")
                .queryParam("permanently", true)
                .when()
                .delete("/disk/resources")
                .then()
                .statusCode(400);
    }

    @Test
    public void testDeleteNonExistentFile() {
        String filePath = testRoot + "/no_such_file.zip";

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .queryParam("permanently", true)
                .when()
                .delete("/disk/resources")
                .then()
                .statusCode(404);
    }

    @Test
    public void testMoveFileToTrash() {
        String fileName = "test_move_to_trash.zip";
        String filePath = testRoot + "/" + fileName;
        uploadFile(filePath, 0);

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .queryParam("permanently", false)
                .when()
                .delete("/disk/resources")
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", "/")
                .when()
                .get("/disk/trash/resources")
                .then()
                .statusCode(200)
                .body("_embedded.items.name", hasItem(fileName));

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("permanently", true)
                .when()
                .delete("/disk/trash/resources");
    }

    @Test
    public void testRepeatedDeleteDuringAsyncOperation() {
        String filePath = testRoot + "/test_repeat_delete.zip";
        uploadFile(filePath, LARGE_FILE_SIZE_BYTES);

        Response firstDelete = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .queryParam("permanently", true)
                .when()
                .delete("/disk/resources");

        Response secondDelete = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", filePath)
                .queryParam("permanently", true)
                .when()
                .delete("/disk/resources");

        int secondStatus = secondDelete.statusCode();

        assertTrue(secondStatus == 202 || secondStatus == 409 || secondStatus == 404,
                "Неожиданный статус при повторном delete: " + secondStatus);

        if (firstDelete.statusCode() == 202) {
            waitForOperationSuccess(firstDelete.path("href"));
        }
    }
}