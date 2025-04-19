package usc.edu.ph.taskybear;

public class Task {
    private String title;
    private String details;
    private String date;
    private String resource;
    private String category;

    public Task(String title, String details, String date, String resource, String category) {
        this.title = title;
        this.details = details;
        this.date = date;
        this.resource = resource;
        this.category = category;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    public String getDate() {
        return date;
    }

    public String getResource() {
        return resource;
    }

    public String getCategory() {
        return category;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}