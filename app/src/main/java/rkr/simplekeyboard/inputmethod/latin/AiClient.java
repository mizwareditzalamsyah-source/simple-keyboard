package rkr.simplekeyboard.inputmethod.latin;

import java.io.*;
import java.net.*;
import org.json.*;

public class AiClient {
    public interface Callback { void onResult(String text); }
    private static final String API_KEY = "ISI_API_KEY_KAMU";
    private static final String MODEL = "gemini-2.5-flash";

    public static void ask(final String prompt, final Callback cb) {
        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                        + MODEL + ":generateContent");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(30000);
                c.setRequestMethod("POST");
                c.setRequestProperty("x-goog-api-key", API_KEY);
                c.setRequestProperty("content-type", "application/json");
                c.setDoOutput(true);
                JSONObject part = new JSONObject().put("text", prompt);
                JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
                JSONObject thinking = new JSONObject().put("thinkingBudget", 0);
                JSONObject gen = new JSONObject()
                        .put("maxOutputTokens", 300)
                        .put("thinkingConfig", thinking);
                JSONObject body = new JSONObject()
                        .put("contents", new JSONArray().put(content))
                        .put("generationConfig", gen);
                c.getOutputStream().write(body.toString().getBytes("UTF-8"));
                int code = c.getResponseCode();
                InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                if (code >= 400) {
                    String why = "";
                    try {
                        why = new JSONObject(sb.toString())
                                .getJSONObject("error").getString("status");
                    } catch (Exception ignored) { }
                    cb.onResult("ERR:" + code + " " + why);
                    return;
                }
                String text = new JSONObject(sb.toString())
                        .getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).getString("text");
                cb.onResult(text);
            } catch (Exception e) {
                cb.onResult("ERR:" + e.getClass().getSimpleName());
            }
        }).start();
    }
}