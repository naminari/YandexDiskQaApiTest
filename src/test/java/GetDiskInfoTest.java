import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetDiskInfoTest extends BaseTest {
    private static String testRoot;

    @BeforeAll
    static void createTestEnvironment() {
        testRoot = "disk:/test_GetDiskInfo_" + System.currentTimeMillis();
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnvironment() {
        deleteFolder(testRoot);
    }

    @Test
    public void testGetDiskInfoPositive() {
        given()
                .header("Authorization", "OAuth " + TOKEN)
                .accept(ContentType.JSON)
                .log().all()
                .when()
                .get("/disk")
                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("total_space", instanceOf(Number.class))
                .body("used_space", instanceOf(Number.class))
                .body("trash_size", instanceOf(Number.class))
                .body("max_file_size", instanceOf(Number.class))
                .body("revision", instanceOf(Number.class))
                .body("system_folders", instanceOf(java.util.Map.class))
                .body("system_folders.downloads", notNullValue())
                .body("system_folders.photostream", notNullValue())
                .body("user", instanceOf(java.util.Map.class))
                .body("user.login", notNullValue())
                .body("user.display_name", notNullValue())
                .body("is_paid", instanceOf(Boolean.class))
                .body("user.uid", not(emptyOrNullString()))
                .body("reg_time", matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*"));
    }
}