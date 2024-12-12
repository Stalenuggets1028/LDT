package com.example.ldt.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

import com.example.ldt.R;
import com.example.ldt.databinding.ActivityHomeBinding;
import com.example.ldt.databinding.DialogDeleteAcctConfirmBinding;
import com.example.ldt.databinding.DialogExitGameBinding;
import com.example.ldt.db.AppDatabase;
import com.example.ldt.db.Health;
import com.example.ldt.db.UserDao;
import com.example.ldt.fragments.BathroomFragment;
import com.example.ldt.fragments.HealthFragment;
import com.example.ldt.fragments.LightsFragment;
import com.example.ldt.fragments.MainFragment;
import com.example.ldt.fragments.SickFragment;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private UserDao userDao;
    private SickFragment sickFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View binding setup
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize database
        userDao = Room.databaseBuilder(this, AppDatabase.class, AppDatabase.DB_NAME)
                .allowMainThreadQueries().build().userDao();

        // Load MainFragment by default
        if (savedInstanceState == null) {
            replaceFragment(new MainFragment(), "MAIN_FRAGMENT");

            // Add SickFragment to the FragmentManager
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.findFragmentByTag("SICK_FRAGMENT") == null) {
                Log.d("HomeActivity", "Adding SickFragment to FragmentManager.");
                SickFragment sickFragment = new SickFragment();
                fragmentManager.beginTransaction()
                        .add(sickFragment, "SICK_FRAGMENT") // Add without attaching to the container
                        .commitNow(); // Commit immediately
            }
        }

        // Handle tamagotchi hatching (if applicable)
        handleEggHatching();

        // Set up button listeners
        setupNavigationListeners();
    }

    /**
     * Handles tamagotchi hatching logic.
     */
    private void handleEggHatching() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String usr = sharedPref.getString("usr", "");
        int id = userDao.findByUsername(usr).getUid();
        Health health = userDao.findByUid(id);

        if ("Egg".equals(health.getName())) {
            CountDownTimer eggHatchingTimer = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int sec = (int) (millisUntilFinished / 1000);
                    binding.eggHatchingTimer.setText("0:" + sec);
                    binding.eggHatchingTimer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFinish() {
                    binding.eggHatchingTimer.setVisibility(View.INVISIBLE);
                }
            }.start();

            new android.os.Handler(getMainLooper()).postDelayed(() -> {
                binding.ivHealth.setImageResource(R.drawable.health_icon_black);
                binding.ivLights.setImageResource(R.drawable.lights_icon_black);
                binding.ivBathroom.setImageResource(R.drawable.bathroom_icon_black);
                binding.ivSick.setImageResource(R.drawable.sick_icon_black);
            }, 6000);
        } else {
            // Tamagotchi already hatched
            binding.ivHealth.setImageResource(R.drawable.health_icon_black);
            binding.ivLights.setImageResource(R.drawable.lights_icon_black);
            binding.ivBathroom.setImageResource(R.drawable.bathroom_icon_black);
            binding.ivSick.setImageResource(R.drawable.sick_icon_black);
        }
    }

    /**
     * Sets up navigation button listeners for fragment switching.
     */
    private void setupNavigationListeners() {
        binding.ivHealth.setOnClickListener(view -> toggleFragment(new HealthFragment(), "HEALTH_FRAGMENT"));
        binding.ivLights.setOnClickListener(view -> toggleFragment(new LightsFragment(), "LIGHTS_FRAGMENT"));
        binding.ivBathroom.setOnClickListener(view -> handleBathroomAction());
        binding.ivBack.setOnClickListener(view -> openLandingActivity());
        binding.ivExit.setOnClickListener(view -> openExitGameDialog());

        // Sick Icon OnClickListener
        binding.ivSick.setOnClickListener(view -> {
            Log.d("HomeActivity", "Sick icon clicked.");

            // Retrieve SickFragment from FragmentManager
            FragmentManager fragmentManager = getSupportFragmentManager();
            SickFragment sickFragment = (SickFragment) fragmentManager.findFragmentByTag("SICK_FRAGMENT");

            if (sickFragment == null) {
                Log.e("HomeActivity", "SickFragment is null. Re-initializing.");
                sickFragment = new SickFragment();
                fragmentManager.beginTransaction()
                        .add(sickFragment, "SICK_FRAGMENT")
                        .commitNow();
            }

            if (sickFragment.isAdded()) {
                Log.d("HomeActivity", "Calling clearSkull() on SickFragment.");
                sickFragment.clearSkull();
            } else {
                Log.e("HomeActivity", "SickFragment is not added. Cannot clear skull.");
            }
        });
    }

    /**
     * Toggles between the given fragment and MainFragment.
     *
     * @param newFragment The fragment to toggle to.
     * @param tag         The fragment's tag.
     */
    private void toggleFragment(Fragment newFragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView);

        if (currentFragment != null && currentFragment.getTag() != null && currentFragment.getTag().equals(tag)) {
            Log.d("HomeActivity", "Switching back to MainFragment.");
            replaceFragment(new MainFragment(), "MAIN_FRAGMENT");
        } else {
            Log.d("HomeActivity", "Switching to " + tag + ".");
            replaceFragment(newFragment, tag);
        }
    }

    private void initializeSickFragment() {
        // Check if SickFragment is already added to the FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        sickFragment = (SickFragment) fragmentManager.findFragmentByTag("SICK_FRAGMENT");

        if (sickFragment == null) {
            sickFragment = new SickFragment();
            fragmentManager.beginTransaction()
                    .add(sickFragment, "SICK_FRAGMENT")
                    .commitNow(); // Commit immediately to ensure it's available
        }
    }

    /**
     * Shows the skull in SickFragment.
     */
    public void showSickFragment() {
        if (sickFragment == null) {
            Log.e("HomeActivity", "SickFragment is null. Re-initializing.");
            initializeSickFragment();
        }

        Log.d("HomeActivity", "Showing skull in SickFragment.");
        sickFragment.showSkull();
    }

    /**
     * Handles bathroom action by ensuring the MainFragment is loaded and starting animations.
     */
    private void handleBathroomAction() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView);

        if (!(currentFragment instanceof MainFragment)) {
            Log.d("HomeActivity", "Switching to MainFragment for Bathroom action.");
            MainFragment mainFragment = new MainFragment();
            replaceFragment(mainFragment, "MAIN_FRAGMENT");

            new android.os.Handler().postDelayed(() -> {
                Fragment reloadedFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView);
                if (reloadedFragment instanceof MainFragment) {
                    ((MainFragment) reloadedFragment).startCleanerAnimation();
                } else {
                    Log.e("HomeActivity", "MainFragment not found after switching. Cannot start animation.");
                }
            }, 100);
        } else {
            Log.d("HomeActivity", "MainFragment is already active. Starting cleaner animation.");
            ((MainFragment) currentFragment).startCleanerAnimation();
        }
    }

    /**
     * Replaces the current fragment with a new one.
     *
     * @param fragment The new fragment.
     * @param tag      The tag for the fragment.
     */
    private void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment, tag)
                .commit();
    }

    /**
     * Open MainActivity.
     */
    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /**
     * Validates and deletes the user account if allowed.
     *
     * @param usr      The username of the account to delete.
     * @param userDao  The database access object.
     * @param editor   The shared preferences editor.
     * @return True if the account was successfully deleted, false otherwise.
     */
    public boolean isValidDeleteUser(String usr, UserDao userDao, SharedPreferences.Editor editor) {
        Toast toast = new Toast(this);
        TextView tv = new TextView(this);
        Typeface font = ResourcesCompat.getFont(this, R.font.arcade_classic);

        tv.setTypeface(font);
        tv.setTextSize(15);

        if (usr.equals("testuser1") || usr.equals("admin2")) {
            tv.setTextColor(Color.rgb(210, 43, 43));
            tv.setText("Predefined user");
            toast.setView(tv);
            toast.show();
            return false;
        } else {
            editor.clear().apply();
            userDao.deleteHealth(userDao.findByUid(userDao.findByUsername(usr).getUid()));
            userDao.deleteUser(userDao.findByUsername(usr));

            tv.setTextColor(Color.rgb(60, 179, 113));
            tv.setText("Account Deleted");
            toast.setView(tv);
            toast.show();
            return true;
        }
    }

    /**
     * Opens the landing activity.
     */
    public void openLandingActivity() {
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
    }

    /**
     * Opens the exit game dialog.
     */
    private void openExitGameDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        DialogExitGameBinding dialogBinding = DialogExitGameBinding.inflate(getLayoutInflater());

        dialogBuilder.setView(dialogBinding.getRoot());
        Dialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        dialogBinding.btnCloseWindow.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnQuitGame.setOnClickListener(v -> openLandingActivity());
        dialogBinding.btnLogout.setOnClickListener(v -> logoutAndOpenMain());
        dialogBinding.btnDeleteAccount.setOnClickListener(v -> {
            // Get shared preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            String usr = sharedPref.getString("usr", "");

            // Validate and delete user
            if (isValidDeleteUser(usr, userDao, editor)) {
                dialog.dismiss();
                openMainActivity();
            }
        });
    }

    /**
     * Logs out the user and opens the main activity.
     */
    private void logoutAndOpenMain() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.clear().apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void showSickFragmentSkull() {
        if (sickFragment == null) {
            sickFragment = new SickFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag("SICK_FRAGMENT") == null) {
            Log.d("HomeActivity", "Adding SickFragment to FragmentManager.");
            fragmentManager.beginTransaction()
                    .add(sickFragment, "SICK_FRAGMENT")
                    .commitAllowingStateLoss();
        }

        sickFragment.showSkull();
    }

}




