package etranspo.ph.Activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.VideoView;

import etranspo.ph.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends Activity implements MediaPlayer.OnCompletionListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        VideoView video = (VideoView) findViewById(R.id.videoView);
        video.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.splash);
        video.start();
        video.setOnCompletionListener((MediaPlayer.OnCompletionListener) this);
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}