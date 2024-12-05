package net.megogo.api;

import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import retrofit2.Response;

public class ApiTestsFixture {

    protected MegogoRestClient megogoRestClient;

    @BeforeClass
    public void beforeClass() {
        megogoRestClient = new MegogoRestClient();
    }

    @Step("Assert API Request")
    protected void assertApiRequest(Response<?> response){
        Assert.assertTrue(response.isSuccessful(), "API request failed");
        Assert.assertEquals(response.code(), 200, "Received an unexpected HTTP status code");
        Assert.assertNotNull(response.body(), "Request body is null, but it was expected to contain specific information");
    }

}
