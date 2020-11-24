package vn.icar.rim.device.entitiy;

import java.io.Serializable;

public class TaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectName;
    private String name;

    public TaskInfo(String projectName, String name) {

        this.projectName = projectName;
        this.name = name;
    }

    public String getProject() {

        return projectName;
    }

    public String getName() {

        return name;
    }

}
