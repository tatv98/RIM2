package vn.icar.rim.device.entitiy;

import vn.icar.rim.device.actions.executors.ActionExecutor.ActionType;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "action")
public class ActionInfo {

    public enum EventType {

        CLICK, HOLD

    }

    @DatabaseField(generatedId = true) private long id;
    @DatabaseField(foreign = true, columnDefinition = "button_id bigint references button(id) on delete cascade") private ButtonInfo button;
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_INTEGER) private EventType event;
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_INTEGER) private ActionType actionType;
    @DatabaseField private String action;

    public ActionInfo() {

        this.actionType = ActionType.NONE;
    }

    public ActionInfo(EventType event) {

        this.event = event;
        this.actionType = ActionType.NONE;
    }

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public ButtonInfo getButton() {

        return button;
    }

    public void setButton(ButtonInfo button) {

        this.button = button;
    }

    public EventType getEvent() {

        return event;
    }

    public ActionType getActionType() {

        return actionType;
    }

    public void setActionType(ActionType actionType) {

        this.actionType = actionType;
    }

    public String getAction() {

        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

}
