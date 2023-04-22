package commands;

import Assistant.Main;

public class FileCommands {

    public static boolean downloadFile(String url, String fileName) {
        // download the url to workspace
        String dir = Main.getWorkingDirectory();
        String command = "curl -o " + dir + fileName + " " + url;

        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
