package com.example.mybestlocation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.mybestlocation.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_SCREEN = 5000;
    private ActivityMainBinding binding;

    Animation topAnim, bottomAnim;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        image = findViewById(R.id.imageView);

        image.setAnimation(topAnim);

        // After the animation finishes, transition to the HomeFragment
        new Handler().postDelayed(() -> {
            // Start the MainNavigationActivity where the fragments are managed
            Intent intent = new Intent(MainActivity.this, MainNavigationActivity.class);
            startActivity(intent);
            finish();  // Close MainActivity after the transition
        }, SPLASH_SCREEN);
    }
}
