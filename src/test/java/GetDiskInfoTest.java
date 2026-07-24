import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetDiskInfoTest extends BaseTest {
    private static String testRoot;

    @BeforeAll
    static void setupTestEnv() {
        testRoot = "disk:/test_GetDiskInfo";
        createFolder(testRoot);
    }

    @AfterAll
    static void deleteTestEnv() {
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
                .body("user.login", notNullValue());
    }
}