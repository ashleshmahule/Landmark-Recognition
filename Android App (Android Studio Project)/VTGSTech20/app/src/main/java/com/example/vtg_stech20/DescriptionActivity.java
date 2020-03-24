package com.example.vtg_stech20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptionActivity extends AppCompatActivity {

    Map<String, Object> result = new HashMap<>();
    TextView nametv, loctv, statetv, desctv;
    EditText userReview;
    List<String> re;
    ListView reviews;
    String place;
    Button add, openMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);


        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String res = bundle.getString("result");


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert res != null;
        DocumentReference dr = db.collection("monument_description").document(res);
        dr.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    result = doc.getData();
                    Log.println(7, "result", "" + result);

                    String name = res;

                    place = result.get("Place").toString();
                    String state = result.get("State").toString();
                    String desc = result.get("Description").toString();

                    nametv = findViewById(R.id.name);
                    nametv.setText(name);
                    Log.println(7, "state", "" + state);

                    statetv = findViewById(R.id.state);
                    statetv.setText(state);

                    loctv = findViewById(R.id.location);
                    loctv.setText(place);

                    desctv = findViewById(R.id.description);
                    desctv.setText(desc);

                    re = (List<String>) result.get("Reviews");

                    reviews = findViewById(R.id.reviews);
                    assert re != null;
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(DescriptionActivity.this, R.layout.custom_list, re);
                    reviews.setAdapter(adapter);

                } else {
                    Log.d("", "No such doc");
                }
            } else {
                Log.d("", "get failed ", task.getException());
            }
        });

        add = findViewById(R.id.add);
        add.setOnClickListener(v -> {
            userReview = findViewById(R.id.userReview);
            String ur = userReview.getText().toString();
            re.add(ur);
            Map<String, Object> map = new HashMap<>();
            map.put("Reviews", re);

            dr.set(map, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.println(7, "add review", "DocumentSnapshot successfully written!")).addOnFailureListener(e -> Log.println(7, "add review", "Error writing document " + e));
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        });

        openMap = findViewById(R.id.openMap);

        openMap.setOnClickListener(v -> {
            Intent i = new Intent(DescriptionActivity.this, MapsActivity.class);
            Bundle b = new Bundle();
            b.putString("location", place);
            b.putString("name", res);
            i.putExtras(b);
            startActivity(i);
        });
    }

}
