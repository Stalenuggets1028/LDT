package com.example.ldt.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.example.ldt.R;

public class BathroomFragment implements LifecycleObserver {
    private final Handler handler = new Handler();
    private final ImageView poopAnimationView;
    private final ImageView cleanerView;
    private AnimationDrawable poopAnimation;
    private boolean isCleaning = false;

    public BathroomFragment(ImageView poopAnimationView, ImageView cleanerView) {
        this.poopAnimationView = poopAnimationView;
        this.cleanerView = cleanerView;
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
            poopAnimationView.setVisibility(View.VISIBLE);

            // Start poop animation
            poopAnimationView.setBackgroundResource(R.drawable.animation_poop);
            poopAnimation = (AnimationDrawable) poopAnimationView.getBackground();
            poopAnimation.start();

            // Schedule the next check
            handler.postDelayed(this, 30000);
        }
    };

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void start() {
        Log.d("PoopAnimationManager", "Starting poop animation loop");
        handler.postDelayed(poopTask, 30000);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stop() {
        Log.d("PoopAnimationManager", "Stopping poop animation loop");
        handler.removeCallbacks(poopTask);
    }

    public void startCleanerAnimation() {
        if (isCleaning) return; // Prevent multiple animations
        isCleaning = true;

        Log.d("CleanerAnimationManager", "Starting cleaner animation");

        // Make cleaner visible
        cleanerView.setVisibility(View.VISIBLE);

        // Slide cleaner from right to left
        TranslateAnimation cleanerSlide = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, 1.0f, // Start off-screen right
                TranslateAnimation.RELATIVE_TO_PARENT, -1.0f, // End off-screen left
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f);
        cleanerSlide.setDuration(5000); // 5 seconds
        cleanerSlide.setFillAfter(true);

        // Slide poop animation view with cleaner
        TranslateAnimation poopSlide = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, -1.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f);
        poopSlide.setDuration(5000);
        poopSlide.setFillAfter(true);

        cleanerView.startAnimation(cleanerSlide);
        poopAnimationView.startAnimation(poopSlide);

        // Reset views after animation
        new Handler().postDelayed(() -> {
            Log.d("CleanerAnimationManager", "Resetting positions after cleaner animation");

            cleanerView.clearAnimation();
            poopAnimationView.clearAnimation();

            // Hide poop animation for the next 30 seconds
            poopAnimationView.setVisibility(View.INVISIBLE);

            // Reappear poop animation after 30 seconds
            new Handler().postDelayed(() -> {
                Log.d("CleanerAnimationManager", "Making poop animation visible again");
                poopAnimationView.setVisibility(View.VISIBLE);
                poopAnimation.start();
                isCleaning = false; // Allow new cleaning animations
            }, 30000);

            cleanerView.setVisibility(View.GONE);
        }, 5000);
    }
}
