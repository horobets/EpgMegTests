package net.megogo.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import net.megogo.api.models.Program;
import net.megogo.api.utils.RequestUtilInterceptor;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Epic("Channel Data Querying")
public class QueryingChannelDataTest extends MegApiTestsFixture {

    private static final Logger logger = Logger.getLogger(QueryingChannelDataTest.class);

    @Test
    @Feature("Current Time Provider")
    @Story("Return the Accurate Current Time")
    public void sampleTest() throws IOException {

        var startTime = Instant.now();
        var timeResult = megogoRestClient.megogoScheduleService.getTime().execute().body();
        var endTime = Instant.now();

        logger.info("%nStart time: %s  End time: %s%nReturned time: %s%n".formatted( startTime.getEpochSecond(), endTime.getEpochSecond(),timeResult));

        doM();

        Assert.assertTrue(true);
    }

    @Test(dataProvider = "test-channels-list")
    @Parameters({"channelId"})
    @Feature("Channel Data Provider")
    @Story("Check Channel Data Sorting")
    public void channelDataSortingTest(String channelId) throws IOException {
        var channelDataRequestResult = megogoRestClient.megogoScheduleService.getChannel(channelId).execute();
        Assert.assertTrue(channelDataRequestResult.isSuccessful(), "Get channel data request failed");
        Assert.assertEquals(channelDataRequestResult.code(), 200, "Received an unexpected HTTP status code");
        Assert.assertNotNull(channelDataRequestResult.body(), "Request body is null, but it was expected to contain specific information");
        channelDataRequestResult.body().data().forEach(channelData -> assertProgramsSorted(channelData.programs()));
    }

    @Step("Assert programs sorted")
    private void assertProgramsSorted(List<Program> programs){
        var sortedPrograms = new ArrayList<Program>(programs);
        sortedPrograms.sort(Comparator.comparingInt(Program::startTimestamp));
        Assert.assertEquals(programs, sortedPrograms, "The programs are not sorted as expected");
    }

    @DataProvider(name = "test-channels-list",parallel=true)
    public Object[][] channelsTestData() {
        return new Object[][] {
                new Object[] { "1639111" },
                new Object[] { "1585681" },
                new Object[] { "1639231" }
        };
    }

    @Test
    @Feature("Channel Schedule")
    @Story("Error On Missing Channel Id")
    public void errorOnMissingIdTest() throws IOException {

        var channelRequestResult = megogoRestClient.megogoScheduleService.getChannel(null).execute();
        Assert.assertFalse(channelRequestResult.isSuccessful(), "The request succeeded, but a failure was expected");
        Assert.assertEquals(channelRequestResult.code(), 400, "Received an unexpected HTTP status code");
        Assert.assertNotNull(channelRequestResult.errorBody(), "The error body is null, but it was expected to contain specific information");
    }

    @Step("Do important action")
    private void doM(){

        System.gc();

    }


}