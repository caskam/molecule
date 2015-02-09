package com.vtence.molecule.test;

import org.hamcrest.Matcher;
import org.junit.Assert;

import java.net.HttpCookie;
import java.util.List;

import static com.vtence.molecule.support.CharsetDetector.detectCharsetOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public class HttpResponseAssert {
    private final HttpResponse response;

    public HttpResponseAssert(HttpResponse response) {
        this.response = response;
    }

    public static HttpResponseAssert assertThat(HttpResponse response) {
        return new HttpResponseAssert(response);
    }

    public HttpResponseAssert isOK() {
        return hasStatusCode(200);
    }

    public HttpResponseAssert hasStatusCode(int code) {
        return hasStatusCode(is(code));
    }

    private HttpResponseAssert hasStatusCode(Matcher<? super Integer> matching) {
        Assert.assertThat("response status code", response.statusCode(), matching);
        return this;
    }

    public HttpResponseAssert hasStatusMessage(String message) {
        return hasStatusMessage(is(message));
    }

    public HttpResponseAssert hasStatusMessage(Matcher<? super String> matching) {
        Assert.assertThat("response status message", response.statusMessage(), matching);
        return this;
    }

    public HttpResponseAssert hasHeader(String name, String value) {
        return hasHeader(name, equalTo(value));
    }

    public HttpResponseAssert hasHeader(String name, Matcher<? super String> matching) {
        Assert.assertThat("response '" + name + "' header", response.header(name), matching);
        return this;
    }

    public HttpResponseAssert hasHeaders(String name, Matcher<? super List<String>> matching) {
        Assert.assertThat("response '" + name + "' headers", response.headers(name), matching);
        return this;
    }

    public HttpResponseAssert isChunked() {
        return hasHeader("Transfer-Encoding", "chunked");
    }

    public HttpResponseAssert isNotChunked() {
        return hasHeader("Transfer-Encoding", not("chunked"));
    }

    public HttpResponseAssert hasBodyText(String text) {
        return hasBodyText(equalTo(text));
    }

    public HttpResponseAssert hasBodyText(Matcher<? super String> matching) {
        Assert.assertThat("response body text", response.bodyText(), matching);
        return this;
    }

    public HttpResponseAssert hasContentEncodedAs(String charset) {
        return hasContentEncodedAs(is(charset));
    }

    public HttpResponseAssert hasContentEncodedAs(Matcher<? super String> matching) {
        Assert.assertThat("response content encoding", detectCharsetOf(response.body()), matching);
        return this;
    }

    public HttpCookieAssert hasCookie(String named) {
        HttpCookie cookie = response.cookie(named);
        Assert.assertTrue("No cookie named '" + named + "'", cookie != null);
        return HttpCookieAssert.assertThat(cookie);
    }

    public HttpResponseAssert hasContentType(String contentType) {
        return hasContentType(equalTo(contentType));
    }

    private HttpResponseAssert hasContentType(Matcher<? super String> matching) {
        return hasHeader("Content-Type", matching);
    }
}