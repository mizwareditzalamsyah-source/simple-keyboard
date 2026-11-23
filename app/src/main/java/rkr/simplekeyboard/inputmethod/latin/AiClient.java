package rkr.simplekeyboard.inputmethod.latin;

import java.io.*;
import java.net.*;
import org.json.*;

public class AiClient {
    public interface Callback { void onResult(String text); }
    private static final String API_KEY = "ISI_API_KEY_KAMU";

    public static void ask(final String prompt, final Callback cb) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.anthropic.com/v1/messages");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("x-api-key", API_KEY);
                c.setRequestProperty("anthropic-version", "2023-06-01");
                c.setRequestProperty("content-type", "application/json");
                c.setDoOutput(true);
                JSONObject m = new JSONObject();
                m.put("role", "user");
                m.put("content", prompt);
                JSONObject body = new JSONObject();
                body.put("model", "claude-haiku-5-5");
                body.put("max_tokens", 300);
                body.put("messages", new JSONArray().put(m));
                c.getOutputStream().write(body.toString().getBytes("UTF-8"));
                BufferedReader r = new BufferedReader(
                        new InputStreamReader(c.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                String text = new JSONObject(sb.toString())
                        .getJSONArray("content").getJSONObject(0).getString("text");
                cb.onResult(text);
            } catch (Exception e) {
                cb.onResult(null);
            }
        }).start();
    }
}