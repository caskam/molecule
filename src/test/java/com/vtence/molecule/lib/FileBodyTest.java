package com.vtence.molecule.lib;

import com.vtence.molecule.Response;
import com.vtence.molecule.helpers.Streams;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.vtence.molecule.testing.ResourceLocator.onClasspath;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class FileBodyTest {

    File base = onClasspath().locate("assets/images");
    File file = new File(base, "sample.png");
    Response response = new Response();

    @Test public void
    rendersFileContent() throws Exception {
        FileBody body = new FileBody(file);
        assertThat("file", body.file(), sameInstance(file));
        response.body(body);
        assertThat(response).hasBodySize(file.length())
                            .hasBodyContent(contentOf(file));
    }

    private byte[] contentOf(final File file) throws IOException {
        return Streams.consume(new FileInputStream(file));
    }
}