package vn.icar.rim.device.entitiy;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "button")
public class ButtonInfo {

    @DatabaseField(generatedId = true) private long id;
    @DatabaseField(canBeNull = false) private int value;
    @DatabaseField(canBeNull = false) private int error;
    @ForeignCollectionField(eager = true, orderColumnName = "event") private ForeignCollection<ActionInfo> actions;

    public ButtonInfo() {

    }

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public int getValue() {

        return value;
    }

    public void setValue(int value) {

        this.value = value;
    }

    public int getError() {

        return error;
    }

    public void setError(int error) {

        this.error = error;
    }

    public ForeignCollection<ActionInfo> getActions() {

        return actions;
    }

}
