package com.example.ldt.fragments;

import static android.os.Looper.getMainLooper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ldt.R;
import com.example.ldt.activities.HomeActivity;
import com.example.ldt.databinding.ActivityHomeBinding;
import com.example.ldt.databinding.FragmentMainBinding;
import com.example.ldt.db.AppDatabase;
import com.example.ldt.db.Health;
import com.example.ldt.db.TamadexDao;
import com.example.ldt.db.UserDao;

import java.util.Random;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.fragment.app.FragmentManager;


/**
 * @author Erika Iwata
 * @since 4/12/23 <br>
 * Title: Project 2 <br>
 * Description: Main Fragment
 */

public class MainFragment extends Fragment {

    //Declare fields
    private UserDao userDao;
    private TamadexDao tamadexDao;
    private FragmentMainBinding binding;
    private BathroomFragment bathroomFragment;
    private ImageView ivCleaner;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * On Create method
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Setup onCreate
        super.onCreate(savedInstanceState);
    }

    /**
     * On Create View method - Displays fragment
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Assign to fragment-level binding
        binding = FragmentMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Build database
        userDao = Room.databaseBuilder(getContext(), AppDatabase.class, AppDatabase.DB_NAME)
                .allowMainThreadQueries().build().userDao();
        tamadexDao = Room.databaseBuilder(getContext(), AppDatabase.class, AppDatabase.DB_NAME)
                .allowMainThreadQueries().build().tamadexDao();

        // Initialize and attach BathroomFragment
        attachBathroomFragment();

        // Get username
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String usr = sharedPref.getString("usr", "");

        // Find health entry corresponding to current user
        int id = userDao.findByUsername(usr).getUid();
        Health health = userDao.findByUid(id);

        // Access SharedPreferences to determine light state
        sharedPref = getActivity().getSharedPreferences("LightPrefs", Context.MODE_PRIVATE);
        boolean isLightsOn = sharedPref.getBoolean("isLightsOn", true);

        // Lights?
        if (!isLightsOn) { // Lights off
            binding.ivMiddleScreen2.setVisibility(View.INVISIBLE);
            playSleepAnimation(view, binding);
        } else {
            binding.ivMiddleScreen3.setVisibility(View.INVISIBLE);
        }

        // Play idle animation for corresponding tamagotchi
        if (health.getName().equals("Egg")) {
            eggIdleAnimation(view, binding, health, userDao, tamadexDao);
        } else if (health.getName().equals("Tarakotchi")) {
            tarakotchiIdleAnimation(view, binding);
        } else if (health.getName().equals("Hanatchi")) {
            hanatchiIdleAnimation(view, binding);
        } else if (health.getName().equals("Zuccitchi")) {
            zuccitchiIdleAnimation(view, binding);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Re-initialize the binding to ensure it's not null
        if (binding == null) {
            Log.e("MainFragment", "Binding is null in onViewCreated. Re-binding...");
            binding = FragmentMainBinding.bind(view);  // Re-bind the view
        }

        ivCleaner = binding.ivCleaner;

        if (ivCleaner != null) {
            Log.d("MainFragment", "ivCleaner initialized successfully.");
        } else {
            Log.e("MainFragment", "ivCleaner is null.");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear the binding to prevent memory leaks
    }


    private void attachBathroomFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        bathroomFragment = (BathroomFragment) fragmentManager.findFragmentByTag("BATHROOM_FRAGMENT");

        if (binding.ivSkull == null) {
            Log.e("MainFragment", "ivSkull is null in MainFragment.");
        } else {
            Log.d("MainFragment", "ivSkull is initialized in MainFragment.");
        }

        if (bathroomFragment == null) {
            // Instantiate BathroomFragment
            bathroomFragment = new BathroomFragment(binding.ivPoopAnimation, binding.ivCleaner, binding.ivSkull);
            getLifecycle().addObserver(bathroomFragment);


            // Add BathroomFragment as a child fragment
            fragmentManager.beginTransaction()
                    .add(bathroomFragment, "BATHROOM_FRAGMENT")
                    .commitNow();

            Log.d("MainFragment", "BathroomFragment attached.");
        } else {
            Log.d("MainFragment", "BathroomFragment already exists.");
        }

        // Add as a LifecycleObserver
        getLifecycle().addObserver(bathroomFragment);
    }

    public void startCleanerAnimation() {
        if (binding == null || binding.ivCleaner == null) {
            Log.e("MainFragment", "Binding or ivCleaner is null. Animation cannot start.");
            return;
        }

        // Initialize Cleaner Animation
        ImageView ivCleaner = binding.ivCleaner;
        ImageView ivPoopAnimation = binding.ivPoopAnimation;
        ImageView ivMiddleScreen2 = binding.ivMiddleScreen2; // Example for Tamagotchi idle animation

        // Make cleaner and poop animation visible
        ivCleaner.setVisibility(View.VISIBLE);
        ivPoopAnimation.setVisibility(View.VISIBLE);

        // Screen width (to calculate animation distance)
        int screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;

        // Create Animator for the Cleaner
        ObjectAnimator cleanerAnimator = ObjectAnimator.ofFloat(ivCleaner, "translationX", screenWidth, -ivCleaner.getWidth());
        cleanerAnimator.setDuration(5000); // Duration in milliseconds (adjust to control speed)

        // Create Animators for Poop and Idle Animations
        ObjectAnimator poopAnimator = ObjectAnimator.ofFloat(ivPoopAnimation, "translationX", 0, -screenWidth);
        ObjectAnimator idleAnimator = ObjectAnimator.ofFloat(ivMiddleScreen2, "translationX", 0, -screenWidth);

        poopAnimator.setDuration(5000); // Same duration as cleaner to sync movement
        idleAnimator.setDuration(5000);

        // Animator Set to Run Animations Together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(cleanerAnimator, poopAnimator, idleAnimator);
        animatorSet.start();

        // Listener to Handle Post-Animation Behavior
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Hide cleaner after it slides off screen
                ivCleaner.setVisibility(View.GONE);

                // Reset positions of animations
                ivPoopAnimation.setTranslationX(0);
                ivMiddleScreen2.setTranslationX(0);

                // Delay Poop Animation Visibility after Cleaner Animation Ends
                new Handler().postDelayed(() -> ivPoopAnimation.setVisibility(View.INVISIBLE), 0);
            }
        });
    }


    /**
     * Play egg idle animation using timers and handlers
     * This method calls setTamaType() to determine type of tamagotchi after hatching
     * @param view
     * @param binding
     * @param health
     * @param userDao
     * @param tamadexDao
     */
    public void eggIdleAnimation(View view, FragmentMainBinding binding, Health health, UserDao userDao, TamadexDao tamadexDao) {
        //Egg idle animation
        binding.ivMiddleScreen.setImageResource(R.drawable.animation_egg_idle);
        AnimationDrawable eggIdleAnimation = (AnimationDrawable) binding.ivMiddleScreen.getDrawable();
        eggIdleAnimation.start();

        //Timer for egg idle animation
        new Handler(getMainLooper()).postDelayed(() -> {
            binding.ivMiddleScreen.clearAnimation();
            binding.ivMiddleScreen.setVisibility(View.INVISIBLE);
        }, 5000); // 5 second

        //Timer for egg hatching animation (Show hatched egg)
        new Handler(getMainLooper()).postDelayed(() -> {
            binding.ivEgg.setVisibility(View.VISIBLE);
        }, 5000); // 5 second

        //Timer for egg hatching animation (Disappear hatched egg)
        new Handler(getMainLooper()).postDelayed(() -> {
            binding.ivEgg.setVisibility(View.INVISIBLE);
            setTamaType(health, userDao, tamadexDao);
        }, 6000); // 6 seconds

        //Check what type of tamagotchi hatched
        new Handler(getMainLooper()).postDelayed(() -> {
            if (health.getName().equals("Tarakotchi")) {
                tarakotchiIdleAnimation(view, binding);
            } else if (health.getName().equals("Hanatchi")) {
                hanatchiIdleAnimation(view, binding);
            } else if (health.getName().equals("Zuccitchi")) {
                zuccitchiIdleAnimation(view, binding);
            }
        }, 6000); // 6 seconds
    }

    /**
     * Play sleep idle animation
     * @param view
     * @param binding
     */
    private void playSleepAnimation(View view, FragmentMainBinding binding) {
        // Set the ImageView resource to the animation drawable
        binding.ivMiddleScreen3.setImageResource(R.drawable.animation_sleep);
        AnimationDrawable sleepAnimation = (AnimationDrawable) binding.ivMiddleScreen3.getDrawable();
        sleepAnimation.start();
    }


    /**
     * Play tarakotchi idle animation
     * @param view
     * @param binding
     */
    public void tarakotchiIdleAnimation(View view, FragmentMainBinding binding) {
        //Tarakotchi idle animation
        binding.ivMiddleScreen2.setImageResource(R.drawable.animation_tarakotchi_idle);
        AnimationDrawable tarakotchiIdleAnimation = (AnimationDrawable) binding.ivMiddleScreen2.getDrawable();
        tarakotchiIdleAnimation.start();
    }

    /**
     * Play hanatchi idle animation
     * @param view
     * @param binding
     */
    public void hanatchiIdleAnimation(View view, FragmentMainBinding binding) {
        //Hanatchi idle animation
        binding.ivMiddleScreen2.setImageResource(R.drawable.animation_hanatchi_idle);
        AnimationDrawable hanatchiIdleAnimation = (AnimationDrawable) binding.ivMiddleScreen2.getDrawable();
        hanatchiIdleAnimation.start();
    }

    /**
     * Play zuccitchi idle animation
     * @param view
     * @param binding
     */
    public void zuccitchiIdleAnimation(View view, FragmentMainBinding binding) {
        //Zuccitchi idle animation
        binding.ivMiddleScreen2.setImageResource(R.drawable.animation_zuccitchi_idle);
        AnimationDrawable zuccitchiIdleAnimation = (AnimationDrawable) binding.ivMiddleScreen2.getDrawable();
        zuccitchiIdleAnimation.start();
    }


    /**
     * Sets the tamagotchi type after it hatches (based on rarity)
     * @param health
     * @param userDao
     * @param tamadexDao
     */
    public void setTamaType(Health health, UserDao userDao, TamadexDao tamadexDao) {

        //Random number from 1 to 100
        int randNum = new Random().nextInt(100) + 1;

        //Set values
        int firstRarity = tamadexDao.getAllRarities().get(0);
        int secondRarity = tamadexDao.getAllRarities().get(1);

        //Set tamagotchi
        String firstTama = tamadexDao.getAllNames().get(0);
        String secondTama = tamadexDao.getAllNames().get(1);
        String thirdTama = tamadexDao.getAllNames().get(2);

        //Update tamagotchi type
        if (randNum <= firstRarity) {
            health.setName(firstTama);
        } else if (randNum <= secondRarity) {
            health.setName(secondTama);
        } else {
            health.setName(thirdTama);
        }
        userDao.updateHealth(health);
    }
}
