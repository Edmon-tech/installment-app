package com.shop.installment;
import java.io.*; import java.net.*; import org.json.JSONObject;
final class Api {
    static JSONObject post(String action, JSONObject body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(Cfg.API + "?a=" + action).openConnection();
        c.setConnectTimeout(15000); c.setReadTimeout(15000); c.setRequestMethod("POST"); c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        try (OutputStream o = c.getOutputStream()) { o.write(body.toString().getBytes("UTF-8")); }
        int code = c.getResponseCode();
        InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream();
        ByteArrayOutputStream b = new ByteArrayOutputStream(); byte[] buf = new byte[4096]; int n;
        while (in != null && (n = in.read(buf)) > 0) b.write(buf, 0, n);
        if (code >= 400) throw new Exception("HTTP " + code);
        return new JSONObject(b.toString("UTF-8"));
    }
}
