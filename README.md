# ZlwAudioRecorder
### 功能
1. 使用AudioRecord进行录音
2. 实现pcm音频的录制
3. 实现wav音频的录制 
4. 实现mp3音频的录制
5. 实时获取录音的音量
6. 获取wav/mp3录音文件的时长

### Gradle  
    
    dependencies {  
    	    implementation 'com.github.zhaolewei:ZlwAudioRecorder:1.0'
    	}

### 博客
https://www.jianshu.com/p/c0222de2faed

### 如何使用

1. 初始化
    ```java
    RecordManager.getInstance().init(MyApp.getInstance(), false);
   ```
2. 配置录音参数

* 修改录音格式
    ```java       
     RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.WAV);
    ```
* 录音状态监听
    ```java     
    RecordManager.getInstance().setRecordStateListener();
    ```
* 声音大小监听
    ```java
    RecordManager.getInstance().setRecordSoundSizeListener()
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

默认录音文件位置文件位置：sdcard/Record/

