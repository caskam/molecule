package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.util.Streams;
import org.junit.Test;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class CompressorTest {

    Compressor compressor = new Compressor();

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Test public void
    deflatesResponseWhenClientAcceptsDeflate() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.withHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
        assertThat("inflated body", inflate(response), equalTo("uncompressed body"));
    }

    @Test public void
    gzipsResponseWhenClientsAcceptsGZip() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.withHeader("Accept-Encoding", "gzip");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "gzip");
        assertThat("inflated body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    usesFirstAcceptedContentCoding() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.withHeader("Accept-Encoding", "gzip", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "gzip");
        assertThat("inflated body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    gracefullyHandlesLackOfContent() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
            }
        });

        request.withHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
    }

    @Test public void
    removesContentLengthHeaderWhenCompressing() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentLength(128);
                response.body("uncompressed body...");
            }
        });

        request.withHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Length", nullValue());
    }

    @Test public void
    fallsBackToNoCompressionWhenClientDoesNotAcceptOurContentCodings() throws
            Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.withHeader("Accept-Encoding", "compress");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", nullValue());
        response.assertBody("uncompressed body");
    }

    @Test public void
    preservesContentLengthOfIdentityResponses() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentLength(128);
                response.body("uncompressed body...");
            }
        });

        request.withHeader("Accept-Encoding", "identity");
        compressor.handle(request, response);
        response.assertHeader("Content-Length", "128");
    }

    private String inflate(MockResponse response) throws IOException {
        return response.empty() ? "" : Streams.toString(new InflaterInputStream(response.stream(),
                new Inflater(true)));
    }

    private String unzip(MockResponse response) throws IOException {
        return response.empty() ? "" : Streams.toString(new GZIPInputStream(response.stream()));
    }
}
