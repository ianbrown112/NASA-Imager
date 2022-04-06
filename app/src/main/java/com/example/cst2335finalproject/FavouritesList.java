package com.example.cst2335finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

public class FavouritesList extends AppCompatActivity {
    private MyListAdapter myAdapter;
    ArrayList<NasaImage> favNasaImages = new ArrayList<NasaImage>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_list);

        ListView myList = findViewById(R.id.ListView1);

        Intent intent = getIntent();

        Bundle favsBundle =  intent.getExtras();

        favNasaImages = favsBundle.getParcelableArrayList("favs");

        System.out.println(favNasaImages.size());

        myList.setAdapter( myAdapter = new MyListAdapter());
    }
    private class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return favNasaImages.size();
        }

        @Override
        public NasaImage getItem(int position) {
            NasaImage item = favNasaImages.get(position);
            return item;
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
        }

        @Override
        public View getView(int position, View old, ViewGroup parent) {
            View newView = old;
            LayoutInflater inflater = getLayoutInflater();

            //make a new row:
            if(newView == null) {
                newView = inflater.inflate(R.layout.list_item, parent, false);

            }
            //set what the text should be for this row:
            TextView tView = newView.findViewById(R.id.itemText);
            NasaImage nasaImage;
            nasaImage = getItem(position);
            tView.setText( nasaImage.getTitle() );

            //return it to be put in the table
            return newView;

        }

    };
}