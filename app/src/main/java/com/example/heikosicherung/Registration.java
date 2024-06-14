package com.example.heikosicherung;


import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Registration extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextDisplay;
    Button btnReg;
    TextView textView, slogan;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.registration_pb_progressBar);
        textView = findViewById(R.id.registration_tv_goto_login);
        editTextEmail = findViewById(R.id.registration_et_email);
        editTextPassword = findViewById(R.id.registration_et_passwort);
        editTextDisplay = findViewById(R.id.registration_et_display);
        btnReg = findViewById(R.id.registration_btn_register);
        slogan = findViewById(R.id.registration_tv_slogan);
        user = mAuth.getCurrentUser();

        slogan.setText(Html.fromHtml("Ob WG, Urlaub, Festivaltrip oder was auch immer BILLY macht das gemeinsame Haushalten ganz einfach. Sein Kraftfutter: eure Ausgaben. <b>Wer zahlt, trägt ein</b>.<br><br><b>Billy macht \"Muh\"</b>, so dass ihr keine Mühe habt eure Ausgaben zu sortieren, kontrollieren und auszugleichen.<br><br>BILLY verdaut alles sofort und schon hast du alles sofort - mit einem Klick auf einem Blick. <b>Das Ergebnis erste Sahne</b>.<br><br><b>Melde dich jetzt kostenlos an</b> und mache dir nie wieder Sorgen darüber, ob dir noch wer oder du noch wem was schuldest."));

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        btnReg.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password, disName;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            disName = String.valueOf(editTextDisplay.getText());

            if (TextUtils.isEmpty((email))) {
                Toast.makeText(Registration.this, "Enter E-Mail", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty((password))) {
                Toast.makeText(Registration.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    user = FirebaseAuth.getInstance().getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(disName).build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Registration.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(Registration.this, "Invalid entries.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}