package com.cs360.warehousemanager;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Database extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Realm
        Realm.init(this);

        // Set up Realm configuration
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm") // Set database name
                .allowWritesOnUiThread(true) // FIXME: (future update) - this should be delegated to background thread for smoother performance
                .allowQueriesOnUiThread(true)
                .schemaVersion(6)
                .migration(new MyRealmMigration())
                .deleteRealmIfMigrationNeeded() // !! TAKE OUT IN PRODUCTION
                .build();

        Realm.setDefaultConfiguration(config);
    }
}