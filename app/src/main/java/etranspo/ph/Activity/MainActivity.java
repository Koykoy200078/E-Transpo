package etranspo.ph.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import etranspo.notify.Notify;
import etranspo.ph.Adapter.ImagesRecyclerAdapter;
import etranspo.ph.Entity.History;
import etranspo.ph.Entity.ImagesList;
import etranspo.ph.Entity.UsersData;
import etranspo.ph.Other.QRCodeFoundListener;
import etranspo.ph.Other.QRCodeImageAnalyzer;
import etranspo.ph.Other.SClick;
import etranspo.ph.R;
import etranspo.ph.alert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity
{
    private TextView userName, fullName, phone, bal;
    private CircleImageView circleImageView;
    private ImagesRecyclerAdapter imagesRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private List<ImagesList> imagesList;
    private static final int IMAGE_REQUEST = 1;
    private StorageTask<UploadTask.TaskSnapshot> storageTask;
    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private UsersData usersData;

    ImageView imageView;
    Camera camera;

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private Button qrCodeFoundButton;
    private String qrCode;

    String driverName = "Christian Franc Carvajal";
    String plateNum = "1432";
    String currentFare = "₱25.00";

    etranspo.ph.SQLite.ORM.HistoryORM h = new etranspo.ph.SQLite.ORM.HistoryORM();
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        imagesList = new ArrayList<>();

        // id
        userName = findViewById(R.id.username);
        fullName = findViewById(R.id.fullname);
        phone = findViewById(R.id.mobile);
        bal = findViewById(R.id.currentBalance);
        circleImageView = findViewById(R.id.profileImage);
        previewView = findViewById(R.id.activity_main_previewView);
        qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                usersData = dataSnapshot.getValue(UsersData.class);
                assert usersData != null;
                //bal.setText(Integer.toString(Math.toIntExact(usersData.getBalance())));
                bal.setText(usersData.getBalance());
                //userName.setText("("+ usersData.getUsername()+")");
                fullName.setText(usersData.getFullname());
                phone.setText(usersData.getMobile());
                if(usersData.getImageURL().equals("default"))
                {
                    circleImageView.setImageResource(R.drawable.default_picture);
                }
                else
                {
                    Glide.with(getApplicationContext()).load(usersData.getImageURL()).into(circleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        circleImageView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            View mView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_select_image_layout, null);

            RecyclerView recyclerView = mView.findViewById(R.id.recyclerView);
            collectOldImages();
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
            recyclerView.setHasFixedSize(true);
            imagesRecyclerAdapter = new ImagesRecyclerAdapter(imagesList, MainActivity.this);
            recyclerView.setAdapter(imagesRecyclerAdapter);
            imagesRecyclerAdapter.notifyDataSetChanged();

            Button openImage = mView.findViewById(R.id.openImages);
            openImage.setOnClickListener(v1 -> openImage());
            builder.setView(mView);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        ImageView history = findViewById(R.id.history);
        ImageView reloadAcc = findViewById(R.id.reload_account);
        history.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
        reloadAcc.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "You can now reload to your nearest Easyride Dauin Branch.", Toast.LENGTH_SHORT).show());
        qrCodeFoundButton.setVisibility(View.INVISIBLE);
        qrCodeFoundButton.setOnClickListener(v -> {
            if (!SClick.check(SClick.BUTTON_CLICK, 3000)) return;
            Toast.makeText(getApplicationContext(), "Please Wait!", Toast.LENGTH_SHORT).show();
            updateData();
        });
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
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

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String _qrCode) {
                String displayname = _qrCode.trim();
                if(displayname.equals("Christian Franc Carvajal - Easyride Driver"))
                {
                    qrCode = _qrCode;
                    qrCodeFoundButton.setVisibility(View.VISIBLE);
                    TextView textView = findViewById(R.id.notSupported);
                    textView.setVisibility(View.GONE);
                }
                else
                {
                    TextView textView = findViewById(R.id.notSupported);
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void qrCodeNotFound() {
                qrCodeFoundButton.setVisibility(View.INVISIBLE);
            }
        }));
        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    private void openImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            if (data.getData() != null) {
                imageUri = data.getData();
                if (storageTask != null && storageTask.isInProgress()) {
                    Toast.makeText(MainActivity.this, "Uploading is in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImage();
                }
            }
        }
    }

    private void uploadImage()
    {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Uploading Image");
        progressDialog.show();
        if (imageUri != null)
        {
            Bitmap bitmap = null;
            try
            {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
                catch (IOException e)
            {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
            byte[] imageFileToByte = byteArrayOutputStream.toByteArray();
            final StorageReference imageReference = storageReference.child(usersData.getUsername()+System.currentTimeMillis()+".jpg");
            storageTask = imageReference.putBytes(imageFileToByte);
            storageTask.continueWithTask(task -> {
                if (!task.isSuccessful())
                {
                    throw Objects.requireNonNull(task.getException());
                }
                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    Uri downloadUri = task.getResult();
                    String sDownloadUri = downloadUri.toString();
                    Map<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageUrl", sDownloadUri);
                    databaseReference.updateChildren(hashMap);
                    final DatabaseReference profileImagesReference = FirebaseDatabase.getInstance().getReference("profile_images").child(firebaseUser.getUid());
                    profileImagesReference.push().setValue(hashMap).addOnCompleteListener(task1 ->
                    {
                        if (task1.isSuccessful())
                        {
                            progressDialog.dismiss();
                        }
                        else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.changePsw)
        {
            startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class));
        }
        else if (id == R.id.logout)
        {
            firebaseAuth.signOut();
            Toast.makeText(MainActivity.this, "Successfully Logout!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        return true;
    }

    private void collectOldImages()
    {
        DatabaseReference imageListReference = FirebaseDatabase
                .getInstance()
                .getReference("profile_images")
                .child(firebaseUser.getUid());

        imageListReference.addValueEventListener(new ValueEventListener()
        {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                imagesList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    imagesList.add(snapshot.getValue(ImagesList.class));
                }

                imagesRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //int updatedCoins = 100;
    public void updateData()
    {
        TextView viewBal = findViewById(R.id.currentBalance);
        int updatedCoins = Integer.parseInt(viewBal.getText().toString());
        if (updatedCoins < 25)
        {
            Toast.makeText(getApplicationContext(), "Insufficient balance", Toast.LENGTH_SHORT).show();
        }
        else
        {
            updatedCoins = updatedCoins - 25;
            Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_SHORT).show();
            Log.i(MainActivity.class.getSimpleName(), "QR Code Found: " + qrCode);
            Notify.build(getApplicationContext())
                    .setTitle("Successfully Paid! - " + currentFare)
                    .setContent(qrCode)
                    .setSmallIcon(R.drawable.logo)
                    .setLargeIcon(R.drawable.ic_done_gr)
                    .largeCircularIcon()
                    .setColor(R.color.purple_700)
                    .show();

            // adding result to history
            String mydate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mydate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            }
            History history001 = new History();
            history001.setContext("\nDriver Name:\n" + driverName + "\nPlate Number:\n" + plateNum);
            history001.setDate(mydate);
            h.add(getApplicationContext(), history001);
            playNotificationSound();
        }
        TextView test001 = findViewById(R.id.test001);
        test001.setText(String.valueOf(updatedCoins));

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1)
            {
                String newBal = Objects.requireNonNull(test001.getText()).toString();
                databaseReference.child("balance").setValue(newBal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Are you sure you want to close the app?")
                .setConfirmText("Yes")
                .setCancelText("No")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> finish())
                .show();
    }
}