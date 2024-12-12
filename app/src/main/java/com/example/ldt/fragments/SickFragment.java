package com.example.ldt.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ldt.R;

public class SickFragment extends Fragment {

    private ImageView ivSkull;
    private ImageView ivSun;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Initialize views
        ivSkull = view.findViewById(R.id.iv_skull);
        ivSun = new ImageView(requireContext());
        ivSun.setImageResource(R.drawable.sun);
        ivSun.setLayoutParams(ivSkull.getLayoutParams());
        ((ViewGroup) view).addView(ivSun);
        ivSun.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivSkull = view.findViewById(R.id.iv_skull);

        if (ivSkull != null) {
            Log.d("SickFragment", "ivSkull successfully initialized.");
        } else {
            Log.e("SickFragment", "ivSkull is null. Check layout or ID.");
        }
    }

    public void showSkull() {
        if (ivSkull == null) {
            Log.e("BathroomFragment", "ivSkull is null. Cannot make it visible.");
            return;
        }

        if (ivSkull.getVisibility() == View.VISIBLE) {
            Log.d("BathroomFragment", "Skull is already visible.");
            return;
        }

        ivSkull.setVisibility(View.VISIBLE);
        Log.d("BathroomFragment", "Skull is now visible.");
    }


    public void clearSkull() {
        requireActivity().runOnUiThread(() -> {
            if (ivSkull == null || ivSkull.getParent() == null) {
                Log.e("SickFragment", "ivSkull or its parent is null. Cannot update.");
                return;
            }

            // Get parent and force layout updates
            ViewGroup parent = (ViewGroup) ivSkull.getParent();
            parent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            parent.layout(0, 0, parent.getMeasuredWidth(), parent.getMeasuredHeight());
            parent.requestLayout();
            parent.invalidate();

            Log.d("SickFragment", "Parent width: " + parent.getWidth() + ", height: " + parent.getHeight());

            if (ivSkull.getVisibility() != View.VISIBLE) {
                Log.d("SickFragment", "Making skull visible.");
                ivSkull.setVisibility(View.VISIBLE);
            }

            // Replace skull with sun
            ivSkull.setImageResource(R.drawable.sun);

            // Delay to clear the sun
            new Handler().postDelayed(() -> {
                ivSkull.setVisibility(View.INVISIBLE);
                Log.d("SickFragment", "Sun animation cleared.");
            }, 2000);
        });
    }
}
