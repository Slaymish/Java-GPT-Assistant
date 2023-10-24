package Functions;

public class Search implements Command{
    private String query;
    private String[] results;

    public Search(String query) {
        this.query = query;
    }

    public void execute(String query) {
        // do something
        results = new String[]{"result1", "result2"};
    }

    public String getResult() {
        return String.join("\n", results);
    }

}
