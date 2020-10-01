package com.launchableinc.client.maven.imported.info;

import com.launchableinc.client.maven.imported.LoaderVersionExtractor;
import com.launchableinc.client.maven.imported.StringUtils;
import org.apache.maven.surefire.providerapi.ProviderParameters;

import static com.launchableinc.client.maven.imported.KnownProvider.*;
import static org.apache.maven.surefire.booter.ProviderParameterNames.*;

public class TestNgProviderInfo implements ProviderInfo {


    public TestNgProviderInfo() {
    }

    public String getProviderClassName() {
        return TESTNG.getProviderClassName();
    }

    public boolean isApplicable() {
        return LoaderVersionExtractor.getTestNgVersion() != null
            && LoaderVersionExtractor.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return String.join(":", TESTNG.getGroupId(), TESTNG.getArtifactId(),
            LoaderVersionExtractor.getSurefireApiVersion());
    }

    @Override
    public ProviderParameters convertProviderParameters(ProviderParameters providerParameters) {
        // remove threadcount property where the value is lower than 1 or is not numeric
        // workaround for https://issues.apache.org/jira/browse/SUREFIRE-1398
        String threadCount = providerParameters.getProviderProperties().get(THREADCOUNT_PROP);
        if (!StringUtils.isNumeric(threadCount) || Integer.parseInt(threadCount) < 1) {
            providerParameters.getProviderProperties().remove(THREADCOUNT_PROP);
        }
        return providerParameters;
    }
}
