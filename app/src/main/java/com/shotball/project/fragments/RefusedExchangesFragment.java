package com.shotball.project.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RefusedExchangesFragment extends ExchangesFragment {

    public RefusedExchangesFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return mDatabase.child("exchanges").child("refused").orderByChild("who").equalTo(getUid());
    }

}
