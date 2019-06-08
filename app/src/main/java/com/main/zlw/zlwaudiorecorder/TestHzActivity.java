package com.main.zlw.zlwaudiorecorder;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.main.zlw.zlwaudiorecorder.base.MyApp;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zlw.loggerlib.Logger;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordFftDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;

import java.io.File;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestHzActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = TestHzActivity.class.getSimpleName();

    @BindView(R.id.btRecord)
    Button btRecord;
    @BindView(R.id.btStop)
    Button btStop;
    @BindView(R.id.tvState)
    TextView tvState;
    @BindView(R.id.audioView)
    AudioView audioView;
    @BindView(R.id.spUpStyle)
    Spinner spUpStyle;
    @BindView(R.id.spDownStyle)
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
        ButterKnife.bind(this);
        initPermission();
        initAudioView();
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
        recordManager.init(MyApp.getInstance(), BuildConfig.DEBUG);
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

    @OnClick({R.id.btRecord, R.id.btStop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btRecord:
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

                break;
            case R.id.btStop:
                recordManager.stop();
                btRecord.setText("开始");
                isPause = false;
                isStart = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spUpStyle:
                audioView.setStyle(AudioView.ShowStyle.getStyle(STYLE_DATA[position]), audioView.getDownStyle());
                break;
            case R.id.spDownStyle:
                audioView.setStyle(audioView.getUpStyle(), AudioView.ShowStyle.getStyle(STYLE_DATA[position]));
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
