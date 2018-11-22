
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import java.net.DatagramPacket;


public class MicrophoneStreamer {
    private static int[] sampleRates = new int[]{8000, 11025, 22050, 44100};

    private int SAMPLE_RATE = 8000;

    private int senderBufferSize;

    private AudioRecord senderAudioRecord;

    private int ENCODING_FORMAT = AudioFormat.ENCODING_PCM_8BIT;

    private boolean isRecording = false;

    private int receiverBufferSize;

    private AudioTrack receiverAudioTrack;

    private RawDataListener rawDataListener;

    private PacketDataListener packetDataListener;


    private void startRecordingT() {
        isRecording = true;

        byte[] buffer = new byte[senderBufferSize];
        senderAudioRecord.startRecording();
        while (isRecording) {
            senderAudioRecord.read(buffer, 0, senderBufferSize);
            if (rawDataListener != null)
                rawDataListener.onRawDataCapture(buffer);

            DatagramPacket dPacket = new DatagramPacket(buffer, senderBufferSize);
            if (packetDataListener != null)
                packetDataListener.onRawDataSend(dPacket);

        }
    }


    private class RecordTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            startRecordingT();
            return null;
        }
    }

    public interface RawDataListener {
        public void onRawDataCapture(byte[] audioData);
    }

    public int getReceiverBufferSize() {
        return receiverBufferSize;
    }

    public interface PacketDataListener {
        public void onRawDataSend(DatagramPacket audioDataPacket);
    }

    public void setRawDataListener(RawDataListener rawDataListener) {
        this.rawDataListener = rawDataListener;
    }

    public void setPacketDataListener(PacketDataListener packetDataListener) {
        this.packetDataListener = packetDataListener;
    }

    public void setENCODING_FORMAT(int ENCODING_FORMAT) {
        this.ENCODING_FORMAT = ENCODING_FORMAT;
    }

    public void prepareRecorder() {
        senderBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                ENCODING_FORMAT);

        senderAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                ENCODING_FORMAT, senderBufferSize);
    }


    public void startRecording() {
         new RecordTask().execute();

    }


    public void stopRecording() {
        isRecording = false;
    }

    public void releaseRecorder() {
        senderAudioRecord.release();
    }


    public void startReceiver() {
        receiverBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_8BIT);

        receiverAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT, receiverBufferSize, AudioTrack.MODE_STREAM);

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        receiverAudioTrack.play();
    }

    public void stopReceiver() {
        receiverAudioTrack.release();
    }

    public void playReceiverAudioTrack(DatagramPacket dPacket) {
        byte[] buffer = new byte[receiverBufferSize];
        buffer = dPacket.getData();
        receiverAudioTrack.setPlaybackRate(SAMPLE_RATE);
        receiverAudioTrack.write(buffer, 0, buffer.length);
    }

    public void playReceiverAudioTrack(byte[] buffer) {
        receiverAudioTrack.setPlaybackRate(SAMPLE_RATE);
        receiverAudioTrack.write(buffer, 0, buffer.length);
    }


    int streamCount=0;
    byte[] streamBuffer;
    int bufferPacketCount=24;
    public void playReceiverAudioTrackBuffered(byte[] buffer) {
        streamCount++;
        if(streamCount==1 ){
            streamBuffer=new byte[receiverBufferSize*bufferPacketCount];
            System.arraycopy(buffer, 0, streamBuffer, 0 , receiverBufferSize);
        }else{
            System.arraycopy(buffer, 0, streamBuffer, ((streamCount-1)*receiverBufferSize)-1 , receiverBufferSize);
        }

        if(streamCount==bufferPacketCount) {
            receiverAudioTrack.setPlaybackRate(SAMPLE_RATE);
            receiverAudioTrack.write(streamBuffer, 0, streamBuffer.length);
            streamCount=0;
        }
    }
}
