package com.launchableinc.client.maven.imported.info;

import com.launchableinc.client.maven.imported.LoaderVersionExtractor;
import org.apache.maven.surefire.providerapi.ProviderParameters;

import static com.launchableinc.client.maven.imported.KnownProvider.*;

/**
 * Lifted from Surefire.
 * 
 * Kohsuke: appear to be a fall back impl in case everything else fails, since JUnit3 supports
 * POJO testing that doesn't depend on anything else
 */
public class JUnit3ProviderInfo implements ProviderInfo {

    public JUnit3ProviderInfo() {
    }

    public String getProviderClassName() {
        return JUNIT_3.getProviderClassName();
    }

    public boolean isApplicable() {
        return true;
    }

    public String getDepCoordinates() {
        return String.join(":", JUNIT_3.getGroupId(), JUNIT_3.getArtifactId(),
            LoaderVersionExtractor.getSurefireApiVersion());
    }

    @Override
    public ProviderParameters convertProviderParameters(ProviderParameters providerParameters) {
        return providerParameters;
    }
}
