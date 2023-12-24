package com.zlw.audio_recorder;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Nullable;

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
import com.zlw.main.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;

import java.io.File;
import java.util.Locale;

public class MainActivity extends ComponentActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    Button btRecord;
    Button btStop;
    TextView tvState;
    TextView tvSoundSize;
    RadioGroup rgAudioFormat;
    RadioGroup rgSimpleRate;
    RadioGroup tbEncoding;
    RadioGroup tbSource;
    AudioView audioView;
    Spinner spUpStyle;
    Spinner spDownStyle;

    private boolean isStart = false;
    private boolean isPause = false;
    private final RecordManager recordManager = RecordManager.getInstance();

    private MediaProjectionManager mediaProjectionManager;
    private static final String[] STYLE_DATA = new String[]{"STYLE_ALL", "STYLE_NOTHING", "STYLE_WAVE", "STYLE_HOLLOW_LUMP"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAudioView();
        initEvent();
        initRecord();
        AndPermission.with(this)
                .runtime()
                .permission(new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO})
                .start();
    }

    private void initView() {
        btRecord = findViewById(R.id.btRecord);
        btStop = findViewById(R.id.btStop);
        tvState = findViewById(R.id.tvState);
        btRecord = findViewById(R.id.btRecord);
        tvSoundSize = findViewById(R.id.tvSoundSize);
        rgAudioFormat = findViewById(R.id.rgAudioFormat);
        rgSimpleRate = findViewById(R.id.rgSimpleRate);
        tbEncoding = findViewById(R.id.tbEncoding);
        audioView = findViewById(R.id.audioView);
        spUpStyle = findViewById(R.id.spUpStyle);
        spDownStyle = findViewById(R.id.spDownStyle);
        tbSource = findViewById(R.id.tbSource);
        btRecord.setOnClickListener(this);
        btStop.setOnClickListener(this);
        findViewById(R.id.jumpTestActivity).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecordEvent();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 2000) {
            if (data != null) {
                MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                recordManager.setMediaProjection(mediaProjection);
            }
        }
    }

    private void initAudioView() {
        tvState.setVisibility(View.GONE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, STYLE_DATA);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUpStyle.setAdapter(adapter);
        spDownStyle.setAdapter(adapter);
        spUpStyle.setOnItemSelectedListener(this);
        spDownStyle.setOnItemSelectedListener(this);
    }

    private void initEvent() {
        rgAudioFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbPcm) {
                    recordManager.changeFormat(RecordConfig.RecordFormat.PCM);
                } else if (checkedId == R.id.rbMp3) {
                    recordManager.changeFormat(RecordConfig.RecordFormat.MP3);
                } else if (checkedId == R.id.rbWav) {
                    recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
                }
            }
        });

        rgSimpleRate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb8K) {
                    recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(8000));
                } else if (checkedId == R.id.rb16K) {
                    recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
                } else if (checkedId == R.id.rb44K) {
                    recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(44100));
                }
            }
        });

        tbEncoding.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb8Bit) {
                    recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT));
                } else if (checkedId == R.id.rb16Bit) {
                    recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
                }
            }
        });
        tbSource.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbMic) {
                    recordManager.setSource(RecordConfig.SOURCE_MIC);
                } else if (checkedId == R.id.rbSystem) {
                    recordManager.setSource(RecordConfig.SOURCE_SYSTEM);
                    mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 2000);
                }
            }
        });
    }

    private void initRecord() {
        recordManager.init(MyApp.getInstance(), true);
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
        String recordDir = String.format(Locale.getDefault(), "%s/Record/com.zlw.main/",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        recordManager.changeRecordDir(recordDir);
        initRecordEvent();
    }

    private void initRecordEvent() {
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
                        tvSoundSize.setText("---");
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
        recordManager.setRecordSoundSizeListener(new RecordSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
                tvSoundSize.setText(String.format(Locale.getDefault(), "声音大小：%s db", soundSize));
            }
        });
        recordManager.setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                Toast.makeText(MainActivity.this, "录音文件： " + result.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        });
        recordManager.setRecordFftDataListener(new RecordFftDataListener() {
            @Override
            public void onFftData(byte[] data) {
                audioView.setWaveData(data);
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btRecord) {
            doPlay();
        } else if (id == R.id.btStop) {
            doStop();
        } else if (id == R.id.jumpTestActivity) {
            startActivity(new Intent(this, TestHzActivity.class));
        }
    }

    private void doStop() {
        recordManager.stop();
        btRecord.setText("开始");
        isPause = false;
        isStart = false;
    }

    private void doPlay() {
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
    }


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
        //nothing
    }
}
