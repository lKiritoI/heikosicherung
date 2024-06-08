package com.example.heikosicherung;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity2 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String __MAIN_PATH = "users";
    private final String __NAME_USER_1 = "Heiko";
    private final String __NAME_USER_2 = "Anke";
    private final String __NOTI_DB_SUCCESSFUL = "Daten gespeichert";
    private final String __NOTI_DB_EMPTY_AMOUNT = "Betrag leer";
    private final String __NOTI_DB_EMPTY_USAGE = "Grund leer";
    private final List<Float> floatList_user_1 = new ArrayList<>();
    private final List<Float> floatList_user_2 = new ArrayList<>();
    ArrayList<User> list = new ArrayList<>();
    ArrayList<String> list_key = new ArrayList<>();
    MyAdapter adapter;
    String empty = "";
    DatabaseReference databaseReference;
    float amount_user_1 = 0.00f;
    float amount_user_2 = 0.00f;
    RadioButton rb_user_1, rb_user_2;
    EditText amount, usage;
    TextView result, result_user_1, result_user_2, header, misc_cat;
    Button insert, view, logout;
    Spinner dropdown;
    String[] items = new String[]{"Miete / Hotel", "Essen, Haushalt etc.", "Restaurant / Mekkes / Döner", "Kneipe / Club", "Lieferdienst", "Auto / Taxi / Uber / Carsharing", "Tanken / Parken", "Streaming", "Kino / Konzert / Party / Festival", "Sonstiges:"};
    FirebaseAuth auth;
    FirebaseUser user;
    private float __GLOBAL_AMOUNT_USER_1 = 0.00f;
    private float __GLOBAL_AMOUNT_USER_2 = 0.00f;

    protected static String getMainPath() {
        return __MAIN_PATH;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        System.out.println("Start");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadData();

        dropdown = findViewById(R.id.spinner);
        dropdown.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        rb_user_1 = findViewById(R.id.rb_user_1);
        rb_user_2 = findViewById(R.id.rb_user_2);

        rb_user_1.setText(__NAME_USER_1);
        rb_user_2.setText(__NAME_USER_2);

        rb_user_1.setChecked(true);
        rb_user_2.setChecked(false);

        amount = findViewById(R.id.et_amount);

        auth = FirebaseAuth.getInstance();

        header = findViewById(R.id.header);

        misc_cat = findViewById(R.id.cat_misc);
        misc_cat.setVisibility(View.GONE);

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            header.setText(user.getDisplayName());
        }


    }

    private void resetFields() {
        amount = findViewById(R.id.et_amount);
        usage = findViewById(R.id.et_usage);

        amount.setText(empty);
        usage.setText(empty);

        amount.setHint("Wie viel €?");
        usage.setHint("Wofür?");
    }

    private void loadData() {
        databaseReference = FirebaseDatabase.getInstance().getReference(__MAIN_PATH);
        adapter = new MyAdapter(this, list, list_key);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    list.add(user);
                }
                getBetraege();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error);
            }
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void getBetraege() {
        __GLOBAL_AMOUNT_USER_1 = 0.00f;
        __GLOBAL_AMOUNT_USER_2 = 0.00f;

        result = findViewById(R.id.result);
        result_user_1 = findViewById(R.id.tv_amount_user_1);
        result_user_2 = findViewById(R.id.tv_amount_user_2);

        readData(floating -> {
            amount_user_1 = __GLOBAL_AMOUNT_USER_1;
            amount_user_2 = __GLOBAL_AMOUNT_USER_2;

            String res_user_1 = String.format(__NAME_USER_1 + "s Ausgaben:\n%.2f€", amount_user_1).replace(".", ",");
            String res_user_2 = String.format(__NAME_USER_2 + "s Ausgaben:\n%.2f€", amount_user_2).replace(".", ",");

            result_user_1.setText(res_user_1);
            result_user_2.setText(res_user_2);

            String dept_user_1 = String.format(__NAME_USER_1 + " schuldet " + __NAME_USER_2 + " %.2f€", (Math.abs(amount_user_1 - amount_user_2) / 2)).replace(".", ",");
            String dept_user_2 = String.format(__NAME_USER_2 + " schuldet " + __NAME_USER_1 + " %.2f€", (Math.abs(amount_user_2 - amount_user_1) / 2)).replace(".", ",");

            if (amount_user_1 == amount_user_2) {
                result.setText("Gleichstand! Let's Go!");
            } else if (amount_user_1 < amount_user_2) {
                result.setText(dept_user_1 + ".");
            } else {
                result.setText(dept_user_2 + ".");
            }
        });
    }

    private void readData(FirebaseCallback firebaseCallback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                floatList_user_2.clear();
                floatList_user_1.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    User user = dataSnapshot.getValue(User.class);
                    list.add(user);

                    assert user != null;
                    if (Objects.equals(user.getName(), __NAME_USER_1)) {
                        Float sum_amount_user_1 = Float.valueOf((String) user.getAmount().replace(",", "."));
                        floatList_user_1.add(sum_amount_user_1);
                    }

                    if (Objects.equals(user.getName(), __NAME_USER_2)) {
                        Float sum_amount_user_2 = Float.valueOf((String) user.getAmount().replace(",", "."));
                        floatList_user_2.add(sum_amount_user_2);
                    }
                }
                for (int i = 0; i < floatList_user_1.size(); i++) {
                    __GLOBAL_AMOUNT_USER_1 += floatList_user_1.get(i);
                }

                for (int i = 0; i < floatList_user_2.size(); i++) {
                    __GLOBAL_AMOUNT_USER_2 += floatList_user_2.get(i);
                }
                firebaseCallback.onCallback(__GLOBAL_AMOUNT_USER_1);
                firebaseCallback.onCallback(__GLOBAL_AMOUNT_USER_2);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error);
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void insertData() {
        String nameX = rb_user_1.isChecked() ? __NAME_USER_1 : __NAME_USER_2;
        String amountX = amount.getText().toString().replace(".", ",");
        String usageX = usage.getText().toString();
        String id = databaseReference.push().getKey();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String dateX = df.format(new Date());

        User user = new User(nameX, amountX, usageX, dateX);

        if (amountX.isEmpty()) {
            Toast.makeText(MainActivity2.this, __NOTI_DB_EMPTY_AMOUNT, Toast.LENGTH_SHORT).show();
            return;
        } else if (usageX.isEmpty()) {
            Toast.makeText(MainActivity2.this, __NOTI_DB_EMPTY_USAGE, Toast.LENGTH_SHORT).show();
            return;
        }

        assert id != null;
        databaseReference.child(id).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity2.this, __NOTI_DB_SUCCESSFUL, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                header.setText(items[position]);
                misc_cat.setVisibility(View.GONE);
                misc_cat.setText("");
                break;
            case 9:
                misc_cat.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private interface FirebaseCallback {
        void onCallback(float floating);
    }
}