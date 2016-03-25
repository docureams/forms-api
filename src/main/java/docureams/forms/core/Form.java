package docureams.forms.core;

import java.io.Serializable;

public class Form implements Serializable {
    private long id;
    
    private String name;
    
    private String jsonData;

    public Form() {
    }

    public Form(String name, String jsonData) {
        this.name = name;
        this.jsonData = jsonData;
    }
    
    public long getId() {
        return id;
    }

    public Form setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Form setName(String name) {
        this.name = name;
        return this;
    }

    public String getJsonData() {
        return jsonData;
    }

    public Form setJsonData(String jsonData) {
        this.jsonData = jsonData;
        return this;
    }

    public String toJson() {
        return String.format("{\"id\":\"%s\", \"name\":\"%s\", \"data\":%s}", id, name, jsonData);
    }
}
