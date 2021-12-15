package etranspo.ph.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    etranspo.ph.SQLite.ORM.HistoryORM h = new etranspo.ph.SQLite.ORM.HistoryORM();
    private ZXingScannerView mScannerView;

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

        ViewGroup contentFrame = findViewById(R.id.content_frame);
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

    @Override
    public void handleResult(final Result rawResult) {
        String displayname = rawResult.toString().trim();
        if(displayname.equals("Christian Franc Carvajal - Easyride Driver"))
        {
            // adding result to history
            String mydate = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            }
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

            Button close = dialog.findViewById(R.id.closeBtn);

            // QR Code Scanner Confirm
            close.setOnClickListener(v1 ->
            {
                startActivity(new Intent(ScannerActivity.this, MainActivity.class));
                Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
                Notify.build(getApplicationContext())
                        .setTitle("Successfully Paid")
                        .setContent("Christian Franc Carvajal - Easyride Driver")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(R.drawable.ic_done_gr)
                        .largeCircularIcon()
                        .setColor(R.color.purple_700)
                        .show();
                playNotificationSound();
            });
            dialog.setOnCancelListener(dialog1 -> mScannerView.resumeCameraPreview(ScannerActivity.this));
            dialog.show();
        }
        {
            Toast.makeText(ScannerActivity.this, "This QR code is not supported by E-Transpo. Please scan a valid QR code !", Toast.LENGTH_SHORT).show();
            onResume();
        }
    }

    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://"+ getApplicationContext().getPackageName()
                    + "/" + R.raw.notification);
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
}