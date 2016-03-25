package com.jackie.puzzlegame.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jackie.puzzlegame.R;
import com.jackie.puzzlegame.view.PuzzleLayout;

public class MainActivity extends AppCompatActivity {
    private PuzzleLayout mPuzzleLayout;
    private TextView mLevel;
    private TextView mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPuzzleLayout = (PuzzleLayout) findViewById(R.id.puzzle_layout);
        mLevel = (TextView) findViewById(R.id.level);
        mTime = (TextView) findViewById(R.id.time);

        mPuzzleLayout.setTimeEnabled(true);
        mPuzzleLayout.setOnPuzzleGameListener(new PuzzleLayout.OnPuzzleGameListener() {
            @Override
            public void nextLevel(final int nextLevel) {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Game Info").setMessage("LEVEL UP !!!")
                        .setPositiveButton("NEXT LEVEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPuzzleLayout.nextLevel();
                                mLevel.setText(nextLevel + "");
                            }
                        }).create();

                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            @Override
            public void timeChanged(int currentTime) {
                mTime.setText(currentTime + "");
            }

            @Override
            public void gameOver() {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Game Info").setMessage("Game over !!!")
                        .setPositiveButton("RESTART", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPuzzleLayout.restart();
                            }
                        }).setNegativeButton("QUIT",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPuzzleLayout.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPuzzleLayout.resume();
    }
}
