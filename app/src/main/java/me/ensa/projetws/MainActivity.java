package me.ensa.projetws;

import static android.content.ContentValues.TAG;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ensa.projetws.adapter.EtudiantAdapter;
import me.ensa.projetws.beans.Etudiant;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EtudiantAdapter etudiantAdapter = null;
    private FloatingActionButton fab;
    private ProgressBar load_data;
    private TextView text_error;
    private Button try_again;
    private RelativeLayout main;
    RequestQueue requestQueue;
    List<Etudiant> list_etudiants = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = findViewById(R.id.main);
        text_error = findViewById(R.id.error);
        try_again = findViewById(R.id.try_again);
        load_data = findViewById(R.id.load_data);
        recyclerView = findViewById(R.id.recycle_view);

        // app title
        // Set the custom title layout
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_title);

        // Optionally, hide the default title
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // Register a BroadcastReceiver to listen for the refresh signal
        IntentFilter intentFilter = new IntentFilter("refresh_main_activity");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshContent();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        refreshContent();

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEtudiant.class);
                startActivity(intent);
            }
        });

        // exit the app
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // enable swipe to delete
        enableSwipeToDeleteAndUndo();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final Etudiant item = etudiantAdapter.getData().get(position);

                etudiantAdapter.removeItem(position);

                Snackbar snackbar = Snackbar
                        .make(main, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        etudiantAdapter.restoreItem(item, position);
                        recyclerView.scrollToPosition(position);
                    }
                });

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                            deleteEtudiant(item.getId());
                        }
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    private void refreshContent() {
        String insertUrl = "http://192.168.0.131/backend_volley/ws/loadEtudiant.php";

        load_data.setVisibility(View.VISIBLE);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);
                    load_data.setVisibility(View.GONE);
                    text_error.setVisibility(View.GONE);
                    try_again.setVisibility(View.GONE);

                    Type type = new TypeToken<Collection<Etudiant>>(){}.getType();
                    Collection<Etudiant> etudiants = new Gson().fromJson(response, type);

                    list_etudiants = new ArrayList<>(etudiants);

                    for(Etudiant e : list_etudiants){
                        Log.d(TAG, e.toString());
                    }

                    etudiantAdapter = new EtudiantAdapter(MainActivity.this, list_etudiants);
                    recyclerView.setAdapter(etudiantAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
                load_data.setVisibility(View.GONE);
                text_error.setVisibility(View.VISIBLE);
                try_again.setVisibility(View.VISIBLE);
                try_again.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        text_error.setVisibility(View.GONE);
                        try_again.setVisibility(View.GONE);
                        refreshContent();
                    }
                });
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return null;
            }
        };

        requestQueue.add(request);
    }

    private void deleteEtudiant(int id) {
        String insertUrl = "http://192.168.0.131/backend_volley/ws/deleteEtudiant.php";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);
                    refreshContent();
                    Toast.makeText(MainActivity.this, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
                Toast.makeText(MainActivity.this, "Error in delete!", Toast.LENGTH_SHORT).show();

            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };

        requestQueue.add(request);
    }

}