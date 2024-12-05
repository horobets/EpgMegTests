package net.megogo;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import net.megogo.api.MegogoRestClient;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

@Epic("Channel Data Querying")
public class QueryingChannelDataTest {

    private static final Logger log = Logger.getLogger(QueryingChannelDataTest.class);
    private MegogoRestClient megogoRestClient;

    @BeforeClass
    public void beforeClass() {
        megogoRestClient = new MegogoRestClient();
    }

    @Test
    @Feature("User critical features")
    @Story("Processing Time")
    public void sampleTet() throws IOException {
        var timeResult = megogoRestClient.megogoScheduleService.getTime().execute().body();
        System.out.println(timeResult);
        doM();

        Assert.assertTrue(true);
    }

    @Test
    @Feature("Channel Data Sorting")
    @Story("Check Channel Data Sorting")
    public void channelDataSortingTest() throws IOException {
        var channelResults = megogoRestClient.megogoScheduleService.getChannel("1639111,1585681").execute().body();
        System.out.println(channelResults);
        doM();

        Assert.assertTrue(true);
    }

    @Test
    @Feature("Channel Schedule")
    @Story("Error On Missing Channel Id")
    public void errorOnMissingIdTest() throws IOException {

        var channelRequestResult = megogoRestClient.megogoScheduleService.getChannel("").execute();
        Assert.assertFalse(channelRequestResult.isSuccessful(), "The request succeeded, but a failure was expected");
        Assert.assertEquals(channelRequestResult.code(), 400, "Received an unexpected HTTP status code");
        Assert.assertNotNull(channelRequestResult.errorBody(), "The error body is null, but it was expected to contain specific information");
    }

    @Step("Do important action")
    private void doM(){

        System.gc();

    }


}