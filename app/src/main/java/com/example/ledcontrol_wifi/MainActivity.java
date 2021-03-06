package com.example.ledcontrol_wifi;

import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ColorPicker picker;
    SeekBar seekBar;
    ToggleButton toggleButton;
    Button sendButton;
    FloatingActionButton refreshButton;
    WebView webView;
    String serverAddress="192.168.100.21:80"; // 아두이노의 ip주소와 포트번호
    HttpRequestTask requestTask;
    int brightness=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("RGB LED WiFi Remote Controller");
        requestTask=new HttpRequestTask(serverAddress);

        // 조도를 WebView로 출력 (아두이노가 웹페이지에 조도 전송)
        webView=(WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());

        WebSettings webSet= webView.getSettings();
        webSet.setBuiltInZoomControls(true);
        webSet.setJavaScriptEnabled(true);
        webView.loadUrl("http://"+serverAddress);

        // On,Off 토글 버튼 클릭 이벤트
        toggleButton=(ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                String ledStatus;
                if(toggleButton.isChecked()){
                    Toast.makeText(MainActivity.this,"ON",Toast.LENGTH_SHORT).show();
                    ledStatus="On";
                }
                else{
                    Toast.makeText(MainActivity.this,"OFF",Toast.LENGTH_SHORT).show();
                    ledStatus="Off";
                }
                // 아두이노로 전송
                requestTask=new HttpRequestTask(serverAddress);
                requestTask.execute(ledStatus);
            }
        });

        picker=(ColorPicker) findViewById(R.id.picker);
        SaturationBar saturationBar=(SaturationBar) findViewById(R.id.saturationBar);
        picker.addSaturationBar(saturationBar);
        picker.setShowOldCenterColor(false);

        //led의 세기를 조절하는 시크바
        seekBar=(SeekBar)findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness=seekBar.getProgress(); //brightness : 선택한 밝기 (0~100)
            }

            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // SEND COLOR 버튼 클릭 이벤트
        sendButton=(Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor();
                Toast.makeText(MainActivity.this,"Send Color",Toast.LENGTH_SHORT).show();
            }
        });

        // 새로고침 버튼 클릭 이벤트
        refreshButton=(FloatingActionButton) findViewById(R.id.floatingButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Update Illuminance",Toast.LENGTH_SHORT).show();
                webView.loadUrl("http://"+serverAddress);
            }
        });

        sendColor(); //앱 시작 시 기본색(연두색)으로 초기화
    }

    class MyWebViewClient extends android.webkit.WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    // 선택한 색상의 RGB 값을 전송
    public void sendColor(){
        int color=picker.getColor(); //ColorPicker에서 선택한 색상 가져옴
        int r,g,b;

        r=Color.red(color);
        g=Color.green(color);
        b=Color.blue(color);

        //RGB000000000BRT000 형식의 문자열 전송
        String str="RGB"+String.format("%03d",r)+String.format("%03d",g)+String.format("%03d",b)
                +"BRT"+String.format("%03d",brightness);

        requestTask=new HttpRequestTask(serverAddress);
        requestTask.execute(str);
    }


    public class HttpRequestTask extends AsyncTask<String,Void,String> {
        private String serverAddress;
        public HttpRequestTask(String serverAddress){
            this.serverAddress=serverAddress;
        }

        @Override
        protected String doInBackground(String... params) {
            String val=params[0];
            final String url="http://"+serverAddress+"/"+val;

            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .url(url)
                    .build();

            try{
                Response response=client.newCall(request).execute();
                return null;
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }

        protected void onCancelled(){
            super.onCancelled();
        }
    }
}