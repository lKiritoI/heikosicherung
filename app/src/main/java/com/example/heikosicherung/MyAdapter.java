package com.example.heikosicherung;

import static com.example.heikosicherung.R.color.background_alex;
import static com.example.heikosicherung.R.color.background_amelie;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private final static String __MAIN_PATH = MainActivity.getMainPath();
    private final static String __NAME_USER_1 = MainActivity.getNameUser1();
    private final static String __NAME_USER_2 = MainActivity.getNameUser2();
    EditText popup_name, popup_betrag, popup_grund;
    TextView popup_date, popup_title;
    CardView popup_cardView;
    DatabaseReference databaseReference;
    LinearLayout popup_linlay;
    private final Context context;
    private final ArrayList<User> list;
    private final ArrayList<String> list_key;

    public MyAdapter(Context context, ArrayList<User> list, ArrayList<String> list_key) {
        this.context = context;
        this.list = list;
        this.list_key = list_key;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.userentry, parent, false);
        return new MyViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = list.get(position);
        Float c = 0.00f;

        if (user.getAmount() != null) {
            c = Float.valueOf((String) user.getAmount().replace(",", "."));
        }

        holder.name_title.setText("Name");
        holder.amount_title.setText("Betrag");
        holder.usage_title.setText("Grund");

        holder.name_id.setText("\t" + user.getName());
        holder.amount_id.setText("\t" + String.format("%.2f€", c).replace(".", ","));
        String usage = user.getUsage();
        holder.usage_id.setText("\t" + usage.substring(0, 1).toUpperCase() + usage.substring(1).toLowerCase());
        holder.date_id.setText("\t" + user.getDate());


        if (Objects.equals(user.getName(), __NAME_USER_1)) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, background_alex));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, background_amelie));
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopup(holder.getAdapterPosition(), user.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void openPopup(int position, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_layout, null);
        View customTitleView = inflater.inflate(R.layout.popup_title, null);

        User user = list.get(position);
        String key = list_key.get(position);

        popup_title = customTitleView.findViewById(R.id.popup_title);

        popup_title.setText("Eintrag ändern?");
        popup_title.setGravity(Gravity.CENTER);

        databaseReference = FirebaseDatabase.getInstance().getReference(__MAIN_PATH);

        popup_cardView = dialogView.findViewById(R.id.popup_cardView);

        popup_linlay = dialogView.findViewById(R.id.popup_lin_lay);

        popup_cardView.setCardBackgroundColor(Objects.equals(name, "Alex") ? ContextCompat.getColor(context, background_alex) : ContextCompat.getColor(context, background_amelie));

        popup_name = dialogView.findViewById(R.id.popup_name);
        popup_betrag = dialogView.findViewById(R.id.popup_amount);
        popup_grund = dialogView.findViewById(R.id.popup_usage);
        popup_date = dialogView.findViewById(R.id.popup_date);

        popup_name.setText(user.getName());
        popup_betrag.setText(user.getAmount());
        popup_grund.setText(user.getUsage());
        popup_date.setText(user.getDate());

        builder.setCustomTitle(customTitleView).setTitle("Eintrag ändern?").setPositiveButton("OK", null).setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());

        builder.setCancelable(false);

        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            dialogView.measure(widthMeasureSpec, heightMeasureSpec);
            int measuredHeight = dialogView.getMeasuredHeight();

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            Window window = alertDialog.getWindow();
            if (window != null) {
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.height = measuredHeight + 400;
                window.setAttributes(layoutParams);
            }
        });

        alertDialog.show();

        Button okButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button cancelButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        okButton.setTextColor(Color.BLACK);
        cancelButton.setTextColor(Color.BLACK);

        okButton.setOnClickListener(v -> {
            user.setName(popup_name.getText().toString());
            user.setAmount(popup_betrag.getText().toString());
            user.setUsage(popup_grund.getText().toString());

            databaseReference.child(key).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Eintrag geändert!", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(context, "Eintrag nicht geändert! (@Alex)", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            });
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Nichts geändert (Abgebrochen)!", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name_id, amount_id, usage_id, date_id, name_title, amount_title, usage_title;
        CardView cardView;
        Button edit;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name_id = itemView.findViewById(R.id.textname);
            amount_id = itemView.findViewById(R.id.textbetrag);
            usage_id = itemView.findViewById(R.id.textgrund);
            date_id = itemView.findViewById(R.id.textDate);

            cardView = itemView.findViewById(R.id.cardView);
            edit = itemView.findViewById(R.id.btn_edit);

            name_title = itemView.findViewById(R.id.name_title);
            amount_title = itemView.findViewById(R.id.betrag_title);
            usage_title = itemView.findViewById(R.id.grund_title);
        }
    }
}
