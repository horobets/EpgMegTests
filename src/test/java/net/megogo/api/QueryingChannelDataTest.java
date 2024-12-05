package net.megogo.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import net.megogo.api.models.ChannelData;
import net.megogo.api.models.Program;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

@Epic("Channel Data Querying")
@Feature("Channel Schedule")
public class QueryingChannelDataTest extends ApiTestsFixture {

    @DataProvider(name = "test-channels-list", parallel=true)
    public Object[][] channelsTestData() {
        return new Object[][] {
                new Object[] { "1639111" },
                new Object[] { "1585681" },
                new Object[] { "1639231" }
        };
    }

    @Test(dataProvider = "test-channels-list")
    @Parameters({"channelId"})
    @Story("Channel Data Sorting")
    public void channelDataSortingTest(String channelId) throws IOException {
        var channelDataRequestResult = megogoRestClient.megogoScheduleService.getChannel(channelId).execute();
        assertApiRequest(channelDataRequestResult);

        channelDataRequestResult.body().data().forEach(
                this::assertChannelProgramsSorted);
    }

    @Test(dataProvider = "test-channels-list")
    @Parameters({"channelId"})
    @Story("Channel Contains Active Program")
    public void channelContainsCurrentProgram(String channelId) throws IOException {
        var channelDataRequestResult = megogoRestClient.megogoScheduleService.getChannel(channelId).execute();
        assertApiRequest(channelDataRequestResult);

        channelDataRequestResult.body().data().forEach(
                this::assertChannelHasActiveProgram);
    }

    @Test(dataProvider = "test-channels-list")
    @Parameters({"channelId"})
    @Story("Channel schedule excludes programs from the past and those scheduled more than 24h ahead")
    public void channelExcludesOutdatedAndFutureProgramsTest(String channelId) throws IOException {
        var channelDataRequestResult = megogoRestClient.megogoScheduleService.getChannel(channelId).execute();
        assertApiRequest(channelDataRequestResult);

        channelDataRequestResult.body().data().forEach(
                this::assertChannelHasNoOutdatedAndFuturePrograms);
    }


    @Step("Assert programs are sorted by start_timestamp field")
    private void assertChannelProgramsSorted(ChannelData channelData){
        var sortedPrograms = new ArrayList<Program>(channelData.programs());
        sortedPrograms.sort(Comparator.comparingInt(Program::startTimestamp));
        Assert.assertEquals(channelData.programs(), sortedPrograms,
                "The programs are not sorted as expected (should be asc by start_timestamp field)");
    }

    @Step("Assert channel has currently active program")
    private void assertChannelHasActiveProgram(ChannelData channelData){
        Assert.assertTrue(channelData.programs().stream().anyMatch(program ->
                        Instant.ofEpochSecond(program.startTimestamp()).isBefore(Instant.now()) &&
                                Instant.ofEpochSecond(program.endTimestamp()).isAfter(Instant.now())),
                "No currently active programs found in the channel: %s".formatted(channelData.id()));
    }

    @Step("Assert channel has no outdated and more than 24h ahead programs")
    private void assertChannelHasNoOutdatedAndFuturePrograms(ChannelData channelData){
        Assert.assertTrue(channelData.programs().stream().noneMatch(program ->
                Instant.ofEpochSecond(program.startTimestamp()).isAfter(Instant.now().plus(24, ChronoUnit.HOURS))),
                "An invalid program scheduled more than 24 hours in the future was found in the channel: %s".formatted(channelData.id()));

        Assert.assertTrue(channelData.programs().stream().noneMatch(program ->
                Instant.ofEpochSecond(program.endTimestamp()).isBefore(Instant.now())),
                "An invalid program scheduled in the past was found in the channel: %s".formatted(channelData.id()));
    }

}