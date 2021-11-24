package etranspo.ph.Activity;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.zxing.Result;

import java.util.Objects;

import etranspo.notify.Notify;
import etranspo.ph.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    // Init ui elements
    //@BindView(R.id.lightButton)
    ImageView flashImageView;

    //Variables
    Intent i;
    etranspo.ph.SQLite.ORM.HistoryORM h = new etranspo.ph.SQLite.ORM.HistoryORM();
    private ZXingScannerView mScannerView;
    private boolean flashState = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Scan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(ScannerActivity.this, MainActivity.class)));

        ActivityCompat.requestPermissions(ScannerActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void handleResult(final Result rawResult) {

        // adding result to history
        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        etranspo.ph.Entity.History history = new etranspo.ph.Entity.History();
        history.setContext(rawResult.getText());
        history.setDate(mydate);
        h.add(getApplicationContext(), history);

        // show custom alert dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog);

        View v = dialog.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);

        TextView text = dialog.findViewById(R.id.someText);
        text.setText(rawResult.getText());

        ImageView img = dialog.findViewById(R.id.imgOfDialog);
        img.setImageResource(R.drawable.ic_done_gr);

        //Button webSearch = (Button) dialog.findViewById(R.id.searchButton);
        Button copy = dialog.findViewById(R.id.copyButton);
        Button close = dialog.findViewById(R.id.closeBtn);
        /*webSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url;
                if(Patterns.WEB_URL.matcher(rawResult.getText()).matches()) {
                    url = rawResult.getText();
                }else {
                    url = "http://www.google.com/#q=" + rawResult.getText();
                }
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                dialog.dismiss();
                mScannerView.resumeCameraPreview(etranspo.ph.Activity.ScannerActivity.this);
            }
        });*/
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", rawResult.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
                mScannerView.resumeCameraPreview(ScannerActivity.this);
            }
        });
        close.setOnClickListener(v1 ->
        {
            startActivity(new Intent(ScannerActivity.this, MainActivity.class));
            Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
            Notify.build(getApplicationContext())
                    .setTitle("Successfully Paid")
                    .setContent("")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(R.drawable.ic_done_gr)
                    .largeCircularIcon()
                    .setColor(R.color.purple_700)
                    .show();
            playNotificationSound();
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mScannerView.resumeCameraPreview(ScannerActivity.this);
            }
        });
        dialog.show();
    }

    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.notification);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(ScannerActivity.this, "Permission to camera is granted", Toast.LENGTH_SHORT).show();
            }
                else
            {
                Toast.makeText(ScannerActivity.this, "Permission to camera is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*@OnClick
    void mainActivityOnClickEvents(View v) {

        switch (v.getId()) {
            case R.id.historyButton:
                i = new Intent(this, HistoryActivity.class);
                startActivity(i);
                break;
            case R.id.lightButton:
                if(flashState==false) {
                    v.setBackgroundResource(R.drawable.ic_flash_off);
                    Toast.makeText(getApplicationContext(), "Flashlight turned on", Toast.LENGTH_SHORT).show();
                    mScannerView.setFlash(true);
                    flashState = true;
                }else if(flashState) {
                    v.setBackgroundResource(R.drawable.ic_flash_on);
                    Toast.makeText(getApplicationContext(), "Flashlight turned off", Toast.LENGTH_SHORT).show();
                    mScannerView.setFlash(false);
                    flashState = false;
                }
                break;
        }

    }*/
}