package webService.services.cscl.result.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Dorinela on 6/26/2017.
 */
public class Category {

    private String name;
    private String description;
    private List<Community> communities;

    public Category(String name, String description, List<Community> communities) {
        this.name = name;
        this.description = description;
        this.communities = communities;
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

    public List<Community> getCommunities() {
        return communities;
    }

    public void setCommunities(List<Community> communities) {
        this.communities = communities;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", communities=" + communities +
                '}';
    }

}
