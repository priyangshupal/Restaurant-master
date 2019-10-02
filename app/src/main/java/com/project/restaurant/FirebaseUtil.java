package com.project.restaurant;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    private static FirebaseDatabase mData;

    public static FirebaseDatabase getDatabase() {
        if (mData == null) {

            mData = FirebaseDatabase.getInstance();
            mData.setPersistenceEnabled(true);
        }
        return mData;
    }
}
