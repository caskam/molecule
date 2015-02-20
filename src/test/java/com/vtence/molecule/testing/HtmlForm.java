package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Joiner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlForm {

    private final Map<String, String> data = new HashMap<String, String>();
    private Charset charset = Charsets.UTF_8;

    public String contentType() {
        return "application/x-www-form-urlencoded";
    }

    public HtmlForm encoding(String charsetName) {
        return encoding(Charset.forName(charsetName));
    }

    public HtmlForm encoding(Charset charset) {
        this.charset = charset;
        return this;
    }

    public HtmlForm set(String name, String value) {
        data.put(name, value);
        return this;
    }

    public String encode() {
        List<String> pairs = new ArrayList<String>();
        URLEscaper escaper = URLEscaper.to(charset);
        for (String name : data.keySet()) {
            pairs.add(escaper.escape(name) + "=" + escaper.escape(data.get(name)));
        }
        return Joiner.on("&").join(pairs);
    }
}
