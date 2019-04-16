package com.example.timerz;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText tempTextInput;
    private Button tempSet;
    private TextView tempViewCountdown;
    private Button tempBtnStartPause;
    private Button tempBtnReset;

    private CountDownTimer tempViewCountDownTimer;

    private boolean tempTimerRunning;
    private long tempStartInMillis;
    private long tempTimeLeftInMillis;
    private long tempEnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempTextInput = findViewById(R.id.text_input);
        tempSet = findViewById(R.id.set);
        tempViewCountdown = findViewById(R.id.view_countdown);
        tempBtnStartPause = findViewById(R.id.btn_start_pause);
        tempBtnReset = findViewById(R.id.btn_reset);

        tempSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = tempTextInput.getText().toString();
                if(input.length()==0)
                {
                    Toast.makeText(MainActivity.this,"Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(input)*60000;
                if(millisInput==0)
                {
                    Toast.makeText(MainActivity.this,"Please Enter Positive Number",Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                tempTextInput.setText("");
            }
        });

        tempBtnStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempTimerRunning)
                {
                    pauseTimer();
                }
                else
                {
                    startTimer();
                }
            }
        });

        tempBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
    }

    private void setTime(long milliseconds){
        tempStartInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }
    private void startTimer(){
        tempEnd = System.currentTimeMillis()+tempTimeLeftInMillis;

        tempViewCountDownTimer = new CountDownTimer(tempTimeLeftInMillis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tempTimeLeftInMillis=millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                tempTimerRunning=false;
                updateWatchInterface();
            }
        }.start();

        tempTimerRunning=true;
        updateWatchInterface();
    }

    private void pauseTimer(){
        tempViewCountDownTimer.cancel();
        tempTimerRunning=false;
        updateWatchInterface();
    }

    private void resetTimer(){
        tempTimeLeftInMillis = tempStartInMillis;
        updateCountDownText();
        updateWatchInterface();
    }

    private void updateCountDownText(){
        int hours = (int) (tempTimeLeftInMillis/1000)/3600;
        int minutes = (int) ((tempTimeLeftInMillis/1000)%3600)/60;
        int seconds = (int) (tempTimeLeftInMillis/1000)%60;

        String timeLeftFormatted;
        if(hours>0)
        {
            timeLeftFormatted=String.format(Locale.getDefault(),"%d:%02d:%02d",hours,minutes,seconds);
        }
        else
        {
            timeLeftFormatted=String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        }
        tempViewCountdown.setText(timeLeftFormatted);
    }

    private void updateWatchInterface(){
        if(tempTimerRunning)
        {
            tempTextInput.setVisibility(View.INVISIBLE);
            tempSet.setVisibility(View.INVISIBLE);
            tempBtnReset.setVisibility(View.INVISIBLE);
            tempBtnStartPause.setText("Pause");
        }
        else
        {
            tempTextInput.setVisibility(View.VISIBLE);
            tempSet.setVisibility(View.VISIBLE);
            tempBtnStartPause.setText("Start");

            if(tempTimeLeftInMillis<1000)
            {
                tempBtnStartPause.setVisibility(View.INVISIBLE);
            }
            else
            {
                tempBtnStartPause.setVisibility(View.VISIBLE);
            }

            if (tempTimeLeftInMillis<tempStartInMillis)

            {
                tempBtnReset.setVisibility(View.VISIBLE);
            }
            else
            {
                tempBtnReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view!=null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    @Override
    protected  void onStop(){
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", tempStartInMillis);
        editor.putLong("millisLeft", tempTimeLeftInMillis);
        editor.putBoolean("timerRunning", tempTimerRunning);
        editor.putLong("endTime", tempEnd);

        editor.apply();

        if (tempViewCountDownTimer != null) {
            tempViewCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        tempStartInMillis = prefs.getLong("startTimeInMillis", 600000);
        tempTimeLeftInMillis = prefs.getLong("millisLeft", tempStartInMillis);
        tempTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateWatchInterface();

        if (tempTimerRunning) {
            tempEnd = prefs.getLong("endTime", 0);
            tempTimeLeftInMillis = tempEnd - System.currentTimeMillis();

            if (tempTimeLeftInMillis < 0) {
                tempTimeLeftInMillis = 0;
                tempTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }
}
