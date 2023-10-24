package me.ensa.projetws;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import me.ensa.projetws.utlis.GetIPAddress;


public class AddEtudiant extends AppCompatActivity{
//    public String host = "http://" + GetIPAddress.getIP();
    public String host = "http://192.168.0.161";
    private EditText nom;
    private EditText prenom;
    private AutoCompleteTextView ville;
    private RadioButton m;
    private RadioButton f;
    private Button add;
    private TextView error_msg;
    private ImageView image;
    private String imageBase64;

    public static final int PICK_IMAGE_REQUEST = 1;
    RequestQueue requestQueue;
    private String selectedVille;
    String insertUrl = host + "/backend_volley/ws/createEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);
        image = (ImageView) findViewById(R.id.upload_image);
        nom = (EditText) findViewById(R.id.nom);
        prenom = (EditText) findViewById(R.id.prenom);
        ville = (AutoCompleteTextView) findViewById(R.id.ville);
        add = (Button) findViewById(R.id.add);
        m = (RadioButton) findViewById(R.id.m);
        f = (RadioButton) findViewById(R.id.f);
        error_msg = findViewById(R.id.error_msg);

        // Enable the "Up" button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        ville.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedVille = ville.getText().toString();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nom != null && prenom != null && selectedVille != null && image != null && (m.isChecked() || f.isChecked())){
                    error_msg.setVisibility(View.GONE);
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                    add.setText("Creating Student...");
                    StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("message", response);
                                    clear();
                                    add.setText("Create");
                                    Toast.makeText(AddEtudiant.this, "Created Successfully", Toast.LENGTH_LONG).show();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error", error.toString());
                            Toast.makeText(AddEtudiant.this, "Error", Toast.LENGTH_LONG).show();

                        }
                    }
                    ){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            String sexe = "";
                            if(m.isChecked())
                                sexe = "homme";
                            else
                                sexe = "femme";
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("nom", nom.getText().toString());
                            params.put("prenom", prenom.getText().toString());
                            params.put("ville", selectedVille);
                            params.put("sexe", sexe);
                            if(imageBase64 != null){
                                params.put("photo", imageBase64);
                                return params;
                            }else{
                                error_msg.setVisibility(View.VISIBLE);
                                return null;
                            }
                        }
                    };

                    requestQueue.add(request);

//                    Intent intent = new Intent(AddEtudiant.this, MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//                    finish();
                }else{
                    error_msg.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                // Convert the selected image to a base64 string
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void clear(){
        nom.setText("");
        prenom.setText("");
        ville.setSelection(0);
        m.setChecked(false);
        f.setChecked(false);
        image.setImageResource(R.drawable.upload);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//    }
}