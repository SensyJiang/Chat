package com.sensy.chat.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nononsenseapps.filepicker.*;
import com.sensy.chat.R;
import com.sensy.chat.adapter.MessageListAdapter;
import com.sensy.chat.bean.MessageBean;
import com.sensy.chat.helper.AES;
import com.sensy.chat.helper.TempHelper;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;

import static android.view.View.*;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private static final int IS_MESSAGE = 1;//标识：文本消息
    private static final int IS_TXT = 3;//标识：文本文件

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private Button btnPic;
    private Button btnTxt;
    private Socket socket;
    private ArrayList<MessageBean> messageList;
    private MessageListAdapter messageListAdapter;

    private Intent intent;
    private String serverIp;
    private int serverPort;
    private String name;

    private final String secretKey = AES.generateKey();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        rvMessages = (RecyclerView) findViewById(R.id.rv_messages);
        etMessage = (EditText) findViewById(R.id.et_message);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnPic = (Button) findViewById(R.id.btn_pic);
        btnTxt = (Button) findViewById(R.id.btn_txt);
        messageList = new ArrayList<>();
        messageListAdapter = new MessageListAdapter();



        intent = getIntent();
        serverIp = intent.getStringExtra("server_ip");
        serverPort = intent.getIntExtra("server_port", -1);
        name = intent.getStringExtra("name");

        final Handler handler = new MyHandler();

        //开启线程，建立socket连接，接收来自服务器的消息
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverIp, serverPort);//建立连接
                    //将读取的字节流准换为字符流，utf-8避免乱码
                    InputStreamReader inputStreamReader =
                            new InputStreamReader(socket.getInputStream(), "utf-8");
                    char[] buffer = new char[65535];
                    int len;
                    while ((len = inputStreamReader.read(buffer)) != -1) {
                        String data = new String(buffer, 0, len);
                        Message message = Message.obtain();
                        int flag = Integer.parseInt(new String(buffer, 0, 1));
                        message.what = flag;//标示
                        Log.d(TAG, "run: messageType: " + message.what);
                        message.obj = data;
                        Log.d(TAG, "run: data: " + message.obj);
                        handler.sendMessage(message);//添加到消息队列中
                    }
                    inputStreamReader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String data = etMessage.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream outputStream = socket.getOutputStream();
                            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
                            //数据发送到服务器端
                            outputStream.write((IS_MESSAGE + "//" + socket.getLocalPort() + "//" + data + "//"
                                    + name + "//" + df.format(new Date())).getBytes("utf-8"));
                            outputStream.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                //消息发送完后，消息框清空
                etMessage.setText("");
            }
        });

        btnTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChatActivity.this, FilePickerActivity.class);

                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, 3);
            }
        });

    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int localPort = socket.getLocalPort();
            Log.d(TAG, "handleMessage: data: " + msg.obj);
            String[] split = ((String) msg.obj).split("//");
            if (msg.what == IS_MESSAGE) {//收到一条文本消息
                if (split[1].equals(localPort + "")) {
                    MessageBean messageBean = new MessageBean(
                            Integer.parseInt(split[0]), split[2],
                            "我", split[4], 2);
                    messageList.add(messageBean);
                } else {
                    MessageBean messageBean = new MessageBean(
                            Integer.parseInt(split[0]), split[2],
                            "from: " + split[3], split[4], 1);
                    messageList.add(messageBean);
                }
            } else if (msg.what == IS_TXT) {//文本文件
                String fileStreamp = split[2];
                String[] t = fileStreamp.split("/");
                String decryptKey = t[0];
                String fileStream = t[1];
                Log.d(TAG, "handleMessage: fileStream: " + fileStream);
                //文件解密
                fileStream = AES.decrypt(decryptKey, fileStream);
                Log.d(TAG, "handleMessage: fileStream: " + fileStream);
                String[] split2 = fileStream.split("/");
                String fileName = split2[0];
                String content = split2[1];
                Log.d(TAG, "handleMessage: content: " + content);
                if (split[1].equals(localPort + "")) {
                    MessageBean messageBean = new MessageBean(Integer.parseInt(split[0]),
                            "发送文件：" + fileName, "我", split[4], 2);
                    messageList.add(messageBean);
                } else {
                    try {
                        String savePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
                        FileOutputStream file = new FileOutputStream(savePath);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(file);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream,
                                "utf-8");
                        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                        int size = -1;
                        bufferedWriter.write(content);
                        bufferedWriter.close();
                        outputStreamWriter.close();
                        bufferedOutputStream.flush();
                        file.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "handleMessage: name:" + split[3]);
                    MessageBean messageBean = new MessageBean(Integer.parseInt(split[0]),
                            "收到文件：" + fileName, "from: " + split[3], split[4], 1);
                    messageList.add(messageBean);
                }
            }

            // 向适配器set数据
            messageListAdapter.setData(messageList);
            rvMessages.setAdapter(messageListAdapter);
            LinearLayoutManager manager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false);
            rvMessages.setLayoutManager(manager);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {//处理文件
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            for (Uri uri: files) {
                final File file = Utils.getFileForUri(uri);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream outputStream = socket.getOutputStream();
                            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
                            String fileStream = file.getName() + "/";
                            FileInputStream fileInputStream = new FileInputStream(file);
                            int len = -1;
                            byte[] buffer = new byte[65535];
                            while((len = fileInputStream.read(buffer, 0, 65535)) != -1){
                                fileStream += new String(buffer, 0, len);
                            }

                            fileInputStream.close();
                            //数据加密
                            fileStream = AES.encrypt(secretKey, fileStream);
                            outputStream.write((IS_TXT + "//" + socket.getLocalPort()
                                    + "//" + secretKey + "/" + fileStream + "//" + name
                                    + "//" + df.format(new Date())).getBytes("utf-8"));
                            outputStream.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

}
