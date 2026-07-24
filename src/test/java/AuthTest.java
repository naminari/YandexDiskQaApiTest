
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;

public class AuthTest extends BaseTest {

    @Test
    public void testRequestWithoutAuthHeader() {
        given()
                .when()
                .get("/disk")
                .then()
                .statusCode(401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid_token_garbage", ""})
    public void testRequestWithInvalidToken(String badToken) {
        given()
                .header("Authorization", "OAuth " + badToken)
                .when()
                .get("/disk")
                .then()
                .statusCode(401);
    }
}