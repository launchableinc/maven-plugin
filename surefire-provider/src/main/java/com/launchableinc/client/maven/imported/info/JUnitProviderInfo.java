package com.launchableinc.client.maven.imported.info;

import com.launchableinc.client.maven.imported.SurefireDependencyResolver;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import shaded.org.apache.maven.artifact.versioning.ArtifactVersion;
import shaded.org.apache.maven.artifact.versioning.DefaultArtifactVersion;

abstract class JUnitProviderInfo implements ProviderInfo {

    private ArtifactVersion junitDepVersion;

    JUnitProviderInfo(String junitVersion) {
        if (junitVersion != null) {
            junitDepVersion = new DefaultArtifactVersion(junitVersion);
        }
    }

    boolean isAnyJunit4() {
        return SurefireDependencyResolver.isWithinVersionSpec(junitDepVersion, "[4.0,5.0.0)");
    }

    ArtifactVersion getJunitDepVersion() {
        return junitDepVersion;
    }

    @Override
    public ProviderParameters convertProviderParameters(ProviderParameters providerParameters) {
        return providerParameters;
    }
}
