package com.launchableinc.client.maven;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchableinc.client.maven.imported.ProviderParametersParser;
import com.launchableinc.client.maven.imported.SurefireProviderFactory;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Entry point.
 */
@MetaInfServices
public class Launchable implements SurefireProvider {
    private final ProviderParameters providerParameters;
    private final SurefireProviderFactory surefireProviderFactory;
    private final ProviderParametersParser paramParser;

    private SurefireProvider delegate;

    public Launchable(ProviderParameters providerParameters) {
        this.providerParameters = providerParameters;
        this.paramParser = new ProviderParametersParser(this.providerParameters);
        // 2nd parameter being null is a scaffold until we figure out how to deal with custom SurefireProvider
        // see how Smart Testing deal with this at http://arquillian.org/smart-testing/#_custom_surefire_providers
        this.surefireProviderFactory = new SurefireProviderFactory(this.paramParser, null);
        this.delegate = surefireProviderFactory.createInstance(providerParameters);
    }

    public TestsToRun optimize(Iterable<Class<?>> src) {
        List<Class<?>> all = new ArrayList<>();
        src.forEach(all::add);
        List<String> result = null;
        try {
            info("LAUNCHABLE: START");
            result = getResult(getJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Comparator<Class<?>> comparator = new InferenceComparator(result);

        info("LAUNCHABLE: REORDERD");
        for (String name : result ) {
            info(name);
        }
        all.sort(comparator);
        return new TestsToRun(new ListBackedBySet<>(all));
    }

    @Override
    public TestsToRun getSuites() {
        return optimize(delegate.getSuites());
    }

    @Override
    public RunResult invoke(Object forkTestSet) throws TestSetFailedException, ReporterException, InvocationTargetException {
        final TestsToRun orderedTests = getTestsToRun(forkTestSet);

        // SurefireProvider.invoke() contract stipulates that we create a new instance
        this.delegate = surefireProviderFactory.createInstance(providerParameters);
        return delegate.invoke(orderedTests);
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    private TestsToRun getTestsToRun(Object forkTestSet) throws TestSetFailedException {
        if (forkTestSet instanceof TestsToRun) {
            return (TestsToRun) forkTestSet;
        } else if (forkTestSet instanceof Class) {
            return TestsToRun.fromClass((Class<?>) forkTestSet);
        } else {
            return getSuites();
        }
    }


    // In the Surefire 2.20 there was changed the surefire logger class from ConsoleLogger to ConsoleStream.
    // The method in ProviderParameters class has the same name, just the return type is different
    // SEE:  https://github.com/arquillian/smart-testing/issues/201
    Object logger;
    Method infoMethod;
    void info(String message) {
        try {
            if(logger == null || infoMethod == null) {
                Class<? extends ProviderParameters> providerParametersClass = providerParameters
                    .getClass();
                Method getConsoleLoggerMethod = providerParametersClass
                    .getMethod("getConsoleLogger", null);
                // ConsoleLogger or ConsoleStream
                logger = getConsoleLoggerMethod.invoke(providerParameters, null);
            }
            infoMethod = logger.getClass().getMethod("info", String.class);
            infoMethod.invoke(logger, message);
        } catch (ReflectiveOperationException e) {
            // Nothing to do
            e.printStackTrace();
        }
    }

    List<String> getResult(String json) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("http://localhost:8080/intake/"); // TODO Fix url
            post.addHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json));
            try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String result = EntityUtils.toString(httpResponse.getEntity());
                    ObjectMapper mapper = new ObjectMapper();
                    return Arrays.asList(mapper.readValue(result, String[].class));
                }
            }
        }
        return Collections.emptyList();
    }

    String getJson() throws IOException {
        Path file = Paths.get("/Users/yoshiori/src/github.com/launchableinc/mothership/research/mercury/test_data/test_data.json"); // TODO get path from options
        return Files.readAllLines(file).get(0);
    }
}
