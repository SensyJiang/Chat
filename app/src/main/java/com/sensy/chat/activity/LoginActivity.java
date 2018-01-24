package com.sensy.chat.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sensy.chat.R;
import com.sensy.chat.helper.TempHelper;

import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    private EditText etServerIp;
    private EditText etName;
    private Button btnEnter;
    private Socket socket;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etServerIp = (EditText) findViewById(R.id.et_server_ip);
        etName = (EditText) findViewById(R.id.et_name);
        btnEnter = (Button) findViewById(R.id.btn_enter);

        //点击“进入聊天室”按钮，连接服务器，进入聊天室
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //将服务器ip、昵称存入intent，传给下一个活动
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                Log.d(TAG, etServerIp.getText().toString());
                intent.putExtra("server_ip", etServerIp.getText().toString());
                intent.putExtra("server_port", TempHelper.serverPort);
                intent.putExtra("name", etName.getText().toString());
                startActivity(intent);
            }
        });
    }
}
