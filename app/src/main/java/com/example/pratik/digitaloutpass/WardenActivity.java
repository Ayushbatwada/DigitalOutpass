package com.example.pratik.digitaloutpass;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WardenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MyStudentsFragment.OnFragmentInteractionListener,
        OutpassRequestFragment.OnFragmentInteractionListener,
        EditProfileFragment.OnFragmentInteractionListener,
        CaretakerRight.OnFragmentInteractionListener{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;
    DatabaseReference curUserRef;
    FirebaseUser currentUser;

    ImageView dpWardenNavHeader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        curUserRef = usersRef.child(currentUser.getUid());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.activity_warden_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_warden);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        final TextView tvName = (TextView) headerView.findViewById(R.id.tvNameWardenCaretakerNavHeader);
        final TextView tvEmail = (TextView) headerView.findViewById(R.id.tvEmailWardenCaretakerNavHeader);
        dpWardenNavHeader = (ImageView) headerView.findViewById(R.id.dpWardenCaretakerNavHeader);

        curUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tvName.setText(dataSnapshot.child("name").getValue(String.class));
                tvEmail.setText(dataSnapshot.child("email").getValue(String.class));
                if (dataSnapshot.child("imageUrl") != null && dataSnapshot.child("imageUrl").getValue(String.class) != null) {

                    Glide.with(WardenActivity.this)
                            .load(dataSnapshot.child("imageUrl").getValue(String.class).toString())
                            .into(dpWardenNavHeader);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        HostelOutpassesFragment hostelOutpassesFragment = HostelOutpassesFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_warden_relative, hostelOutpassesFragment).commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_warden, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_warden_menu:
                mAuth.signOut();
                finish();
                startActivity(new Intent(WardenActivity.this, SplashScreen.class).putExtra("CLASS_NAME", 2));
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.hostelOutpasses:
                HostelOutpassesFragment hostelOutpassesFragment = HostelOutpassesFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_warden_relative, hostelOutpassesFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;


            case R.id.pendingOutpasses:
                OutpassRequestFragment outpassRequestFragment = OutpassRequestFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_warden_relative, outpassRequestFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;
            case R.id.myStudentsItem:

                MyStudentsFragment myStrudentsFragment = MyStudentsFragment.newInstance();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_warden_relative, myStrudentsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                //load fragment here
                break;
            case R.id.navEditWarden:
                EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(FirebaseAuth.getInstance().getCurrentUser());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_warden_relative, editProfileFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;

            case R.id.navCareRight:
                CaretakerRight caretakerRight = CaretakerRight.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_warden_relative, caretakerRight)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;
            case R.id.logout_warden_nav:
                mAuth.signOut();
                finish();
                startActivity(new Intent(WardenActivity.this, SplashScreen.class).putExtra("CLASS_NAME", 2));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_warden_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
