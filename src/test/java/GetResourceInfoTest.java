import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class GetResourceInfoTest extends BaseTest {
    private static String testRoot;
    private static String dirPath;

    @BeforeAll
    static void setupTestEnv() {
        testRoot = "disk:/test_GetResourceMeta";
        dirPath = testRoot + "/meta_dir";
        createFolder(testRoot);
        createFolder(dirPath);
    }

    @AfterAll
    static void deleteTestEnv() {
        deleteFolder(testRoot);
    }

    @Test
    public void testGetResourceInfoPositive() {

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", dirPath)
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(200)
                .body("type", equalTo("dir"))
                .body("path", equalTo(dirPath))
                .body("name", equalTo("meta_dir"));
    }

    @Test
    public void testGetResourceInfoWithFilter() {

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", dirPath)
                .queryParam("fields", "name,type")
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(200)
                .body("name", equalTo("meta_dir"))
                .body("type", equalTo("dir"))
                .body("path", nullValue())
                .body("created", nullValue());
    }

    @Test
    public void testGetResourceInfoNonExistingPath() {
        String missingPath = testRoot + "/no_such_resource";

        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", missingPath)
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetResourceInfoEmptyPath() {
        given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", "")
                .when()
                .get("/disk/resources")
                .then()
                .statusCode(400);
    }
}