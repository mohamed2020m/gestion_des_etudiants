package me.ensa.projetws.utlis;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import me.ensa.projetws.beans.Etudiant;

public class EtudiantDeserializer implements JsonDeserializer<Etudiant> {
    @Override
    public Etudiant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Etudiant etudiant = new Etudiant();
        etudiant.setId(jsonObject.get("id").getAsInt());
        etudiant.setNom(jsonObject.get("nom").getAsString());
        etudiant.setPrenom(jsonObject.get("prenom").getAsString());
        etudiant.setSexe(jsonObject.get("sexe").getAsString());
        etudiant.setVille(jsonObject.get("ville").getAsString());

        JsonElement photoElement = jsonObject.get("photo");
        if (photoElement.isJsonPrimitive() && photoElement.getAsJsonPrimitive().isString()) {
            etudiant.setPhoto(photoElement.getAsString());
        }
        // Handle other cases if necessary

        return etudiant;
    }
}

