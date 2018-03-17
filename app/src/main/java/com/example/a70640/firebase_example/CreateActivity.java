package com.example.a70640.firebase_example;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.a70640.firebase_example.Model.Activity;
import com.example.a70640.firebase_example.Model.Time;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by min on 2018/2/26.
 */

public class CreateActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    //constant to track image chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;
    ProgressDialog progressDialog;
    private Bitmap image;
    private ImageView imageView;
    //uri to store file
    private Uri filePath;
    private String thumbnailURL;
    private String timestamp,time;
    private EditText name;
    private EditText location;
    private EditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_activity);
        setupActionBar();

        progressDialog = new ProgressDialog(this);
        name = findViewById(R.id.name);
        location = findViewById(R.id.location);
        content = findViewById(R.id.content);
        imageView = (ImageView) findViewById(R.id.imageView);

        Button chooseImage = findViewById(R.id.buttonChoose);
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        final Button bt_timestamp = findViewById(R.id.timestamp);
//        timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
//        time = Time.getTime(timestamp);
//        bt_timestamp.setText(Time.getDate(String.valueOf(Calendar.getInstance().getTimeInMillis())) + " " +
//                Time.getTime(String.valueOf(Calendar.getInstance().getTimeInMillis())));
        bt_timestamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                final int mHour = c.get(Calendar.HOUR_OF_DAY); // current hour
                final int mMin = c.get(Calendar.MINUTE); // current minute
                // date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                                Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth,hour,minute);
                                                timestamp = String.valueOf(calendar.getTimeInMillis());
                                                time = Time.getTime(timestamp);
                                                bt_timestamp.setText(Time.getDate(timestamp) + " " + Time.getTime(timestamp));
                                            }
                                        },mHour, mMin,true);
                                timePickerDialog.show();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


    }

    private void createActivity() {
        if (name.getText().length()==0){
            Toast.makeText(getApplicationContext(), "請輸入活動名稱 ", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (location.getText().length()==0){
            Toast.makeText(getApplicationContext(), "請輸入活動地點", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (content.getText().length()==0){
            Toast.makeText(getApplicationContext(), "請輸入活動介紹", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (timestamp.isEmpty()||time.isEmpty()){
            Toast.makeText(getApplicationContext(), "請選取活動時間", Toast.LENGTH_SHORT).show();
            return ;
        }

        Activity activity = new Activity();
        activity.setName(name.getText().toString());
        activity.setDescription(content.getText().toString());
        activity.setLocation(location.getText().toString());
        activity.setDate(timestamp);
        activity.setTime(time);
        uploadFile(activity);
    }

    private void showFileChooser() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, PICK_IMAGE_REQUEST);
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);
            if (image!=null)
                // clear bitmap memory
                image.recycle();

            image = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setImageBitmap(image);
            bitmap.recycle();
        }
    }

    private void firebasePush(final Activity activity) {
        DatabaseReference mDatabase = FirebaseUtil.getBaseRef().getDatabase().getReferenceFromUrl(
                FirebaseUtil.getCurrentUserRef().child("clubAdmin").toString());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final DatabaseReference database = FirebaseDatabase.getInstance().getReferenceFromUrl(dataSnapshot.getValue().toString());
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        database.child(Constants.DATABASE_PATH_ACTIVITY).
                                child(database.push().getKey()).getRef().setValue(activity);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private String uploadFile(final Activity activity) {
        //checking if file is available
        if (filePath != null) {

            //displaying progress dialog while image is uploading
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            //getting the storage reference
            StorageReference sRef = FirebaseStorage.getInstance().getReference().child(
                    Constants.STORAGE_PATH_COVER + System.currentTimeMillis() + "." + getFileExtension(filePath));

            //adding the file to reference
            sRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //dismissing the progress dialog
                            progressDialog.dismiss();
                            progressDialog = null;
                            //displaying success toast
                            Toast.makeText(getApplicationContext(), "Activity Created ", Toast.LENGTH_LONG).show();
                            thumbnailURL = taskSnapshot.getDownloadUrl().toString();
                            activity.setThumbnail(taskSnapshot.getDownloadUrl().toString());
                            firebasePush(activity);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
            return thumbnailURL;
        } else {
            //display an error if no file is selected
            Toast.makeText(getApplicationContext(), "請選取照片", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id= item.getItemId();
        if (id == android.R.id.home) {
            backPressedDialog();
        }else if (id == R.id.action_create) {
            createActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backPressedDialog() {
        new AlertDialog.Builder(CreateActivity.this)
                .setTitle("取消新增活動")
                .setMessage("確定要取消建立活動嗎?")
                .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
    }

    @Override
    public void onBackPressed() {
        backPressedDialog();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("建立活動");

        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
