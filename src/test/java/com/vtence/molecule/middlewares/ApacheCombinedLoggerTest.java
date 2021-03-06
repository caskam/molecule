package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.LoggingSupport.LogRecordingHandler;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

import static com.vtence.molecule.http.HeaderNames.REFERER;
import static com.vtence.molecule.http.HeaderNames.USER_AGENT;
import static com.vtence.molecule.http.HttpMethod.DELETE;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.http.HttpStatus.NO_CONTENT;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.support.LoggingSupport.anonymousLogger;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;


public class ApacheCombinedLoggerTest {
    LogRecordingHandler logRecords = new LogRecordingHandler();
    Instant currentTime = LocalDateTime.of(2012, 6, 27, 12, 4, 0).toInstant(ZoneOffset.of("-05:00"));
    ApacheCombinedLogger logger = new ApacheCombinedLogger(anonymousLogger(logRecords), Clock.fixed(currentTime, ZoneId.of("GMT+01:00")), Locale.US);

    Request request = new Request().protocol("HTTP/1.1").remoteIp("192.168.0.1");
    Response response = new Response();

    @Test
    public void
    logsRequestsServedInApacheCombinedLogFormat() throws Exception {
        request.method(GET)
               .uri("/products?keyword=dogs")
               .header(REFERER, "http://lama/wool")
               .header(USER_AGENT, "Mozilla/5.0 (compatible; MSIE 9.0; AOL 9.7)");

        logger.handle(request, response);
        response.status(OK).body("a response with a size of 28").done();

        response.await();
        logRecords.assertEntries(contains("192.168.0.1 - - [27/Jun/2012:18:04:00 +0100] \"GET /products?keyword=dogs HTTP/1.1\" 200 28 \"http://lama/wool\" \"Mozilla/5.0 (compatible; MSIE 9.0; AOL 9.7)\""));
    }

    @Test
    public void
    logsEmptyStringWhenNoUserAgentInRequest() throws Exception {
        request.method(GET)
               .uri("/products?keyword=dogs")
               .header(REFERER, "http://lama/wool");

        logger.handle(request, response);
        response.status(OK).body("a response with a size of 28").done();

        response.await();
        logRecords.assertEntries(contains("192.168.0.1 - - [27/Jun/2012:18:04:00 +0100] \"GET /products?keyword=dogs HTTP/1.1\" 200 28 \"http://lama/wool\" \"\""));
    }

    @Test
    public void
    logsEmptyStringWhenNoRefererInRequest() throws Exception {
        request.method(GET)
               .uri("/products?keyword=dogs")
               .header(USER_AGENT, "Mozilla/5.0 (compatible; MSIE 9.0; AOL 9.7)");

        logger.handle(request, response);
        response.status(OK).body("a response with a size of 28").done();

        response.await();
        logRecords.assertEntries(contains("192.168.0.1 - - [27/Jun/2012:18:04:00 +0100] \"GET /products?keyword=dogs HTTP/1.1\" 200 28 \"\" \"Mozilla/5.0 (compatible; MSIE 9.0; AOL 9.7)\""));
    }

    @Test
    public void
    usesOriginalRequestValues() throws Exception {
        request.remoteIp("192.168.0.1").method(DELETE).uri("/logout")
               .header(REFERER, "http://lama/wool")
               .header(USER_AGENT, "Mozilla/5.0...");

        logger.connectTo((request, response) -> {
            request.uri("/changed").method(POST).remoteIp("100.100.100.1").protocol("HTTPS")
                   .header(REFERER, "?").header(USER_AGENT, "?");
            response.status(NO_CONTENT).done();
        });
        logger.handle(request, response);

        logRecords.assertEntries(contains(containsString("\"DELETE /logout HTTP/1.1\" 204 - \"http://lama/wool\" \"Mozilla/5.0...\"")));
    }
}