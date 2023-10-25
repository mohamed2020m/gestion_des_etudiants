package me.ensa.projetws;

import static android.content.ContentValues.TAG;

import static me.ensa.projetws.AddEtudiant.PICK_IMAGE_REQUEST;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ensa.projetws.adapter.EtudiantAdapter;
import me.ensa.projetws.beans.Etudiant;
import me.ensa.projetws.utlis.EtudiantDeserializer;

public class MainActivity extends AppCompatActivity {
//    public String host = "http://" + GetIPAddress.getIP();
    public String host = "http://192.168.0.131";
    private RecyclerView recyclerView;
    private EtudiantAdapter etudiantAdapter = null;
    private LottieAnimationView error_icon;
    private FloatingActionButton fab;
    private LottieAnimationView load_data;
    private TextView text_error, empty;
    private Button try_again;
    private RelativeLayout main;
    private TextInputEditText searchEditText;
    RequestQueue requestQueue;
    List<Etudiant> list_etudiants = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        UpdateNetworkSecurityConfig.updateNetworkSecurityConfig(this);


        main = findViewById(R.id.main);
        text_error = findViewById(R.id.error);
        empty = findViewById(R.id.empty);
        try_again = findViewById(R.id.try_again);
        error_icon = findViewById(R.id.error_icon);
        load_data = findViewById(R.id.load_data);
        recyclerView = findViewById(R.id.recycle_view);

        searchEditText = findViewById(R.id.searchEdit);

        // Seach
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (etudiantAdapter != null){
                    etudiantAdapter.getFilter().filter(searchEditText.getText().toString());
                }
            }
        });

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchEditText.clearFocus();
                }
            }
        });

        View rootView = findViewById(R.id.main);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (searchEditText.hasFocus()) {
                        searchEditText.clearFocus(); // Remove focus if EditText is focused
                    }
                }
                return false;
            }
        });

        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isKeyboardVisible(rootView)) {
                    searchEditText.clearFocus();
                }
            }
        });

        // app title
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

    // Check if the keyboard is visible
    private boolean isKeyboardVisible(View rootView) {
        int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
        return heightDiff > rootView.getHeight() / 4;
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        refreshContent();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final Etudiant item = etudiantAdapter.getData().get(position);
                Log.d("item", item.getId() + ", " + item.getNom());
                etudiantAdapter.removeItem(position);

                Snackbar snackbar = Snackbar
                        .make(main, "Student was removed.", Snackbar.LENGTH_LONG);
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
        String insertUrl = host + "/backend_volley/ws/loadEtudiant.php";

        load_data.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.GET, insertUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("tag", response);
                    load_data.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    text_error.setVisibility(View.GONE);
                    try_again.setVisibility(View.GONE);
                    error_icon.setVisibility(View.GONE);

                    if(response != null){

                        Log.d("res: ", response);
                        Type type = new TypeToken<List<Etudiant>>(){}.getType();
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Etudiant.class, new EtudiantDeserializer())
                                .create();
                        List<Etudiant> etudiants = gson.fromJson(response, type);
                        if(etudiants.isEmpty()){
                            empty.setVisibility(View.VISIBLE);
                        }else{
                            empty.setVisibility(View.GONE);
//                        List<Etudiant> etudiants = new Gson().fromJson(response, type);
                            if (etudiants != null) {
                                Log.d("etudiant: ", etudiants.toString());
                                for(Etudiant e : etudiants){
                                    Log.d("e:", e.getNom());
                                }

                                list_etudiants = new ArrayList<>(etudiants);

                                for(Etudiant e : list_etudiants){
                                    Log.d(TAG, e.toString());
                                }
                            } else {
                                Log.d("msg: ", "null");
                            }
                        }
                    }else{
                        list_etudiants = new ArrayList<>();
                    }

                    etudiantAdapter = new EtudiantAdapter(MainActivity.this, list_etudiants, MainActivity.this);
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
                error_icon.setVisibility(View.VISIBLE);
                try_again.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        text_error.setVisibility(View.GONE);
//                        try_again.setVisibility(View.GONE);
//                        error_icon.setVisibility(View.GONE);
                        reset();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        String imageBase64;
        if ( requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            updateImage(selectedImageUri);

//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
//
//                // Convert the selected image to a base64 string
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//                byte[] byteArray = byteArrayOutputStream.toByteArray();
//                imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
//
//                SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("image", imageBase64);
//                editor.apply();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private void deleteEtudiant(int id) {
        String insertUrl = host +"/backend_volley/ws/deleteEtudiant.php";

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("id", String.valueOf(id));
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

    @Override
    protected void onResume() {
        super.onResume();
        reset();
    }

    private void reset(){
        text_error.setVisibility(View.GONE);
        try_again.setVisibility(View.GONE);
        error_icon.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void updateImage(Uri selectedImageUri) {
        new ImageEncoderTask().execute(selectedImageUri);
    }

    private class ImageEncoderTask extends AsyncTask<Uri, Void, String> {
        @Override
        protected String doInBackground(Uri... uris) {
            Uri selectedImageUri = uris[0];
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), selectedImageUri);

                // Convert the bitmap to base64 here (within the doInBackground method)
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                return Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String imageBase64) {
            if (imageBase64 != null) {
                if (etudiantAdapter != null) {
                    etudiantAdapter.updateImage(imageBase64);
                }
            } else {
                Log.d("error", "error");
            }
        }
    }

}