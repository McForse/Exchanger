package com.shotball.project.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class AcceptedExchangesFragment extends ExchangesFragment {

    public AcceptedExchangesFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return mDatabase.child("exchanges").child("accepted").orderByChild("who").equalTo(getUid());
    }

}
