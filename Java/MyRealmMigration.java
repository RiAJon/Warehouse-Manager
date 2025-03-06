package com.cs360.warehousemanager;

import io.realm.DynamicRealm;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class MyRealmMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 5) {
            if (schema.contains("User")) {
                schema.get("User")
                        .addField("username", String.class)
                        .addField("password", String.class); // Ensure password field is added
            }
            if (schema.contains("Item")) {
                schema.get("Item")
                        .addField("name", String.class)
                        .addField("sku", String.class)
                        .addField("count", Integer.class)
                        .addField("image", byte.class);
            }
            if (schema.contains("Warehouse")) {
                schema.get("Warehouse")
                        .addField("name", String.class)
                        .addField("address", String.class)
                        .addField("items", RealmList.class);
            }
            oldVersion ++;
        }
    }
}
