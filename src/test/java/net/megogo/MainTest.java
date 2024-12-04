package net.megogo;

import io.qameta.allure.Step;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class MainTest {

    private static final Logger log = Logger.getLogger(MainTest.class);

    @Test
    public void sampleTet(){

        doM();
        Assert.assertTrue(true);

    }

    @Step("Do important action")
    private void doM(){

        System.gc();

    }


}