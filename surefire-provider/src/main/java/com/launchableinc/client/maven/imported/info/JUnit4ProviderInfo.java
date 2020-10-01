package com.launchableinc.client.maven.imported.info;

import com.launchableinc.client.maven.imported.LoaderVersionExtractor;

import static com.launchableinc.client.maven.imported.KnownProvider.*;

public class JUnit4ProviderInfo extends JUnitProviderInfo {

    public JUnit4ProviderInfo() {
        super(LoaderVersionExtractor.getJunitVersion());
    }

    public String getProviderClassName() {
        return JUNIT_4.getProviderClassName();
    }

    public boolean isApplicable() {
        return getJunitDepVersion() != null && isAnyJunit4() && LoaderVersionExtractor.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return String.join(":", JUNIT_4.getGroupId(), JUNIT_4.getArtifactId(),
            LoaderVersionExtractor.getSurefireApiVersion());
    }
}
