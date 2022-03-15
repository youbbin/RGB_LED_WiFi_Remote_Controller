package com.example.ledcontrol_wifi;

import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ColorPicker picker;
    ToggleButton toggleButton;
    Button sendButton;
    String serverAddress="192.168.100.21:80"; // 아두이노의 ip주소와 포트번호


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("RGB LED WiFi Remote Controller");

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
                HttpRequestTask requestTask=new HttpRequestTask(serverAddress);
                requestTask.execute(ledStatus);
            }
        });

        picker=(ColorPicker) findViewById(R.id.picker);
        SaturationBar saturationBar=(SaturationBar) findViewById(R.id.saturationBar);
        picker.addSaturationBar(saturationBar);
        picker.setShowOldCenterColor(false);
        //picker.getColor(); //스크롤을 돌려서 색상값을 가져옴

        // 전송 버튼 클릭 이벤트
        sendButton=(Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor();
                Toast.makeText(MainActivity.this,"Send Color",Toast.LENGTH_SHORT).show();
            }
        });

        sendColor(); //앱 시작 시 기본색(연두색)으로 초기화
    }

    // 선택한 색상의 RGB 값을 전송
    public void sendColor(){
        int color=picker.getColor();
        int r,g,b;

        r=Color.red(color);
        g=Color.green(color);
        b=Color.blue(color);

        //RGB000000000 형식의 문자열 전송
        String rgb="RGB"+String.format("%03d",r)+String.format("%03d",g)+String.format("%03d",b);

        HttpRequestTask requestTask=new HttpRequestTask(serverAddress);
        requestTask.execute(rgb);
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