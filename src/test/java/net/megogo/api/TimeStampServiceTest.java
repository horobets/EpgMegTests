package net.megogo.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import net.megogo.api.models.TimestampData;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Epic("Current Time Provider Endpoint")
@Feature("Current Time Provider")
public class TimeStampServiceTest extends ApiTestsFixture {

    @Test
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
    private void assertTimestamp(long actualTimestamp, long expectedTimestamp, long acceptableDiscrepancy){
        Assert.assertTrue(Math.abs(actualTimestamp - expectedTimestamp) <= acceptableDiscrepancy,
                "Timestamp differs more than %dms. Actual: %d; Expected: %d".formatted(acceptableDiscrepancy, actualTimestamp, expectedTimestamp));
    }

    @Step("Assert Timestamp Data Format")
    private void assertTimestampDataFormat(TimestampData timestampData){
        var softAssert = new SoftAssert();
        softAssert.assertNotNull(timestampData.timestamp(), "timestamp is missing in the response");
        softAssert.assertNotNull(timestampData.timestampGmt(), "timestampGmt is missing in the response");
        softAssert.assertNotNull(timestampData.timestampLocal(), "timestampLocal is missing in the response");
        softAssert.assertNotNull(timestampData.utcOffset(), "timeLocal is missing in the response");
        softAssert.assertNotNull(timestampData.timeLocal(), "timeLocal is missing in the response");
        softAssert.assertNotNull(timestampData.timezone(), "timezone is missing in the response");

        softAssert.assertEquals(timestampData.timestampGmt() + timestampData.utcOffset(), timestampData.timestampLocal().intValue(),
                "Unexpected timestampLocal");

        var zonedDateTimeFromTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestampData.timestampGmt()), ZoneId.of(timestampData.timezone()));
        Assert.assertEquals(timestampData.timeLocal(),
                zonedDateTimeFromTimestamp.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu h:mm:ss a XXX", Locale.ENGLISH)),
                "Date Time Format mismatch");
    }
}
