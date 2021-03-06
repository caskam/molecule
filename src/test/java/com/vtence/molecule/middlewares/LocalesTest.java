package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.RequestAssert.assertThat;
import static java.util.Locale.FRENCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LocalesTest {

    Locale DEFAULT_LOCALE = Locale.US;
    Locale originalDefault = Locale.getDefault();
    Locale selected;

    Request request = new Request();
    Response response = new Response();

    @Rule
    public ExpectedException error = ExpectedException.none();

    @Before
    public void setPlatformDefaultLocale() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @After
    public void restoreDefaultPlatformLocale() {
        Locale.setDefault(originalDefault);
    }

    @Test
    public void usesThePlatformLocaleAsTheDefault() throws Exception {
        assertThat(selectPreferred(null, fromSupported()), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void usesTheRequestedLanguageIfSupported() throws Exception {
        assertThat(selectPreferred("fr", fromSupported("en", "fr")), equalTo(FRENCH));
    }

    @Test
    public void usesTheHighestQualityLanguageSupported() throws Exception {
        assertThat(selectPreferred("en; q=0.8, fr", fromSupported("en", "fr")), equalTo(FRENCH));
    }

    @Test
    public void fallsBackToPlatformDefaultForUnsupportedLanguages() throws Exception {
        assertThat(selectPreferred("es-ES", fromSupported("en", "fr")), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void ignoresMalformedLanguageTags() throws Exception {
        assertThat(selectPreferred("-fr-", fromSupported("en", "fr")), equalTo(DEFAULT_LOCALE));
    }

    @Test public void
    unbindsPreferredLocaleOnceDone() throws Exception {
        Locales locales = new Locales();
        locales.handle(request, response);

        assertThat(request).hasAttribute(Locale.class, notNullValue());
        response.done();

        assertNoExecutionError();
        assertThat(request).hasNoAttribute(Locale.class);
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }

    @Test public void
    unbindsPreferredLocaleInCaseOfDeferredErrors() throws Exception {
        Locales locales = new Locales();
        locales.handle(request, response);

        response.done(new Exception("Error!"));
        assertThat(request).hasNoAttribute(Locale.class);
    }

    @Test public void
    unbindsPreferredLocaleWhenAnErrorOccurs() throws Exception {
        Locales locales = new Locales();
        locales.connectTo((request, response) -> {
            throw new Exception("Error!");
        });

        error.expectMessage("Error!");
        try {
            locales.handle(request, response);
        } finally {
            assertThat(request).hasNoAttribute(Locale.class);
        }
    }

    private Locale selectPreferred(String accepted, String... supported) throws Exception {
        Locales locales = new Locales(supported);
        locales.connectTo((request, response) -> selected = request.attribute(Locale.class));
        locales.handle(request.header("Accept-Language", accepted), response);

        return selected;
    }

    private String[] fromSupported(String... supported) {
        return supported;
    }
}