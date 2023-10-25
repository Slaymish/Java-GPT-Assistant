package Functions;

public class Search implements Command{
    private String[] results;

    public Search() {
        this.results = new String[]{};
    }

    public void execute(String query) {
        // do something
        results = new String[]{"result for " + query + " 1", "result for " + query + " 2", "result for " + query + " 3"};
    }

    public String getResult() {
        return String.join("\n", results);
    }

}
