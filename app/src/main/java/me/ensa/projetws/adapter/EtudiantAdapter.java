package me.ensa.projetws.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import me.ensa.projetws.AddEtudiant;
import me.ensa.projetws.MainActivity;
import me.ensa.projetws.R;
import me.ensa.projetws.beans.Etudiant;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.StarViewHolder> { // implements Filterable {
    private static final String TAG = "StarAdapter";
    private List<Etudiant> etudiants;
//    private List<Etudiant> starsFilter;
    private Context context;
//    private NewFilter mfilter;

    public EtudiantAdapter(Context context, List<Etudiant> etudiants) {
        this.etudiants = etudiants;
        this.context = context;
//        starsFilter = new ArrayList<>();
//        starsFilter.addAll(etudiants);
//        mfilter = new NewFilter(this);
    }

    @NonNull
    @Override
    public StarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.list_item, viewGroup, false);
        final StarViewHolder holder = new StarViewHolder(v);

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

//                Bitmap bitmap =
//                        ((BitmapDrawable)((ImageView)v.findViewById(R.id.img)).getDrawable()).getBitmap();
//                img.setImageBitmap(bitmap);
//                bar.setRating(((RatingBar)v.findViewById(R.id.etudiants)).getRating());

                edit_nom.setText(((TextView)v.findViewById(R.id.nom)).getText().toString());
                edit_prenom.setText(((TextView)v.findViewById(R.id.prenom)).getText().toString());

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

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Update Info")
                        .setMessage("Update Your Info:")
                        .setView(popup)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestQueue requestQueue;
                                String insertUrl = "http://192.168.0.131/backend_volley/ws/updateEtudiant.php";

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
    public void onBindViewHolder(@NonNull StarViewHolder starViewHolder, int i) {
        Log.d(TAG, "onBindView call ! "+ i);
//        Glide.with(context)
//                .asBitmap()
//                .load(starsFilter.get(i).getImg())
//                .apply(new RequestOptions().override(100, 100))
//                .into(starViewHolder.img);
        starViewHolder.nom.setText(etudiants.get(i).getNom().toUpperCase());
        starViewHolder.prenom.setText(etudiants.get(i).getPrenom().toUpperCase());
        starViewHolder.sexe.setText(etudiants.get(i).getSexe().toUpperCase());
        if(starViewHolder.sexe.getText().equals("FEMME")){
            starViewHolder.sexe_icon.setImageResource(R.drawable.baseline_female_24);
        }else{
            starViewHolder.sexe_icon.setImageResource(R.drawable.baseline_male_24);
        }
        starViewHolder.ville.setText(etudiants.get(i).getVille().toUpperCase());
        starViewHolder.idss.setText(etudiants.get(i).getId()+"");
    }

    @Override
    public int getItemCount() {
//        return starsFilter.size();
        return  etudiants.size();
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

    public class StarViewHolder extends RecyclerView.ViewHolder {
        TextView idss;
//        ImageView img;
        TextView nom;
        TextView prenom;
        TextView sexe;
        TextView ville;
//        RatingBar etudiants;
        ImageView sexe_icon;
        CardView parent;
        public StarViewHolder(@NonNull View itemView) {
            super(itemView);
            idss = itemView.findViewById(R.id.ids);
//            img = itemView.findViewById(R.id.img);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);
            sexe = itemView.findViewById(R.id.sexe);
            sexe_icon = itemView.findViewById(R.id.sexe_icon);
            ville = itemView.findViewById(R.id.ville);
//            etudiants = itemView.findViewById(R.id.etudiants);
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
