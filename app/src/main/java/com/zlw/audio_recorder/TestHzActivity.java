package com.zlw.audio_recorder;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zlw.audio_recorder.base.MyApp;
import com.zlw.audio_recorder.widget.AudioView;
import com.zlw.loggerlib.Logger;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordFftDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;

import java.io.File;
import java.util.Locale;


public class TestHzActivity extends ComponentActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String TAG = TestHzActivity.class.getSimpleName();

    Button btRecord;
    Button btStop;
    TextView tvState;
    AudioView audioView;
    Spinner spUpStyle;
    Spinner spDownStyle;

    private boolean isStart = false;
    private boolean isPause = false;
    final RecordManager recordManager = RecordManager.getInstance();
    private static final String[] STYLE_DATA = new String[]{"STYLE_ALL", "STYLE_NOTHING", "STYLE_WAVE", "STYLE_HOLLOW_LUMP"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_hz);
        initView();
        initPermission();
        initAudioView();
    }

    private void initView() {
        btRecord = findViewById(R.id.btRecord);
        btStop = findViewById(R.id.btStop);
        tvState = findViewById(R.id.tvState);
        audioView = findViewById(R.id.audioView);
        spUpStyle = findViewById(R.id.spUpStyle);
        spDownStyle = findViewById(R.id.spDownStyle);
        btRecord.setOnClickListener(this);
        btStop.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecord();
    }

    @Override
    protected void onStop() {
        super.onStop();
        recordManager.stop();
    }

    private void initAudioView() {
        audioView.setStyle(AudioView.ShowStyle.STYLE_ALL, AudioView.ShowStyle.STYLE_ALL);
        tvState.setVisibility(View.GONE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, STYLE_DATA);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUpStyle.setAdapter(adapter);
        spDownStyle.setAdapter(adapter);
        spUpStyle.setOnItemSelectedListener(this);
        spDownStyle.setOnItemSelectedListener(this);
    }


    private void initPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO})
                .start();
    }

    private void initRecord() {
        recordManager.init(MyApp.getInstance(), true);
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
        String recordDir = String.format(Locale.getDefault(), "%s/Record/com.zlw.main/",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        recordManager.changeRecordDir(recordDir);

        recordManager.setRecordStateListener(new RecordStateListener() {
            @Override
            public void onStateChange(RecordHelper.RecordState state) {
                Logger.i(TAG, "onStateChange %s", state.name());

                switch (state) {
                    case PAUSE:
                        tvState.setText("暂停中");
                        break;
                    case IDLE:
                        tvState.setText("空闲中");
                        break;
                    case RECORDING:
                        tvState.setText("录音中");
                        break;
                    case STOP:
                        tvState.setText("停止");
                        break;
                    case FINISH:
                        tvState.setText("录音结束");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(String error) {
                Logger.i(TAG, "onError %s", error);
            }
        });
        recordManager.setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                Toast.makeText(TestHzActivity.this, "录音文件： " + result.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        });
        recordManager.setRecordFftDataListener(new RecordFftDataListener() {
            @Override
            public void onFftData(byte[] data) {
                byte[] newdata = new byte[data.length - 36];
                for (int i = 0; i < newdata.length; i++) {
                    newdata[i] = data[i + 36];
                }
                audioView.setWaveData(data);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btRecord) {
            if (isStart) {
                recordManager.pause();
                btRecord.setText("开始");
                isPause = true;
                isStart = false;
            } else {
                if (isPause) {
                    recordManager.resume();
                } else {
                    recordManager.start();
                }
                btRecord.setText("暂停");
                isStart = true;
            }
        } else if (view.getId() == R.id.btStop) {
            recordManager.stop();
            btRecord.setText("开始");
            isPause = false;
            isStart = false;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if (parentId == R.id.spUpStyle) {
            audioView.setStyle(AudioView.ShowStyle.getStyle(STYLE_DATA[position]), audioView.getDownStyle());
        } else if (parentId == R.id.spDownStyle) {
            audioView.setStyle(audioView.getUpStyle(), AudioView.ShowStyle.getStyle(STYLE_DATA[position]));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
