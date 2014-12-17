package com.vtence.molecule;

import com.vtence.molecule.http.ContentLanguage;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.HeaderNames;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.http.ContentType;
import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.lib.BinaryBody;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LANGUAGE;
import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.lib.TextBody.text;
import static java.lang.Long.parseLong;

public class Response {
    private final Headers headers = new Headers();
    private final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();

    private int statusCode = HttpStatus.OK.code;
    private String statusText = HttpStatus.OK.text;
    private Body body = BinaryBody.empty();

    public Response() {}

    public Response status(HttpStatus status) {
        statusCode(status.code);
        statusText(status.text);
        return this;
    }

    public Response statusCode(int code) {
        statusCode = code;
        return this;
    }

    public int statusCode() {
        return statusCode;
    }

    public Response statusText(String text) {
        statusText = text;
        return this;
    }

    public String statusText() {
        return statusText;
    }

    public Response redirectTo(String location) {
        status(HttpStatus.SEE_OTHER);
        set(HeaderNames.LOCATION, location);
        return this;
    }

    public boolean has(String name) {
        return headers.has(name);
    }

    public Set<String> names() {
        return headers.names();
    }

    public String get(String name) {
        return headers.get(name);
    }

    public long getLong(String name) {
        String value = get(name);
        return value != null ? parseLong(value) : -1;
    }

    public Response add(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public Response set(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Response set(String name, Date date) {
        return set(name, httpDate(date));
    }

    public Response set(String name, Object value) {
        return set(name, String.valueOf(value));
    }

    public Response remove(String name) {
        headers.remove(name);
        return this;
    }

    public String contentType() {
        return get(CONTENT_TYPE);
    }

    public Response contentType(String contentType) {
        set(CONTENT_TYPE, contentType);
        return this;
    }

    public long contentLength() {
        return getLong(CONTENT_LENGTH);
    }

    public Response contentLength(long length) {
        set(CONTENT_LENGTH, length);
        return this;
    }

    public Response cookie(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
        return this;
    }

    public Response cookie(String name, String value) {
        return cookie(new Cookie(name, value));
    }

    public boolean hasCookie(String name) {
        return cookies.containsKey(name);
    }

    public Response removeCookie(String name) {
        cookies.remove(name);
        return this;
    }

    public Response discardCookie(String name) {
        cookie(name).maxAge(0);
        return this;
    }

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    public List<Cookie> cookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    public Response charset(String charsetName) {
        ContentType contentType = ContentType.of(this);
        if (contentType == null) return this;
        contentType(new ContentType(contentType.type(), contentType.subType(), charsetName).toString());
        return this;
    }

    public Charset charset() {
        ContentType contentType = ContentType.of(this);
        if (contentType == null || contentType.charset() == null) {
            return Charsets.ISO_8859_1;
        }
        return contentType.charset();
    }

    public Response addLocale(Locale locale) {
        set(CONTENT_LANGUAGE, ContentLanguage.of(this).add(locale));
        return this;
    }

    public Response locale(Locale locale) {
        set(CONTENT_LANGUAGE, new ContentLanguage().add(locale));
        return this;
    }

    public Locale locale() {
        List<Locale> locales = locales();
        return locales.isEmpty() ? null : locales.get(0);
    }

    public List<Locale> locales() {
        return ContentLanguage.of(this).locales();
    }

    public Response removeLocale(Locale locale) {
        set(CONTENT_LANGUAGE, ContentLanguage.of(this).remove(locale));
        return this;
    }

    public Response body(String text) {
        return body(text(text));
    }

    public Response body(Body body) {
        this.body = body;
        return this;
    }

    public Body body() {
        return body;
    }

    public long size() {
        return body.size(charset());
    }

    public boolean empty() {
        return size() == 0;
    }
}