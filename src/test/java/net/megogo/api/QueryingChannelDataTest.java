package net.megogo.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import net.megogo.api.models.ChannelData;
import net.megogo.api.models.Program;
import net.megogo.api.models.TimestampData;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

@Epic("Channel Data Querying")
public class QueryingChannelDataTest extends MegApiTestsFixture {

    private static final Logger logger = Logger.getLogger(QueryingChannelDataTest.class);

    @DataProvider(name = "test-channels-list", parallel=true)
    public Object[][] channelsTestData() {
        return new Object[][] {
                new Object[] { "1639111" },
                new Object[] { "1585681" },
                new Object[] { "1639231" }
        };
    }

    @Test
    @Feature("Current Time Provider")
    @Story("Return the Accurate Current Time")
    public void accurateCurrentTimestampTest() throws IOException {

        var timeRequestResult = megogoRestClient.megogoScheduleService.getTime().execute();
        assertApiRequest(timeRequestResult);

        var returnedTimeData = timeRequestResult.body();
        var localTimestamp = Instant.now().getEpochSecond();

        assertTimestamp(returnedTimeData.data().timestamp(), localTimestamp, 10);
        assertTimestampDataFormat(returnedTimeData.data());

    }

    @Step("Assert Timestamp")
    void assertTimestamp(long actualTimestamp, long expectedTimestamp, long acceptableDiscrepancy){
        Assert.assertTrue(Math.abs(actualTimestamp - expectedTimestamp) <= acceptableDiscrepancy, "Timestamp differs more than %dms. Actual: %d; Expected: %d".formatted(acceptableDiscrepancy, actualTimestamp, expectedTimestamp));
    }

    @Step("Assert Timestamp Data Format")
    void assertTimestampDataFormat(TimestampData timestampData){
        var softAssert = new SoftAssert();
        softAssert.assertNotNull(timestampData.timestamp(), "timestamp is missing in the response");
        softAssert.assertNotNull(timestampData.timestampGmt(), "timestampGmt is missing in the response");
        softAssert.assertNotNull(timestampData.timestampLocal(), "timestampLocal is missing in the response");
        softAssert.assertNotNull(timestampData.utcOffset(), "timeLocal is missing in the response");
        softAssert.assertNotNull(timestampData.timeLocal(), "timeLocal is missing in the response");
        softAssert.assertNotNull(timestampData.timezone(), "timezone is missing in the response");

        softAssert.assertEquals(timestampData.timestampGmt() + timestampData.utcOffset(), timestampData.timestampLocal().intValue(), "Unexpected timestampLocal");

        var zonedDateTimeFromTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestampData.timestampGmt()), ZoneId.of(timestampData.timezone()));
        Assert.assertEquals(timestampData.timeLocal(), zonedDateTimeFromTimestamp.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu h:mm:ss a XXX", Locale.ENGLISH)), "Date Time Format mismatch");
    }


    @Test(dataProvider = "test-channels-list")
    @Parameters({"channelId"})
    @Feature("Channel Schedule")
    @Story("Check Channel Data Sorting")
    public void channelDataSortingTest(String channelId) throws IOException {
        var channelDataRequestResult = megogoRestClient.megogoScheduleService.getChannel(channelId).execute();
        assertApiRequest(channelDataRequestResult);

        channelDataRequestResult.body().data().forEach(
                this::assertChannelProgramsSorted);
    }

    @Step("Assert programs sorted")
    private void assertChannelProgramsSorted(ChannelData channelData){
        var sortedPrograms = new ArrayList<Program>(channelData.programs());
        sortedPrograms.sort(Comparator.comparingInt(Program::startTimestamp));
        Assert.assertEquals(channelData.programs(), sortedPrograms, "The programs are not sorted as expected");
    }

    @Test(dataProvider = "test-channels-list")
    @Parameters({"channelId"})
    @Feature("Channel Schedule")
    @Story("Check Channel Contains Active Program")
    public void channelContainsCurrentProgram(String channelId) throws IOException {
        var channelDataRequestResult = megogoRestClient.megogoScheduleService.getChannel(channelId).execute();
        assertApiRequest(channelDataRequestResult);

        channelDataRequestResult.body().data().forEach(
                this::assertChannelHasActiveProgram);
    }

    @Step("Assert channel has currently active program")
    private void assertChannelHasActiveProgram(ChannelData channelData){
        Assert.assertTrue(channelData.programs().stream().anyMatch(this::isActiveProgram), "No active program found in channel: %s".formatted(channelData.id()));
    }

    @Step("Check if the program is currently active")
    @Parameters({"program"})
    private boolean isActiveProgram(Program program){
        var currentTimestamp = Instant.now().getEpochSecond();
        return program.startTimestamp() < currentTimestamp && program.endTimestamp() > currentTimestamp;
    }

    @Test(dataProvider = "test-channels-list")
    @Parameters({"channelId"})
    @Feature("Channel Schedule")
    @Story("Channel schedule excludes programs from the past and those scheduled more than 24h ahead")
    public void channelExcludesOutdatedAndFutureProgramsTest(String channelId) throws IOException {
        var channelDataRequestResult = megogoRestClient.megogoScheduleService.getChannel(channelId).execute();
        assertApiRequest(channelDataRequestResult);

        channelDataRequestResult.body().data().forEach(
                this::assertChannelHasNoOutdatedAndFuturePrograms);
    }


    @Step("Assert channel has no outdated and more than 24h ahead programs")
    private void assertChannelHasNoOutdatedAndFuturePrograms(ChannelData channelData){
        Assert.assertTrue(channelData.programs().stream().allMatch(this::isProgramWithingValidTimestampRange), "Invalid program program found in channel: %s".formatted(channelData.id()));
    }

    @Step("Check if the program is withing valid time range")
    @Parameters({"program"})
    private boolean isProgramWithingValidTimestampRange(Program program){
        var lowestValidTimestamp = Instant.now().minus(24, ChronoUnit.HOURS).getEpochSecond();
        var highestValidTimestamp = Instant.now().plus(24, ChronoUnit.HOURS).getEpochSecond();
        return program.startTimestamp() > lowestValidTimestamp && program.startTimestamp() < highestValidTimestamp;
    }

    @Test
    @Feature("Channel Schedule")
    @Story("Report an error if any required parameters are missing in the request.")
    public void errorOnMissingIdTest() throws IOException {

        var channelRequestResult = megogoRestClient.megogoScheduleService.getChannel(null).execute();
        Assert.assertFalse(channelRequestResult.isSuccessful(), "The request succeeded, but a failure was expected");
        Assert.assertEquals(channelRequestResult.code(), 400, "Received an unexpected HTTP status code");
        Assert.assertNotNull(channelRequestResult.errorBody(), "The error body is null, but it was expected to contain specific information");
    }

}