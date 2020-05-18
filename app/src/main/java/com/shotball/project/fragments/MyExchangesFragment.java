package com.shotball.project.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyExchangesFragment extends ExchangesFragment {

    public MyExchangesFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return mDatabase.child("exchanges").child("accepted").orderByChild("whom").equalTo(getUid());
    }

}
