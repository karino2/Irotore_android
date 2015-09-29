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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    ColorPickerView colorPickerView;
    ColorPanelView answerPanel;
    ColorPanelView selectedPanel;
    Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irotore);
        targetView = (TargetImageView)findViewById(R.id.targetimage_content);

        handleSendIntent();
        handleStoredUriIfNecessary();

        colorPickerView = (ColorPickerView)findViewById(R.id.colorpicker_view);
        colorPickerView.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                ColorPanelView selectedPanel = getSelectedColorPanelView();
                selectedPanel.setColor(color);
            }
        });

        answerPanel = (ColorPanelView)findViewById(R.id.answer_color_panel);
        selectedPanel = (ColorPanelView)findViewById(R.id.selected_color_panel);
        actionButton = (Button)findViewById(R.id.action_button);
        actionButton.setText("Go");


        setDefaultAnswerColor();

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionClicked();
            }
        });


        if(scenario == null)
        {
            chooseTargetImage();
            return;
        }

        setupNewScenario();

    }

    private ColorPanelView getSelectedColorPanelView() {
        return (ColorPanelView)findViewById(R.id.selected_color_panel);
    }

    private void setDefaultAnswerColor() {
        answerPanel.setColor(0xFFE6E6E6);
    }

    final int STATE_SELECT = 1;
    final int STATE_ANSWER = 2;

    int currentState = STATE_SELECT;
    private void onActionClicked() {
        if(currentState == STATE_SELECT) {
            currentState = STATE_ANSWER;
            actionButton.setText("Next");

            int answer = targetView.getAnswerColor();
            int selected = getSelectedColorPanelView().getColor();

            answerPanel.setColor(answer);
            answerPanel.setSelected(true);
            selectedPanel.setSelected(false);
            colorPickerView.setColor(answer);

            checkAnswer(selected, answer);


        } else { // ANSWER, goto next.
            currentState = STATE_SELECT;
            actionButton.setText("Go");


            setDefaultAnswerColor();
            answerPanel.setSelected(false);
            selectedPanel.setSelected(true);
            colorPickerView.setColor(getSelectedColorPanelView().getColor());

            scenario.gotoNextScenarioItem();
            applyCurrentScenario();
        }
    }

    private void applyCurrentScenario() {
        ScenarioItem item = scenario.getCurrentItem();
        targetView.setTargetXY(item.getTargetX(), item.getTargetY());
    }

    private void checkAnswer(int selected, int answer) {
        int sr = (selected & 0x00FF0000) >> 16;
        int sg = (selected & 0x0000FF00) >> 8;
        int sb = (selected & 0x000000FF);

        int ar = (answer & 0x00FF0000) >> 16;
        int ag = (answer & 0x0000FF00) >> 8;
        int ab = (answer & 0x000000FF);

        int diff = 0;
        diff += Math.abs(sr - ar);
        diff += Math.abs(sg-ag);
        diff += Math.abs(sb-ab);

        StringBuilder bldr = new StringBuilder();
        bldr.append("Diff: ");
        bldr.append(diff);
        bldr.append('\n');
        bldr.append("Selected: ");
        bldr.append(String.format("0x%x", selected));
        bldr.append('\n');
        bldr.append("Answer: ");
        bldr.append(String.format("0x%x", answer));
        bldr.append('\n');
        targetView.outputDebug(bldr);

        TextView console = (TextView)findViewById(R.id.output_textview);
        console.setText(bldr.toString());
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

            applyCurrentScenario();

            saveUri(scenario.getTargetImage());
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