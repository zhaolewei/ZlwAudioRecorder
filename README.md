# ZlwAudioRecorder

### 功能
1. 使用AudioRecord进行录音
2. 实现pcm、wav、mp3音频的录制
3. 实时获取录音的音量、及录音byte数据
4. 获取wav/mp3录音文件的时长
5. 可配置录音的采样率、位宽  （v1.04更新）
5. 录音可视化 （v1.05更新）

### 博客
https://www.jianshu.com/p/c0222de2faed

### Gradle
[![](https://jitpack.io/v/zhaolewei/ZlwAudioRecorder.svg)](https://jitpack.io/#zhaolewei/ZlwAudioRecorder)

    dependencies {
	        implementation 'com.github.zhaolewei:ZlwAudioRecorder:v1.08'
	}

    allprojects {
        repositories {
            ...
            maven { url 'https://www.jitpack.io' }
        }
    }
### 如何使用

1. 初始化
* init
    ```java
    /**
    * 参数1： Application 实例
    * 参数2： 是否打印日志   
    */
    RecordManager.getInstance().init(MyApp.getInstance(), false);
   ```
* 在清单文件中注册Services   
   
    ```java
   <service android:name="com.zlw.main.recorderlib.recorder.RecordService" />
   ```
 * 确保有录音权限
   
2. 配置录音参数

* 修改录音格式(默认:WAV)
    ```java       
     RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.WAV);
    ```

* 修改录音配置
    ```java       
         RecordManager.getInstance().changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
         RecordManager.getInstance().changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT));
    ```
* 修改录音文件存放位置（默认sdcard/Record）
    ```java       
       RecordManager.getInstance().changeRecordDir(recordDir);
    ```
* 录音状态监听
    ```java     
   RecordManager.getInstance().setRecordStateListener(new RecordStateListener() {
       @Override
       public void onStateChange(RecordHelper.RecordState state) {
           }
       }

       @Override
       public void onError(String error) {
       }
   });
    ```
* 录音结果监听 
    ```java     
   RecordManager.getInstance().setRecordResultListener(new RecordResultListener() {
       @Override
       public void onResult(File result) {
       }
    });
    ```
* 声音大小监听
    ```java
     RecordManager.getInstance().setRecordSoundSizeListener(new RecordSoundSizeListener() {
        @Override
        public void onSoundSize(int soundSize) {
        }
    });
    ```
* 音频数据监听
    ```java
      recordManager.setRecordDataListener(new RecordDataListener() {
         @Override
         public void onData(byte[] data) {
         }
     });
    ```
* 音频可视化数据监听
    ```java
       recordManager.setRecordFftDataListener(new RecordFftDataListener() {
          @Override
          public void onFftData(byte[] data) {
              audioView.setWaveData(data);
          }
        });
    ```
3. 录音控制
* 开始录音
    ```java
    RecordManager.getInstance().start();
    ```  
* 暂停录音
    ```java
    RecordManager.getInstance().pasue();
    ```  
* 恢复录音
    ```java
    RecordManager.getInstance().resume();
    ```  
* 停止
    ```java
    RecordManager.getInstance().stop();
    ```    

### Demo
![Demo.png](https://raw.githubusercontent.com/zhaolewei/ZlwAudioRecorder/master/doc/demo.jpg)
* 演示视频>>> https://www.bilibili.com/video/av48748708?from=search&seid=7409882966117066343


