package com.thatcakeid.zrytezene;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Just a simple singleton class used to store the current user information without
 * waiting getting the data from the database
 */
public class CurrentUserProfile {
    private static CurrentUserProfile instance;

    private CurrentUserProfile() {}

    public String uid;
    public String username;
    public String bio;
    public String img_url;
    public Timestamp time_creation;
    public String email;

    public static CurrentUserProfile getInstance() {
        if (instance == null) {
            updateData();
        }

        return instance;
    }

    public static void updateData() {
        if (instance == null) {
            instance = new CurrentUserProfile();
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        instance.uid = uid;

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    instance.username = snapshot.getString("username");
                    instance.bio = snapshot.getString("description");
                    instance.img_url = snapshot.getString("img_url");
                    instance.time_creation = snapshot.getTimestamp("time_creation");
                    instance.email = snapshot.getString("mail");
                });
    }
}
