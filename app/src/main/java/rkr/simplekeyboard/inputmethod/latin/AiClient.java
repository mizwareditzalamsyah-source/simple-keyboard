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
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("x-goog-api-key", API_KEY);
                c.setRequestProperty("content-type", "application/json");
                c.setDoOutput(true);
                JSONObject part = new JSONObject().put("text", prompt);
                JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
                JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));
                c.getOutputStream().write(body.toString().getBytes("UTF-8"));
                BufferedReader r = new BufferedReader(
                        new InputStreamReader(c.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                String text = new JSONObject(sb.toString())
                        .getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).getString("text");
                cb.onResult(text);
            } catch (Exception e) {
                cb.onResult(null);
            }
        }).start();
    }
}