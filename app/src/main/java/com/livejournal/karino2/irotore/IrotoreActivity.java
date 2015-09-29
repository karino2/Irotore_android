package com.livejournal.karino2.irotore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class IrotoreActivity extends AppCompatActivity {

    final int REQUEST_GET_IMAGE = 1;
    RandomScenario scenario;

    Uri getStoredUri() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        String uriStr = prefs.getString("target_uri", null);
        if(uriStr == null)
            return null;
        return Uri.parse(uriStr);
    }

    void saveUri(Uri uri) {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("target_uri", uri.toString())
                .commit();
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    TargetImageView targetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irotore);
        targetView = (TargetImageView)findViewById(R.id.targetimage_content);

        handleSendIntent();
        handleStoredUriIfNecessary();

        ColorPickerView colorPickerView = (ColorPickerView)findViewById(R.id.colorpicker_view);
        colorPickerView.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                ColorPanelView selectedPanel = (ColorPanelView)findViewById(R.id.selected_color_panel);
                selectedPanel.setColor(color);
            }
        });

        ColorPanelView answerPanel = (ColorPanelView)findViewById(R.id.answer_color_panel);
        answerPanel.setColor(0xFFE6E6E6);


        if(scenario == null)
        {
            chooseTargetImage();
            return;
        }

        setupNewScenario();

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if(scenario != null) {
            outState.putString("target_uri", scenario.getTargetImage().toString());
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(scenario == null) {
            String uriStr = savedInstanceState.getString("target_uri", null);
            if(uriStr != null)
                scenario = new RandomScenario(Uri.parse(savedInstanceState.getString("target_uri")));
        }
    }

    private void setupNewScenario() {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(scenario.getTargetImage());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            scenario.setSize(options.outWidth, options.outHeight);

            targetView.setImage(bitmap);
            ScenarioItem item = scenario.getCurrentItem();
            targetView.setTargetXY(item.getTargetX(), item.getTargetY());
            saveUri(item.getTargetImage());
        } catch (FileNotFoundException e) {
            showMessage("file not found: " + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                showMessage("is close fail. What situation!?");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GET_IMAGE:
                if(resultCode == RESULT_OK) {
                    String path = data.getData().toString();
                    scenario = new RandomScenario(Uri.parse(path));
                    setupNewScenario();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void chooseTargetImage() {
        showMessage("Choose target image");
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, REQUEST_GET_IMAGE);
    }


    private void handleStoredUriIfNecessary() {
        if(scenario == null)
        {
            Uri uri = getStoredUri();
            if(uri != null)
            {
                scenario = new RandomScenario(uri);
            }
        }
    }

    private void handleSendIntent() {
        Intent intent = getIntent();
        if(intent != null) {

            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                scenario = new RandomScenario(uri);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_irotore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_open) {
            chooseTargetImage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
