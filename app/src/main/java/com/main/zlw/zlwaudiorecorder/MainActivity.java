package com.main.zlw.zlwaudiorecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.main.zlw.zlwaudiorecorder.recorder.RecordManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.btRecord)
    Button btRecord;
    @BindView(R.id.btStop)
    Button btStop;
    @BindView(R.id.tvState)
    TextView tvState;
    private boolean isStart = false;
    private boolean isPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.btRecord, R.id.btStop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btRecord:
                if (isStart) {
                    RecordManager.getInstance().pasue();
                    tvState.setText("暂停");
                    btRecord.setText("开始");
                    isPause = true;
                    isStart = false;
                } else {
                    if (isPause) {
                        RecordManager.getInstance().resume();
                    } else {
                        RecordManager.getInstance().start();
                    }
                    tvState.setText("录音中");
                    btRecord.setText("暂停");
                    isStart = true;
                }

                break;
            case R.id.btStop:
                RecordManager.getInstance().stop();
                tvState.setText("----");
                btRecord.setText("开始");
                isPause = false;
                isStart = false;
                break;
            default:
                break;
        }
    }
}
