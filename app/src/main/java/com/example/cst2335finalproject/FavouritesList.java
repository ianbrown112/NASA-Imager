package com.example.cst2335finalproject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Iterator;

public class FavouritesList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private MyListAdapter myAdapter;
    ArrayList<NasaImage> favNasaImages = new ArrayList<NasaImage>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_list);

        Toolbar tBar = findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ListView myList = findViewById(R.id.ListView1);

        /** use intent and bundles to retrieve ArrayList of favourite NasaImage objects */
        Intent intent = getIntent();
        Bundle favsBundle =  intent.getExtras();
        favNasaImages = favsBundle.getParcelableArrayList("favs");

        myList.setAdapter( myAdapter = new MyListAdapter());
    }

    /** MyListAdapter loads all favourite item lists into view */
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

            if(newView == null) {
                newView = inflater.inflate(R.layout.list_item, parent, false);

            }

            TextView tView = newView.findViewById(R.id.itemText);
            Button selectBtn = (Button) newView.findViewById(R.id.selectBtn);
            NasaImage nasaImage;
            nasaImage = getItem(position);
            tView.setText( nasaImage.getTitle() );

            selectBtn.setOnClickListener( click-> {
                Intent selectedImage = new Intent(FavouritesList.this, SelectedImage.class);
                Bundle bundle = new Bundle();
                bundle.putInt("imageIndex", position);
                bundle.putParcelableArrayList("favs", favNasaImages);
                selectedImage.putExtras(bundle);
                startActivity(selectedImage);
            } );
            return newView;
        }
    };

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("favs", favNasaImages);
        switch(item.getItemId())
        {
            case R.id.favourite:
                Intent intent_favs = new Intent(this, FavouritesList.class);
                intent_favs.putExtras(bundle);
                startActivity(intent_favs);
                break;

            case R.id.home:
                Intent intent_home = new Intent(this, MainActivity.class);
                intent_home.putExtras(bundle);
                startActivity(intent_home);
                break;

            case R.id.random:
                Intent intent_random = new Intent(this, RandomImage.class);
                intent_random.putExtras(bundle);
                startActivity(intent_random);
                break;

            case R.id.exit:
                finishAffinity();
                break;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.help:
                new android.app.AlertDialog.Builder(this).setTitle(getResources().getString(R.string.help))
                        .setMessage(R.string.favs_help).setPositiveButton(android.R.string.ok, null).show();
                break;
        }
        return true;
    }

}