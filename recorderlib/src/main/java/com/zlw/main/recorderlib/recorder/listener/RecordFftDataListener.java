package com.zlw.main.recorderlib.recorder.listener;

/**
 * @author zhaolewei on 2019/3/11.
 */
public interface RecordFftDataListener {

    /**
     * @param data 录音可视化数据，即傅里叶转换后的数据：fftData
     */
    void onFftData(byte[] data);

}
