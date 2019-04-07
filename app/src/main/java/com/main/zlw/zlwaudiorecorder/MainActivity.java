package com.main.zlw.zlwaudiorecorder;

import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.main.zlw.zlwaudiorecorder.base.MyApp;
import com.main.zlw.zlwaudiorecorder.utils.Logger;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;

import java.io.File;
import java.util.Locale;

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
    @BindView(R.id.tvSoundSize)
    TextView tvSoundSize;
    @BindView(R.id.rgAudioFormat)
    RadioGroup rgAudioFormat;
    @BindView(R.id.rgSimpleRate)
    RadioGroup rgSimpleRate;
    @BindView(R.id.tbEncoding)
    RadioGroup tbEncoding;
    private boolean isStart = false;
    private boolean isPause = false;
    final RecordManager recordManager = RecordManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initEvent();
        initRecord();
        AndPermission.with(this)
                .runtime()
                .permission(new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO})
                .start();
    }

    private void initEvent() {
        rgAudioFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbPcm:
                        recordManager.changeFormat(RecordConfig.RecordFormat.PCM);
                        break;
                    case R.id.rbMp3:
                        recordManager.changeFormat(RecordConfig.RecordFormat.MP3);
                        break;
                    case R.id.rbWav:
                        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
                        break;
                    default:
                        break;
                }
            }
        });

        rgSimpleRate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb8K:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(8000));
                        break;
                    case R.id.rb16K:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
                        break;
                    case R.id.rb44K:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(44100));
                        break;
                    default:
                        break;
                }
            }
        });

        tbEncoding.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb8Bit:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT));
                        break;
                    case R.id.rb16Bit:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
                        break;
                    default:
                        break;
                }
            }
        });
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
}
