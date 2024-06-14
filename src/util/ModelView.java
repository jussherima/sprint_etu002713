package util;

import java.util.HashMap;

public class ModelView {
    HashMap<String, Object> data;
    String name;

    public ModelView() {
        data = new HashMap<>();
    }

    public ModelView(String name) {
        this();
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * @return the data
     */
    public HashMap<String, Object> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addObject(String object_name, Object object) {
        data.put(object_name, object);
    }
}
