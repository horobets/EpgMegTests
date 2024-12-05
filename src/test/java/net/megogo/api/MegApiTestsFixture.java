package net.megogo.api;

import org.testng.annotations.BeforeClass;

public class MegApiTestsFixture {

    protected MegogoRestClient megogoRestClient;

    @BeforeClass
    public void beforeClass() {
        megogoRestClient = new MegogoRestClient();
    }
}
