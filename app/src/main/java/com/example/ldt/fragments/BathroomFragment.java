package com.example.ldt.fragments;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.example.ldt.R;
import com.example.ldt.activities.HomeActivity;

public class BathroomFragment extends Fragment implements DefaultLifecycleObserver {
    private final Handler handler = new Handler();
    private final Handler skullHandler = new Handler();
    private final ImageView poopAnimationView;
    private final ImageView cleanerView;
    private final ImageView skullView;
    private AnimationDrawable poopAnimation;
    private boolean isCleaning = false;
    private boolean isPoopVisible = false; // Tracks poop visibility state

    public BathroomFragment(ImageView poopAnimationView, ImageView cleanerView, ImageView skullView) {
        this.poopAnimationView = poopAnimationView;
        this.cleanerView = cleanerView;
        this.skullView = skullView;

        if (this.skullView == null) {
            Log.e("BathroomFragment", "ivSkull is null in BathroomFragment constructor.");
        } else {
            Log.d("BathroomFragment", "ivSkull is properly initialized in BathroomFragment.");
        }
    }


    private final Runnable poopTask = new Runnable() {
        @Override
        public void run() {
            if (poopAnimationView == null || isCleaning) {
                Log.d("PoopAnimationManager", "PoopAnimationView is null or cleaning in progress. Exiting animation.");
                handler.postDelayed(this, 30000); // Retry after 30 seconds
                return;
            }

            Log.d("PoopAnimationManager", "Making poop animation visible");
            setPoopVisible(true);

            // Start the skull timer only after poop becomes visible
            Log.d("BathroomFragment", "Starting skull timer.");
            skullHandler.postDelayed(skullTask, 60000); // Skull appears 60 seconds after poop

            // Reschedule poopTask to repeat after 30 seconds (if needed for looped behavior)
            handler.postDelayed(this, 30000);
        }
    };

    private final Runnable skullTask = new Runnable() {
        @Override
        public void run() {
            if (skullView == null || skullView.getVisibility() == View.VISIBLE) {
                Log.e("BathroomFragment", "Skull is already visible or ivSkull is null.");
                triggerSickFragment();
                return; // Prevent duplicate triggers
            }

            Log.d("BathroomFragment", "Poop animation neglected for too long. Triggering skull in SickFragment.");
            showSkull();
        }
    };


    private void showSkull() {
        if (skullView == null) {
            Log.e("BathroomFragment", "ivSkull is null. Cannot make it visible.");
            return; // Prevent crash
        }

        Log.d("BathroomFragment", "Making ivSkull visible.");
        skullView.setVisibility(View.VISIBLE); // Show the skull
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("PoopAnimationManager", "Starting poop and skull handlers.");
        handler.postDelayed(poopTask, 30000);
        skullHandler.postDelayed(skullTask, 60000); // Trigger skull after 60 seconds of poop

        // Restore poop visibility
        if (isPoopVisible) {
            setPoopVisible(true);
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("PoopAnimationManager", "Stopping poop and skull handlers.");
        handler.removeCallbacks(poopTask);
        skullHandler.removeCallbacks(skullTask);

        // Keep track of poop visibility state
        isPoopVisible = poopAnimationView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getLifecycle().addObserver(this);
    }


    public void setPoopVisible(boolean visible) {
        isPoopVisible = visible;

        if (poopAnimationView == null) return;

        if (visible) {
            poopAnimationView.setVisibility(View.VISIBLE);
            startPoopAnimation();
        } else {
            poopAnimationView.setVisibility(View.INVISIBLE);
            stopPoopAnimation();
        }
    }

    private void startPoopAnimation() {
        if (poopAnimationView != null) {
            poopAnimationView.setBackgroundResource(R.drawable.animation_poop);
            poopAnimation = (AnimationDrawable) poopAnimationView.getBackground();
            poopAnimation.start();
        }
    }

    private void stopPoopAnimation() {
        if (poopAnimation != null) {
            poopAnimation.stop();
        }
    }

    private void triggerSickFragment() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment == null || !(parentFragment.getActivity() instanceof HomeActivity)) {
            Log.e("BathroomFragment", "Cannot trigger SickFragment. Parent fragment or activity is invalid.");
            return;
        }

        HomeActivity homeActivity = (HomeActivity) parentFragment.getActivity();
        homeActivity.showSickFragment();
    }

}
