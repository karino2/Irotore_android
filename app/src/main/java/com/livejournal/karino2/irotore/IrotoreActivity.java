package com.livejournal.karino2.irotore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IrotoreActivity extends AppCompatActivity {

    final int REQUEST_GET_IMAGE = 1;
    RandomScenario scenario;
    GameState gameState;

    Uri getStoredUri() {
        SharedPreferences prefs = getMyPreferences();
        String uriStr = prefs.getString("target_uri", null);
        if(uriStr == null)
            return null;
        return Uri.parse(uriStr);
    }

    private SharedPreferences getMyPreferences() {
        return getSharedPreferences("prefs", MODE_PRIVATE);
    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    TargetImageView targetView;
    ColorPickerView colorPickerView;
    ColorPanelView answerPanel;
    ColorPanelView selectedPanel;
    Button actionButton;
    ScoreRecorder scoreRecorder;
    Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irotore);

        scoreRecorder = new ScoreRecorder(getMyPreferences());

        targetView = (TargetImageView)findViewById(R.id.targetimage_content);

        handleSendIntent();
        loadScenarioIfNecessaryAndPossible();

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
        answerPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAnswerPanelClicked();
            }
        });
        selectedPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedColorPanelClicked();
            }
        });

        skipButton = (Button)findViewById(R.id.buttonSkip);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Skip();
            }
        });

        actionButton = (Button)findViewById(R.id.action_button);
        actionButton.setText(getString(R.string.label_check));


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

    private void Skip() {
        scenario.skipCurrent();
        applyCurrentScenario();
    }

    private void onSelectedColorPanelClicked() {
        if(gameState.getCurrentState() == GameState.STATE_SELECT)
            return;
        if(selectedPanel.isPanelSelected())
            return;
        selectedPanel.setPanelSelected(true);
        answerPanel.setPanelSelected(false);
        colorPickerView.setColor(selectedPanel.getColor());
    }

    private void onAnswerPanelClicked() {
        if(gameState.getCurrentState() == GameState.STATE_SELECT)
            return;
        if(answerPanel.isPanelSelected())
            return;

        answerPanel.setPanelSelected(true);
        selectedPanel.setPanelSelected(false);
        colorPickerView.setColor(answerPanel.getColor());
    }

    private ColorPanelView getSelectedColorPanelView() {
        return (ColorPanelView)findViewById(R.id.selected_color_panel);
    }

    private void setDefaultAnswerColor() {
        answerPanel.setColor(0xFFE6E6E6);
    }

    private void onActionClicked() {
        if (gameState.getCurrentState() == GameState.STATE_FINISH) {
            gameState.restart();
            becomeSelectState();

            return;
        }

        gameState.gotoNextState();

        if (gameState.getCurrentState() == GameState.STATE_ANSWER) {
            actionButton.setText(getString(R.string.label_next));

            int diff = handleAnswerAndCalcDiffCommon();

            StringBuilder bldr = new StringBuilder();
            bldr.append(getString(R.string.label_diff));
            bldr.append(": ");
            bldr.append(diff);

        /*
        bldr.append('\n');
        bldr.append("Selected: ");
        bldr.append(String.format("0x%x", selected));
        bldr.append('\n');
        bldr.append("Answer: ");
        bldr.append(String.format("0x%x", answer));
        bldr.append('\n');
        targetView.outputDebug(bldr);
        */


            String result = bldr.toString();
            displayResult(result);
        }else if (gameState.getCurrentState() == GameState.STATE_FINISH){
            showMessage(getString(R.string.label_done));


            actionButton.setText(getString(R.string.label_start));


            int diff = handleAnswerAndCalcDiffCommon();
            double mean = scoreRecorder.calculateAverage();
            double pastAverage = scoreRecorder.setNewAverageAndResetScores(mean);

            StringBuilder bldr = new StringBuilder();
            bldr.append(getString(R.string.label_diff));
            bldr.append(": ");
            bldr.append(diff);
            bldr.append('\n');

            bldr.append(getString(R.string.label_score));
            bldr.append(": ");
            bldr.append(String.format("%.2f", mean));
            bldr.append('\n');

            bldr.append(getString(R.string.label_past_average));
            bldr.append(": ");
            bldr.append(String.format("%.2f", pastAverage));

            String result = bldr.toString();
            displayResult(result);

            setStateLabel(getString(R.string.label_done));

        } else { // State == SELECT
            becomeSelectState();
        }
    }

    private int handleAnswerAndCalcDiffCommon() {
        int answer = targetView.getAnswerColor();
        int selected = getSelectedColorPanelView().getColor();

        becomeAnswerState(answer);

        int diff = calculateColorDiff(selected, answer);
        scoreRecorder.addScore(diff);
        return diff;
    }

    private void becomeAnswerState(int answer) {
        answerPanel.setColor(answer);
        answerPanel.setPanelSelected(true);
        selectedPanel.setPanelSelected(false);
        colorPickerView.setColor(answer);
        skipButton.setEnabled(false);
    }

    private void becomeSelectState() {
        actionButton.setText(getString(R.string.label_check));

        setDefaultAnswerColor();
        answerPanel.setPanelSelected(false);
        selectedPanel.setPanelSelected(true);
        colorPickerView.setColor(getSelectedColorPanelView().getColor());
        skipButton.setEnabled(true);

        applyCurrentScenario();
    }

    private void applyCurrentScenario() {
        ScenarioItem item = gameState.getCurrentScenarioItem();
        targetView.setTargetXY(item.getTargetX(), item.getTargetY());

        updateStatusLabel(scenario.getCurrentIndex() + 1, scenario.getTotalItemNum());
    }

    private void displayResult(String result) {
        TextView console = (TextView)findViewById(R.id.output_textview);
        console.setText(result);
    }

    private int calculateColorDiff(int selected, int answer) {
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
        return diff;
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(scenario == null) {
            loadScenarioIfNecessaryAndPossible();
        }
    }



    private void setupNewScenario() {
        InputStream is = null;
        try {
            gameState = new GameState(scenario);

            is = openFileInput(TARGET_FILE_NAME);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            scenario.setSize(options.outWidth, options.outHeight);

            targetView.setImage(bitmap);
            applyCurrentScenario();
            becomeSelectState();
            scoreRecorder.resetCurrentScores();

            updateStatusLabel(scenario.getCurrentIndex() + 1, scenario.getTotalItemNum());
        } catch (FileNotFoundException e) {
            showMessage("file not found: " + e.getMessage());
        } finally {
            try {
                if(is != null)
                   is.close();
            } catch (IOException e) {
                showMessage("is close fail. What situation!?");
            }
        }
    }

    private void updateStatusLabel(int currentIndexForDisplay, int totalNum) {
        String parcent = String.format("%d/%d", currentIndexForDisplay, totalNum);
        String label = String.format(getString(R.string.scenario_pos_format), parcent);
        setStateLabel(label);
    }

    private void setStateLabel(String label) {
        TextView tv = (TextView)findViewById(R.id.remain_state_textview);
        tv.setText(label);
    }

    static final String TARGET_FILE_NAME = "target_img.png";

    void saveImageToInternalStorage(Uri uri) {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

            FileOutputStream fos = openFileOutput(TARGET_FILE_NAME, Context.MODE_PRIVATE);

            // Writing the bitmap to the output stream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            showMessage("File not found: " + e.getMessage());
        } catch (IOException e) {
            showMessage("IOException: " + e.getMessage());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GET_IMAGE:
                if(resultCode == RESULT_OK) {
                    String path = data.getData().toString();
                    saveImageToInternalStorage(Uri.parse(path));

                    // now, recreate is not necessary, but keep this line for laziness.
                    scenario = new RandomScenario();
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


    private void loadScenarioIfNecessaryAndPossible() {
        if(scenario == null)
        {
            try {
                FileInputStream fs = openFileInput(TARGET_FILE_NAME);
                scenario = new RandomScenario();
                fs.close();
            } catch (FileNotFoundException e) {
                scenario = null;
            } catch (IOException e) {
                scenario = null;
            }
        }
    }

    private void handleSendIntent() {
        Intent intent = getIntent();
        if(intent != null) {

            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                saveImageToInternalStorage(uri);
                scenario = new RandomScenario();
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
        int id = item.getItemId();

        if(id == R.id.action_open) {
            chooseTargetImage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
