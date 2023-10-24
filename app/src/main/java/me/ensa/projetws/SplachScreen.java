package me.ensa.projetws;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;

public class SplachScreen extends AppCompatActivity {
    private LottieAnimationView lottieAnimationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splach_screen);

//        lottieAnimationView = findViewById(R.id.lottieAnimationView);
//        lottieAnimationView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplachScreen.this, MainActivity.class));
//                decorView.setSystemUiVisibility(0);
                finish();
            }
        }, 3000);
    }

}