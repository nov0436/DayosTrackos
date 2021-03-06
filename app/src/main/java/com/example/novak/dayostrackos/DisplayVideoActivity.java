package com.example.novak.dayostrackos;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DisplayVideoActivity extends AppCompatActivity implements View.OnClickListener {

    private Database db;

    static final int GET_CATEGORY_FROM_LISTVIEW = 2;

    EditText titleEditText;
    EditText contentEditText;

    TextView dateTimeDisplayTextView;
    TextView dateTimeHintTextView;

    TextView selectedCategoryTextView;
    TextView locationTextView;

    ImageView btnDeleteRecord;
    ImageView btnSaveForm;
    ImageView thumbnailImageView;
    ImageView playImageView;

    Button btnSelectCategory;

    String title;
    String content;
    String type;
    String locationCity;
    String dateTime;
    String category;
    String link_to_resource;

    Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_video);

        Intent incomingIntent = getIntent();
        record = (Record) incomingIntent.getSerializableExtra("recordObject");


        ///
        btnDeleteRecord = (ImageView) findViewById(R.id.deleteImageView);
        btnSaveForm = (ImageView) findViewById(R.id.saveImageView);
        btnSelectCategory = (Button) findViewById(R.id.buttonSelectCategory);

        btnDeleteRecord.setOnClickListener(this);
        btnSaveForm.setOnClickListener(this);
        btnSelectCategory.setOnClickListener(this);

        dateTimeDisplayTextView = (TextView) findViewById(R.id.dateTimeDisplayTextView);
        dateTimeDisplayTextView.setOnClickListener(this);

        dateTimeHintTextView = (TextView) findViewById(R.id.dateTimeTextView);
        selectedCategoryTextView = (TextView) findViewById(R.id.selectedCategoryTextView);

        // EditTexts
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        contentEditText = (EditText) findViewById(R.id.noteEditText);

        dateTime = new SimpleDateFormat("yyyy-MM-dd   HH:mm").format(new Date());

        locationTextView = (TextView) findViewById(R.id.locationResultTextView);

        thumbnailImageView = (ImageView) findViewById(R.id.thumbnailImageView);
        playImageView = (ImageView) findViewById(R.id.playImageView);

        displayThumbnail();

        thumbnailImageView.setOnClickListener(this);
        playImageView.setOnClickListener(this);

        // setting of fields from the incoming object
        if (record.getLocation() != null)
            locationTextView.setText(record.getLocation());
        else
            locationTextView.setText(getResources().getString(R.string.location_not_specified));

        link_to_resource = record.getLinkToResource();

        titleEditText.setText(record.getTitle());
        contentEditText.setText(record.getText());
        selectedCategoryTextView.setText(record.getCategory());
        dateTimeDisplayTextView.setText(record.getDatetime());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.saveImageView:
                if (formIsValid()) {
                    saveData();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.form_not_valid), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.deleteImageView:
                deleteData();
                break;

            case R.id.buttonSelectCategory:
                Intent intentCategory = new Intent(this, CategoryActivity.class);
                startActivityForResult(intentCategory, GET_CATEGORY_FROM_LISTVIEW);
                break;

            case R.id.thumbnailImageView:
                dispatchPlayVideoIntent();
                break;

            case R.id.playImageView:
                dispatchPlayVideoIntent();
                break;
        }
    }

    private void deleteData() {
        this.db = new Database(getApplicationContext());
        long deleteSuccess = this.db.delete(record.id);

        if (deleteSuccess != 0) {
            Toast.makeText(this, getResources().getString(R.string.toast_video_deleted), Toast.LENGTH_SHORT).show();
            finish();
        } else
            Toast.makeText(this, getResources().getString(R.string.form_could_not_delete_video), Toast.LENGTH_SHORT).show();
    }

    private void saveData() {
        title = titleEditText.getText().toString();
        content = contentEditText.getText().toString();
        category = selectedCategoryTextView.getText().toString();

        Record recordWithUpdatedValues = new Record(record.id, title, content, type, dateTime, category, locationCity, link_to_resource);

        this.db = new Database(getApplicationContext());
        long updateSuccess = this.db.update(recordWithUpdatedValues);

        if (updateSuccess != 0) {
            Toast.makeText(this, getResources().getString(R.string.toast_video_saved), Toast.LENGTH_SHORT).show();
            finish();
        } else
            Toast.makeText(this, getResources().getString(R.string.toast_video_not_saved), Toast.LENGTH_SHORT).show();
    }

    private void dispatchPlayVideoIntent() {
        Uri uri = Uri.parse(record.getLinkToResource());

        Intent videoIntent = new Intent();
        videoIntent.setAction(Intent.ACTION_VIEW);
        videoIntent.setDataAndType(uri, "video/mp4");
        startActivity(videoIntent);
    }

    private void displayThumbnail() {
        final int THUMBSIZE = 256;

        Uri newUri = Uri.parse(record.getLinkToResource());

        File videoFile = new File(newUri.getPath());

        Bitmap thumbImage = createThumbnailFromPath(videoFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND);

        thumbnailImageView.setImageBitmap(thumbImage);
    }

    public Bitmap createThumbnailFromPath(String filePath, int type) {
        return ThumbnailUtils.createVideoThumbnail(filePath, type);
    }

    private boolean formIsValid() {
        if (TextUtils.isEmpty(titleEditText.getText().toString())) {
            return false;
        }
        if (TextUtils.isEmpty(contentEditText.getText().toString())) {
            return false;
        }
        if (TextUtils.isEmpty(record.getCategory())) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_CATEGORY_FROM_LISTVIEW) {
            if (resultCode == Activity.RESULT_OK) {
                category = data.getStringExtra("category");
                selectedCategoryTextView.setText(category);
            }
        }
    }
}
