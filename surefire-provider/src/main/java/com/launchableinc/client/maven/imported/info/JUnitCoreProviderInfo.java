package com.launchableinc.client.maven.imported.info;

import com.launchableinc.client.maven.imported.LoaderVersionExtractor;
import com.launchableinc.client.maven.imported.ProviderParametersParser;
import com.launchableinc.client.maven.imported.SurefireDependencyResolver;
import com.launchableinc.client.maven.imported.Validate;
import org.apache.maven.surefire.booter.ProviderParameterNames;
import shaded.org.apache.maven.artifact.versioning.ArtifactVersion;

import static com.launchableinc.client.maven.imported.KnownProvider.*;

public class JUnitCoreProviderInfo extends JUnitProviderInfo {

    private final ProviderParametersParser paramParser;

    public JUnitCoreProviderInfo(ProviderParametersParser paramParser) {
        super(LoaderVersionExtractor.getJunitVersion());
        this.paramParser = paramParser;
    }

    public String getProviderClassName() {
        return JUNIT_47.getProviderClassName();
    }

    private boolean is47CompatibleJunitDep() {
        return getJunitDepVersion() != null && isJunit47Compatible(getJunitDepVersion());
    }

    public boolean isApplicable() {
        ArtifactVersion junitDepVersion = getJunitDepVersion();
        if (junitDepVersion == null) {
            return false;
        }
        final boolean isJunitArtifact47 = isAnyJunit4() && isJunit47Compatible(junitDepVersion);
        final boolean isAny47ProvidersForcers = isAnyConcurrencySelected() || isAnyGroupsSelected();
        return isAny47ProvidersForcers && (isJunitArtifact47 || is47CompatibleJunitDep())
            && LoaderVersionExtractor.getSurefireApiVersion() != null;
    }

    public String getDepCoordinates() {
        return String.join(":", JUNIT_47.getGroupId(), JUNIT_47.getArtifactId(),
            LoaderVersionExtractor.getSurefireApiVersion());
    }

    private boolean isJunit47Compatible(ArtifactVersion artifactVersion) {
        return SurefireDependencyResolver.isWithinVersionSpec(artifactVersion, "[4.7,)");
    }

    protected boolean isAnyConcurrencySelected() {
        String parallel = paramParser.getProperty("parallel");
        return parallel != null && parallel.trim().length() > 0 && !parallel.equals("none");
    }

    protected boolean isAnyGroupsSelected() {
        String groups = paramParser.getProperty(ProviderParameterNames.TESTNG_GROUPS_PROP);
        String excludeGroups = paramParser.getProperty(ProviderParameterNames.TESTNG_EXCLUDEDGROUPS_PROP);
        return Validate.isNotEmpty(groups) || Validate.isNotEmpty(excludeGroups);
    }
}
