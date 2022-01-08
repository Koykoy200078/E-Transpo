package etranspo.ph.Activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.VideoView;

import etranspo.ph.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends Activity implements MediaPlayer.OnCompletionListener
{
    SharedPreferences loginPreference;
    String MY_PREF = "my_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize SharePreference
        loginPreference = getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);

        // this condition will do the trick.
        if(loginPreference.getString("tag", "notok").equals("notok")){

            // add tag in SharedPreference here..
            SharedPreferences.Editor edit = loginPreference.edit();
            edit.putString("tag", "ok");
            edit.apply();

            // your logic of splash will go here.
            setContentView(R.layout.activity_splash_screen);

        }
        else if(loginPreference.getString("tag", null).equals("ok"))
        {
            Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        this.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        VideoView video = findViewById(R.id.videoView);
        video.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.splash);
        video.start();
        video.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}