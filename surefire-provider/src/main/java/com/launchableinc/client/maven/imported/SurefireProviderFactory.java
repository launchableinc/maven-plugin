package com.launchableinc.client.maven.imported;

import com.launchableinc.client.maven.imported.info.JUnit3ProviderInfo;
import com.launchableinc.client.maven.imported.info.JUnit4ProviderInfo;
import com.launchableinc.client.maven.imported.info.JUnitCoreProviderInfo;
import com.launchableinc.client.maven.imported.info.ProviderInfo;
import com.launchableinc.client.maven.imported.info.TestNgProviderInfo;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;

import java.io.File;

public class SurefireProviderFactory {

    private final ProviderInfo providerInfo;
    private final Class<SurefireProvider> surefireProviderClass;

    public SurefireProviderFactory(ProviderParametersParser paramParser, File projectDir) {
        providerInfo = detectProvider(paramParser, projectDir);
        surefireProviderClass = loadProviderClass();
    }

    private ProviderInfo detectProvider(
        ProviderParametersParser paramParser, File projectDir) {
        ProviderInfo[] wellKnownProviders = new ProviderInfo[] {
//            new CustomProviderInfo(projectDir),       // TODO
            new TestNgProviderInfo(),
            new JUnitCoreProviderInfo(paramParser),
            new JUnit4ProviderInfo(),
            new JUnit3ProviderInfo(),
        };
        return autoDetectOneProvider(wellKnownProviders);
    }

    public SurefireProvider createInstance(ProviderParameters providerParameters) {
        return SecurityUtils.newInstance(surefireProviderClass, new Class[] {ProviderParameters.class},
            new Object[] {providerInfo.convertProviderParameters(providerParameters)});
    }

    @SuppressWarnings("unchecked")
    private Class<SurefireProvider> loadProviderClass() {
        try {
            ClassLoader classLoader = SurefireDependencyResolver.addProviderToClasspath(providerInfo);
            if (classLoader != null) {
                return (Class<SurefireProvider>) classLoader.loadClass(providerInfo.getProviderClassName());
            }
            return null;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private ProviderInfo autoDetectOneProvider(ProviderInfo[] providers) {
        for (ProviderInfo provider : providers) {
            if (provider.isApplicable()) {
                return provider;
            }
        }
        throw new IllegalStateException(
            "No surefire provider implementation has been detected as applicable for your environment.");
    }
}
