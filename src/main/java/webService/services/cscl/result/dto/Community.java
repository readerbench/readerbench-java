package webService.services.cscl.result.dto;

/**
 * Created by Dorinela on 6/26/2017.
 */
public class Community {

    private String name;
    private String description;

    public Community(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Community{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
