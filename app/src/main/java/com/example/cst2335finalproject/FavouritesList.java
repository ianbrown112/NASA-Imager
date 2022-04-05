package com.example.cst2335finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Iterator;

public class FavouritesList extends AppCompatActivity {
    ArrayList<NasaImage> favNasaImages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_list);

        Intent intent = getIntent();

        Bundle favsBundle =  intent.getExtras();

        ArrayList<NasaImage> favNasaImages = favsBundle.getParcelableArrayList("favs");

        Iterator favImagesIterator = favNasaImages.iterator();
        while (favImagesIterator.hasNext()) {
            NasaImage nasaImage = (NasaImage) favImagesIterator.next();
            System.out.println(nasaImage.getTitle());
        }
    }
}