//public class HomeActivity extends AppCompatActivity {
//
//    //Declare fields
//    private ActivityHomeBinding binding;
//    private UserDao userDao;
//
//    /**
//     * Tells program what to do when this activity is created
//     * @param savedInstanceState saved state of the application
//     */
////    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        //onCreate setup
//        super.onCreate(savedInstanceState);
//        binding = ActivityHomeBinding.inflate(getLayoutInflater());
//        View view = binding.getRoot();
//        setContentView(view);
//
//        //Build database
//        userDao = Room.databaseBuilder(this, AppDatabase.class, AppDatabase.DB_NAME)
//                .allowMainThreadQueries().build().userDao();
//
//        //Get username
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor editor = sharedPref.edit();
//        String usr = sharedPref.getString("usr", "");
//
//        //Find health entry corresponding to current user
//        int id = userDao.findByUsername(usr).getUid();
//        Health health = userDao.findByUid(id);
//
//        // If tamagotchi hasn't hatched yet
//        if (health.getName().equals("Egg")) {
//            // Timer for egg to hatch (5 sec)
//            CountDownTimer eggHatchingTimer = new CountDownTimer(5000,1000) {
//                @Override
//                public void onTick ( long millisUntilFinished){
//                    //Declare variables
//                    int min = (int) millisUntilFinished / 1000 / 60;
//                    int sec = (int) (millisUntilFinished / 1000) % 60;
//
//                    binding.eggHatchingTimer.setText(min + ":" + sec);
//                    binding.eggHatchingTimer.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onFinish () {
//                    // Make timer disappear
//                    binding.eggHatchingTimer.setVisibility(View.INVISIBLE);
//                }
//            }.start();
//            // Make icons black
//            new Handler(getMainLooper()).postDelayed(() -> {
//                binding.ivHealth.setImageResource(R.drawable.health_icon_black);
//                binding.ivLights.setImageResource(R.drawable.lights_icon_black);
//                binding.ivBathroom.setImageResource(R.drawable.bathroom_icon_black);
//            }, 6000); // 6 second
//
//            // If tamagotchi has already hatched
//        } else {
//            binding.ivHealth.setImageResource(R.drawable.health_icon_black);
//            binding.ivLights.setImageResource(R.drawable.lights_icon_black);
//            binding.ivBathroom.setImageResource(R.drawable.bathroom_icon_black);
//        }
//
//        // Click - Back button
//        binding.ivBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //If user is admin
//                if (userDao.findByUsername(usr).isAdmin()) {
//                    openAdminActivity();
//                } else {
//                    openLandingActivity();
//                }
//
//            }
//        });
//
//        //Click - Exit button
//        binding.ivExit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openExitGameDialog(userDao, usr, editor);
//            }
//        });
//
//        // Click - Health icon
//        binding.ivHealth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Ensure MainFragment is added if not already
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView);
//
//                //Build database
//                UserDao userDao2 = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, AppDatabase.DB_NAME)
//                        .allowMainThreadQueries().build().userDao();
//
//                // Find health entry corresponding to current user
//                int id2 = userDao2.findByUsername(usr).getUid();
//                Health health2 = userDao2.findByUid(id2);
//
//                // If tamagotchi has hatched
//                if (!health2.getName().equals("Egg")) {
//                    // Check if the current fragment is MainFragment
//                    if (currentFragment instanceof HealthFragment) {
//                        Log.d("HomeActivity", "Switching back to MainFragment from HealthFragment.");
//                        // Replace with MainFragment
//                        fragmentManager.beginTransaction()
//                                .replace(R.id.fragmentContainerView, new MainFragment(), "MAIN_FRAGMENT")
//                                .commit();
//                    } else {
//                        Log.d("HomeActivity", "Switching to HealthFragment.");
//                        // Replace with HealthFragment
//                        fragmentManager.beginTransaction()
//                                .replace(R.id.fragmentContainerView, new HealthFragment(), "HEALTH_FRAGMENT")
//                                .commit();
//                    }
//                }
//            }
//        });
//
//        // Click - Lights icon
//        binding.ivLights.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Ensure MainFragment is added if not already
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView);
//
//                //Build database
//                UserDao userDao2 = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, AppDatabase.DB_NAME)
//                        .allowMainThreadQueries().build().userDao();
//
//                // Find health entry corresponding to current user
//                int id2 = userDao2.findByUsername(usr).getUid();
//                Health health2 = userDao2.findByUid(id2);
//
//                // If tamagotchi has hatched
//                if (!health2.getName().equals("Egg")) {
//                    if (currentFragment instanceof LightsFragment) {
//                        Log.d("HomeActivity", "Switching back to MainFragment from LightsFragment.");
//                        // Replace with MainFragment
//                        fragmentManager.beginTransaction()
//                                .replace(R.id.fragmentContainerView, new MainFragment(), "MAIN_FRAGMENT")
//                                .commit();
//                    } else {
//                        Log.d("HomeActivity", "Switching to LightsFragment.");
//                        // Replace with LightsFragment
//                        fragmentManager.beginTransaction()
//                                .replace(R.id.fragmentContainerView, new LightsFragment(), "LIGHTS_FRAGMENT")
//                                .commit();
//                    }
//                }
//            }
//        });
//
//        binding.ivBathroom.setOnClickListener(v -> {
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView);
//
//            if (!(currentFragment instanceof MainFragment)) {
//                Log.d("HomeActivity", "Switching to MainFragment for Bathroom action.");
//                // Replace with MainFragment and start cleaner animation
//                MainFragment mainFragment = new MainFragment();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragmentContainerView, mainFragment, "MAIN_FRAGMENT")
//                        .commit();
//
//                fragmentManager.executePendingTransactions(); // Ensure transaction completes
//
//                // Delay to ensure the fragment is attached before starting animation
//                new Handler().postDelayed(() -> {
//                    if (mainFragment.isAdded()) {
//                        mainFragment.startCleanerAnimation();
//                    } else {
//                        Log.e("HomeActivity", "MainFragment re-attachment failed.");
//                    }
//                }, 100); // Add a small delay
//            } else {
//                Log.d("HomeActivity", "MainFragment is already active. Starting cleaner animation.");
//                ((MainFragment) currentFragment).startCleanerAnimation();
//            }
//        });
//
//    } //End onCreate
//
//    /**
//     * Logout current user
//     */
//    public void logout(SharedPreferences.Editor editor) {
//        editor.clear().apply();
//    }
//
//    /**
//     * Open LandingActivity
//     */
//    public void openLandingActivity() {
//        Intent intent = new Intent(this, LandingActivity.class);
//        startActivity(intent);
//    }
//
//    /**
//     * Open MainActivity
//     */
//    private void openMainActivity() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//    }
//
//    /**
//     * Open AdminActivity
//     */
//    public void openAdminActivity() {
//        Intent intent = new Intent(this, AdminActivity.class);
//        startActivity(intent);
//    }
//
//    /**
//     * Open Exit Menu
//     */
//    public void openExitGameDialog(UserDao userDao, String usr, SharedPreferences.Editor editor) {
//
//        //Open Dialog setup
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        DialogExitGameBinding binding = DialogExitGameBinding.inflate(getLayoutInflater());
//        View view = binding.getRoot();
//
//        //Customize Dialog popup
//        dialogBuilder.setView(view);
//        Dialog dialog = dialogBuilder.create();
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//
//        //Click - Close Window
//        binding.btnCloseWindow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        //Click - quit game button
//        binding.btnQuitGame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //If user is admin
//                if (userDao.findByUsername(usr).isAdmin()) {
//                    openAdminActivity();
//                } else {
//                    openLandingActivity();
//                }
//            }
//        });
//
//        //Click - logout button
//        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                logout(editor);
//                openMainActivity();
//            }
//        });
//
//        //Click - delete account
//        binding.btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                openDeleteAcctDialog(userDao, usr, editor);
//            }
//        });
//
//    } //End openExitMenu
//
//    /**
//     * Open Delete confirm menu
//     */
//    public void openDeleteAcctDialog(UserDao userDao, String usr, SharedPreferences.Editor editor) {
//
//        //Open Dialog setup
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        DialogDeleteAcctConfirmBinding binding = DialogDeleteAcctConfirmBinding.inflate(getLayoutInflater());
//        View view = binding.getRoot();
//
//        //Customize dialog popup
//        dialogBuilder.setView(view);
//        Dialog dialog = dialogBuilder.create();
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//
//        //Click - Close window button
//        binding.btnCloseWindow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        //Click - Yes button
//        binding.btnYes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                //Check if user is okay to delete
//                if (isValidDeleteUser(usr, userDao, editor)) {
//                    openMainActivity();
//                }
//
//            }
//        });
//
//        //Click - No button
//        binding.btnNo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//    }
//
//    /**
//     * Check if it's okay to delete this user
//     */
//    public boolean isValidDeleteUser(String usr, UserDao userDao, SharedPreferences.Editor editor) {
//
//        //Create Toast
//        Toast toast = new Toast(this);
//        TextView tv = new TextView(this);
//        Typeface font = ResourcesCompat.getFont(this, R.font.arcade_classic);
//
//        //Customize Toast
//        tv.setTypeface(font);
//        tv.setTextColor(Color.rgb(210, 43, 43));
//        tv.setTextSize(15);
//
//        //Check if predefined user
//        if (usr.equals("testuser1") || usr.equals("admin2")) {
//            tv.setText("Predefined user");
//            toast.setView(tv);
//            toast.show();
//            return false;
//        } else {
//            //Delete user
//            logout(editor);
//            userDao.deleteHealth(userDao.findByUid(userDao.findByUsername(usr).getUid()));
//            userDao.deleteUser(userDao.findByUsername(usr));
//
//            //Set message
//            tv.setTextColor(Color.rgb(60, 179, 113));
//            tv.setText("Account Deleted");
//            toast.setView(tv);
//            toast.show();
//            return true;
//        }
//    }
//
//}