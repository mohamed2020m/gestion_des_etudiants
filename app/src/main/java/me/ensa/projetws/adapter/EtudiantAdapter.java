package me.ensa.projetws.adapter;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static me.ensa.projetws.AddEtudiant.PICK_IMAGE_REQUEST;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.ensa.projetws.AddEtudiant;
import me.ensa.projetws.MainActivity;
import me.ensa.projetws.R;
import me.ensa.projetws.SplachScreen;
import me.ensa.projetws.beans.Etudiant;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> { // implements Filterable {
    private static final String TAG = "EtudiantAdapter";
    public String host = "http://192.168.0.131";
    private String imageBase64;
    private Activity p;
    private List<Etudiant> etudiants;
    private Context context;

    public EtudiantAdapter(Context context, List<Etudiant> etudiants, Activity p) {
        this.etudiants = etudiants;
        this.context = context;
        this.p = p;
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.list_item, viewGroup, false);
        final EtudiantViewHolder holder = new EtudiantViewHolder(v);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popup = LayoutInflater.from(context).inflate(R.layout.edutiant_edit_item, null,false);
                final TextView edit_nom = popup.findViewById(R.id.edit_nom);
                final TextView edit_prenom = popup.findViewById(R.id.edit_prenom);
                final Spinner edit_ville = popup.findViewById(R.id.edit_ville);
                final RadioButton edit_m = popup.findViewById(R.id.edit_m);;
                final RadioButton edit_f = popup.findViewById(R.id.edit_f);;
                final TextView edit_id = popup.findViewById(R.id.edit_id);
                final CircleImageView edit_upload_image = popup.findViewById(R.id.edit_upload_image);

                edit_nom.setText(((TextView)v.findViewById(R.id.nom)).getText().toString());
                edit_prenom.setText(((TextView)v.findViewById(R.id.prenom)).getText().toString());

                Etudiant clickedItem = etudiants.get(holder.getAdapterPosition());
                String base64Image = clickedItem.getPhoto();
                if(base64Image == null){
                    edit_upload_image.setImageResource(R.drawable.student);
                }else{
                    byte[] decodedImage = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                    edit_upload_image.setImageBitmap(bitmap);
                }

                // Get the ArrayAdapter of the Spinner
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) edit_ville.getAdapter();
                String st_ville = ((TextView)v.findViewById(R.id.ville)).getText().toString();

                // Find the position of the item in the Spinner's data
                int position = adapter.getPosition(st_ville.substring(0, 1) + st_ville.substring(1).toLowerCase());
                edit_ville.setSelection(position);

                if( ((TextView)v.findViewById(R.id.sexe)).getText().equals("FEMME") ) {
                    edit_f.setChecked(true);
                }else{
                    edit_m.setChecked(true);
                }
                edit_id.setText(((TextView)v.findViewById(R.id.ids)).getText().toString());

                edit_upload_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        p.startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }
                });

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        imageBase64 = sharedPreferences.getString("image", null);
                        if (imageBase64 != null) {
                            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            edit_upload_image.setImageBitmap(decodedBitmap);

                        }else{
                            Log.d("error", "error");
                        }
                        editor.clear();
                        editor.apply();
                    }
                }, 5000);


                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Update Info")
                        .setMessage("Update Your Info:")
                        .setView(popup)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestQueue requestQueue;
                                String insertUrl = host + "/backend_volley/ws/updateEtudiant.php";

                                requestQueue = Volley.newRequestQueue(context);
                                StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Log.d("edit_message", response);

                                            // Send a broadcast to refresh the main activity
                                            Intent intent = new Intent("refresh_main_activity");
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                            Toast.makeText(context, "Updated Successfully", Toast.LENGTH_LONG).show();
                                        }
                                    }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("edit_error", error.toString());
                                    }
                                }
                                ){
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        String sexe = "";
                                        if(edit_m.isChecked())
                                            sexe = "homme";
                                        else
                                            sexe = "femme";
                                        HashMap<String, String> params = new HashMap<String, String>();
                                        params.put("id", edit_id.getText().toString());
                                        params.put("nom", edit_nom.getText().toString());
                                        params.put("prenom", edit_prenom.getText().toString());
                                        params.put("ville", edit_ville.getSelectedItem().toString());
                                        params.put("sexe", sexe);
                                        if(imageBase64 == null)
                                            params.put("photo", base64Image);
                                        return params;
                                    }
                                };
                                requestQueue.add(request);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder EtudiantViewHolder, int i) {
        if(etudiants != null){
            EtudiantViewHolder.nom.setText(etudiants.get(i).getNom().toUpperCase());
            EtudiantViewHolder.prenom.setText(etudiants.get(i).getPrenom().toUpperCase());
            EtudiantViewHolder.sexe.setText(etudiants.get(i).getSexe().toUpperCase());
            if(EtudiantViewHolder.sexe.getText().equals("FEMME")){
                EtudiantViewHolder.sexe_icon.setImageResource(R.drawable.baseline_female_24);
            }else{
                EtudiantViewHolder.sexe_icon.setImageResource(R.drawable.baseline_male_24);
            }
            EtudiantViewHolder.ville.setText(etudiants.get(i).getVille().toUpperCase());
            EtudiantViewHolder.idss.setText(etudiants.get(i).getId()+"");
            
            String photoBase64 = etudiants.get(i).getPhoto();
            if (photoBase64 != null) {
                EtudiantViewHolder.photo.setImageBitmap(decodeBase64(photoBase64));
            }
        }
    }

    private Bitmap decodeBase64(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public int getItemCount() {
//        return starsFilter.size();
        if(etudiants == null){
            return 0;
        }
        return etudiants.size();
    }

//    @Override
//    public Filter getFilter() {
//        return mfilter;
//    }

    public void removeItem(int position) {
        etudiants.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Etudiant item, int position) {
        etudiants.add(position, item);
        notifyItemInserted(position);
    }

    public List<Etudiant> getData() {
        return etudiants;
    }

    public class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView idss;
        TextView nom;
        TextView prenom;
        TextView sexe;
        TextView ville;
        ImageView sexe_icon;
        CircleImageView photo;
        CardView parent;
        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            idss = itemView.findViewById(R.id.ids);
            photo = itemView.findViewById(R.id.photo);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);
            sexe = itemView.findViewById(R.id.sexe);
            sexe_icon = itemView.findViewById(R.id.sexe_icon);
            ville = itemView.findViewById(R.id.ville);
            parent = itemView.findViewById(R.id.parent);
        }
    }

//    public class NewFilter extends Filter {
//        public RecyclerView.Adapter mAdapter;
//        public NewFilter(RecyclerView.Adapter mAdapter) {
//            super();
//            this.mAdapter = mAdapter;
//        }
//        @Override
//        protected FilterResults performFiltering(CharSequence charSequence) {
//            starsFilter.clear();
//            final FilterResults results = new FilterResults();
//            if (charSequence.length() == 0) {
//                starsFilter.addAll(etudiants);
//            } else {
//                final String filterPattern = charSequence.toString().toLowerCase().trim();
//                for (Etudiant p : etudiants) {
//                    if (p.getName().toLowerCase().startsWith(filterPattern)) {
//                        starsFilter.add(p);
//                    }
//                }
//            }
//            results.values = starsFilter;
//            results.count = starsFilter.size();
//            return results;
//        }
//        @Override
//        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            starsFilter = (List<Etudiant>) filterResults.values;
//            this.mAdapter.notifyDataSetChanged();
//        }
//    }
}
