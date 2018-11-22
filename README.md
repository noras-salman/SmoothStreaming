# SmoothStreaming

### MicrophoneStreamer

In your manifest
```
<uses-permission android:name="android.permission.RECORD_AUDIO"></uses-permission>
```
In your Activity

```
public class MainActivity extends AppCompatActivity {
    MicrophoneStreamer microphoneStreamer;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button button=findViewById(R.id.record);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {



                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    if(hasPermission())
                        microphoneStreamer.startRecording();
                    else
                        requestPermission();

                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    if(hasPermission())
                        microphoneStreamer.stopRecording();
                }

                return false;
            }
        });
        if(hasPermission()) {
            prepareStreamer();
        }

    }
    
    public void prepareStreamer(){
        microphoneStreamer =new MicrophoneStreamer();
        microphoneStreamer.startReceiver();
        microphoneStreamer.prepareRecorder();
        microphoneStreamer.getReceiverBufferSize(); //setBufferSizeOnClient
        microphoneStreamer.setRawDataListener(new MicrophoneStreamer.RawDataListener() {
            @Override
            public void onRawDataCapture(byte[] audioData) {
                //send byte array on udp
                udpGroupCommunication.send(audioData);
            }
        });
        
        //WHEN YOU RECIVE THE DATA byte[]
        // microphoneStreamer.playReceiverAudioTrack(audioData);
    }

    @Override
    protected void onDestroy() {
        if(microphoneStreamer!=null){
            microphoneStreamer.releaseRecorder();
            microphoneStreamer.stopReceiver();
        }
        super.onDestroy();
    }
    
    /* PERMISSION STUFF: "fix you own handlers" */
    
    public boolean hasPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }
    
    
    public void requestPermission(){
        // Here, thisActivity is the current activity
        if (!hasPermission()) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        0x01);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0x01: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    prepareStreamer();
                    prepareSocket();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }    
}
```