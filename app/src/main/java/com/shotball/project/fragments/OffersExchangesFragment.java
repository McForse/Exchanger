package com.shotball.project.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class OffersExchangesFragment extends ExchangesFragment {

    public OffersExchangesFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return mDatabase.child("exchanges").child("proposed").orderByChild("whom").equalTo(getUid());
    }

}
