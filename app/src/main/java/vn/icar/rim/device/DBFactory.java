package vn.icar.rim.device;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import vn.icar.rim.device.entitiy.ActionInfo;
import vn.icar.rim.device.entitiy.ButtonInfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

@EBean
public class DBFactory {

    private static final String TAG = DBFactory.class.getSimpleName();

    private static final String DATABASE_NAME = "vn.icar.rbm.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<ButtonInfo, Long> buttonDao = null;
    private Dao<ActionInfo, Long> actionDao = null;

    @RootContext Context context;

    private DBHelper helper;

    @AfterInject
    void onInit() {

        helper = new DBHelper(context);
    }

    public Dao<ButtonInfo, Long> getButtonDao() throws SQLException {

        if (buttonDao == null) {
            buttonDao = helper.getDao(ButtonInfo.class);
        }
        return buttonDao;
    }

    public Dao<ActionInfo, Long> getActionDao() throws SQLException {

        if (actionDao == null) {
            actionDao = helper.getDao(ActionInfo.class);
        }
        return actionDao;
    }

    public void close() {

        helper.close();
    }

    private class DBHelper extends OrmLiteSqliteOpenHelper {

        public DBHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {

            try {
                TableUtils.createTable(connectionSource, ButtonInfo.class);
                TableUtils.createTable(connectionSource, ActionInfo.class);
            } catch (SQLException e) {
                Log.e(TAG, "Can't create database", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {

            try {
                TableUtils.dropTable(connectionSource, ButtonInfo.class, true);
                TableUtils.dropTable(connectionSource, ActionInfo.class, true);

                onCreate(db, connectionSource);
            } catch (SQLException e) {
                Log.e(TAG, "Can't drop databases", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {

            super.onOpen(db);

            if (!db.isReadOnly()) {
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }

        @Override
        public void close() {

            buttonDao = null;
            actionDao = null;

            super.close();
        }

    }

}
