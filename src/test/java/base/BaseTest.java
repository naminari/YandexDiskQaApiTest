package base;

import config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class BaseTest {
    protected static final String TOKEN = TestConfig.TOKEN;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = TestConfig.BASE_URL;
    }

    protected static void createFolder(String path) {
        int status = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", path)
                .put("/disk/resources")
                .statusCode();

        if (status != 201 && status != 409) {
            fail("Не удалось создать папку " + path + ", статус: " + status);
        }
    }

    protected static void deleteFolder(String path) {
        Response response = given()
                .header("Authorization", "OAuth " + TOKEN)
                .queryParam("path", path)
                .queryParam("permanently", "true")
                .delete("/disk/resources");

        int status = response.statusCode();
        if (status == 202) {
            waitForOperationSuccess(response.path("href"));
        } else if (status != 204 && status != 404) {
            fail("Не удалось удалить папку " + path + ", статус: " + status);
        }
    }

    protected static void waitForOperationSuccess(String href) {
        await()
                .atMost(TestConfig.OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(TestConfig.OPERATION_POLL_INTERVAL_SECONDS, TimeUnit.SECONDS)
                .until(() -> {
                    String state = given()
                            .header("Authorization", "OAuth " + TOKEN)
                            .get(href)
                            .then()
                            .statusCode(200)
                            .extract().path("status");

                    if ("failed".equals(state)) {
                        fail("Операция завершилась с ошибкой, href: " + href);
                    }
                    return "success".equals(state);
                });
    }
}