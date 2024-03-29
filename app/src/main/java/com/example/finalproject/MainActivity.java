package com.example.finalproject;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Movie;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    // Member variables.
    private RecyclerView mRecyclerView;
    private List<MovieItem> mMovieArray;
    private MovieListAdapter mAdapter;
    public static final int ADD_MOVIE_ITEM_REQUEST_CODE = 1;

    //Database
    private MovieViewModel mMovieViewModel;
    private MovieRepository mRepository;

    //FOR SHARED PREFERENCES
    private SharedPreferences mPreferences;
    private final String sharedPrefFile = "com.example.android.FinalProjectSharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int gridColumnCount = getResources().getInteger(R.integer.grid_column_count);

        // Initialize the RecyclerView.
        mRecyclerView = findViewById(R.id.recyclerView);

        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumnCount));

        // Initialize the ArrayList that will contain the data.
        mMovieArray = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new MovieListAdapter(this, mMovieArray);
        mRecyclerView.setAdapter(mAdapter);

        // Get the data.
        //initializeData();

        // For the floating action button.
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddMovieItem.class);
                startActivityForResult(intent, ADD_MOVIE_ITEM_REQUEST_CODE);

            }
        });

        //FOR SHARED PREFERENCES
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        android.support.v7.preference.PreferenceManager
                .setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mPreferences = android.support.v7.preference.PreferenceManager
                .getDefaultSharedPreferences(this);

        //getting viewModel of database
        mMovieViewModel = ViewModelProviders.of(this).get(MovieViewModel.class);

        //observer for the live data
        mMovieViewModel.getmAllMovies().observe(this, new Observer<List<MovieItem>>()
        {
            @Override
            public void onChanged(@Nullable final List<MovieItem> movies)
            {
                // Update the cached copy of the words in the adapter.
                mAdapter.setMovies(movies);
            }
        });

        // Shared preferences - night mode
        enableNightMode();

        // Shared preferences - nickname greeting
        String nickname = mPreferences.getString(SettingsActivity.NICKNAME_PREFERENCE, "Moviegoer");
        Toast.makeText(this, "Welcome back, " + nickname, Toast.LENGTH_SHORT).show();

        // Add functionality for swiping to delete items in recyclerview.
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.d("MainActivity", "I'm in onSwiped");
                int position = viewHolder.getAdapterPosition();
                MovieItem movie = mAdapter.getMovieAtPosition(position);
                Toast.makeText(MainActivity.this, "Deleting " + movie.getTitle(),
                        Toast.LENGTH_SHORT).show();
                mMovieViewModel.delete(movie);
            }
        });
        helper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Menu Options
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // For adding a new movie to the list.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_MOVIE_ITEM_REQUEST_CODE && resultCode == RESULT_OK) {
            String title = data.getStringExtra(AddMovieItem.EXTRA_TITLE);
            String info = data.getStringExtra(AddMovieItem.EXTRA_INFO);
            MovieItem movie = new MovieItem(mMovieArray.size(), title, info, R.drawable.img_coming_soon,
                    "", info, "", "", "", "");
            mMovieViewModel.insert(movie);
        } else {
            Toast.makeText(getApplicationContext(), "Didn't save the movie, a field was left blank.",
                    Toast.LENGTH_SHORT).show();
        }
    }


//COMMENTED OUT AS THE DATA IS NOW INITIALIZED IN ROOM DATABASE
//    //Initialize movie data from resource
//    private void initializeData() {
//        // Get the resources from the XML file.
//        String[] movieArray = getResources()
//                .getStringArray(R.array.array_movieTitles);
//        String[] movieInfo = getResources()
//                .getStringArray(R.array.array_movieInfo);
//        TypedArray movieImageResources =
//                getResources().obtainTypedArray(R.array.array_moviePhotos);
//        String[] movieDirector = getResources()
//                .getStringArray(R.array.array_movieDirector);
//        String[] movieDetails = getResources()
//                .getStringArray(R.array.array_movieDetails);
//        String[] movieCast = getResources()
//                .getStringArray(R.array.array_movieCast);
//        String[] movieRunTime = getResources()
//                .getStringArray(R.array.array_movieRunTimes);
//        String[] movieCastLink = getResources()
//                .getStringArray(R.array.array_castLinks);
//        String[] movieTicketLink = getResources()
//                .getStringArray(R.array.array_ticketLink);
//
//        // Clear the existing data to avoid duplication
//        mMovieArray.clear();
//
//        // Create the ArrayList of Movie objects with titles and information about each sport.
//        for(int i = 0; i < movieArray.length; i++){
//            mMovieArray.add(new MovieItem(movieArray[i],movieInfo[i],
//                    movieImageResources.getResourceId(i,0),
//                    movieDirector[i], movieDetails[i], movieCast[i], movieRunTime[i],
//                    movieCastLink[i], movieTicketLink[i]));
//        }
//
//        movieImageResources.recycle();
//
//        // Notify the adapter of the change.
//        mAdapter.notifyDataSetChanged();
//    }

    //SHARED PREFERENCES
    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.apply();
    }

    //SHARED PREFERENCES
    public void reset(View view)
    {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.clear();
        preferencesEditor.apply();
    }

    // Elton: based on shared preferences, enable night mode style.
    public void enableNightMode() {
        Boolean nightModePref = mPreferences.getBoolean(SettingsActivity.NIGHT_MODE_PREFERENCE, false);
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        Log.d("MainActivity", String.valueOf(nightMode));
        if (nightMode != AppCompatDelegate.MODE_NIGHT_YES && nightModePref) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (nightMode == AppCompatDelegate.MODE_NIGHT_YES && !nightModePref) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    //get all movies
    public void getAllMovie(){ mRepository.getAllMovies();}

    //delete all movies
    public void deleteAll() {mRepository.deleteAll();}

    //delete single movie
    public void deleteMovie(MovieItem movieItem) {mRepository.deleteMovie(movieItem);}
}
