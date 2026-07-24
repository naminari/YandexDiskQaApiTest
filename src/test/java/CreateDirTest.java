import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateDirTest extends BaseTest {
    private static String testRoot;

    @BeforeAll
    static void createTestEnvironment() {
        testRoot = "disk:/test_CreateDir_" + System.currentTimeMillis();
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnvironment() {
        deleteFolder(testRoot);
    }

    @Test
    public void testCreateDirPositive() {
        String dirPath = testRoot + "/test_dir";

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .queryParam("path", dirPath)
                .log().all()
                .when()
                .put("/disk/resources")
                .then()
                .log().all()
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
}