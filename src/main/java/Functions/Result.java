package Functions;

import java.net.URL;

class Result {
    private URL url;
    private String text;

    public Result(String url, String text) {
        try {
            this.url = new URL(url);
        } catch (Exception e) {
            this.url = null;
        }
        this.text = text;
    }

    public URL getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "URL: " + url + ", Text: " + text;
    }
}
