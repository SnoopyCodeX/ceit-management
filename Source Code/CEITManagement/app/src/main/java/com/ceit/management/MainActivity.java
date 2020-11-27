package com.ceit.management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import com.ceit.management.util.Constants;
import com.ceit.management.util.DialogUtil;
import com.ceit.management.util.PreferenceUtil;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity
{
    private AppBarConfiguration mAppBarConfiguration;
    private DashboardClickObserver dashboardClickObserver;
    private NavController navController;
    private int currentFragmentID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_dashboard,
                    R.id.nav_teachers,
                    R.id.nav_class,
                    R.id.nav_students,
                    R.id.nav_parents,
                    R.id.nav_settings,
                    R.id.nav_logout
                )
                .setOpenableLayout(drawer)
                .build();

        dashboardClickObserver = new DashboardClickObserver();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener((MenuItem item) -> {
            DialogUtil.warningDialog(this, "Confirm Logout", "Are you sure you want to logout?", "Yes", "No",
                    (dlg) -> {
                        DialogUtil.progressDialog(this, "Logging out...", getResources().getColor(R.color.themeColor), false);

                        PreferenceUtil.getPreference().edit().clear().apply();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    },
                    SweetAlertDialog::dismissWithAnimation, false);

            return true;
        });
        navController.addOnDestinationChangedListener((@NonNull NavController controller, @NonNull NavDestination destination, Bundle arguments) -> {
                currentFragmentID = destination.getId();
            }
        );
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if(currentFragmentID != R.id.nav_dashboard)
            navController.navigate(R.id.nav_dashboard);
        else
            super.onBackPressed();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter(Constants.TRIGGER_FRAGMENT_NAVIGATION);
        registerReceiver(dashboardClickObserver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(dashboardClickObserver);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private class DashboardClickObserver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(Constants.TRIGGER_FRAGMENT_NAVIGATION))
            {
                Bundle extras = intent.getExtras();
                int fragID = extras.getInt(Constants.KEY_TRIGGER_FRAGMENT_NAVIGATE);
                navController.navigate(fragID);
            }
        }
    }
}