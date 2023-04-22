package commands;

public class GoogleSearch {
    public static String search(String query) {
        String url = "https://www.google.com/search?q=" + query;
        System.out.println("Opening " + url);
        open(url);
        return url;
    }

    private static void open(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
