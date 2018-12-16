package com.drawthink.websokect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.WebSocket;

import rx.Subscriber;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private TextView serverMessage;
    private Button start;
    private Button stop;
    private Button send;
    private EditText editText;
    private StompClient mStompClient;
    private Button cheat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStompClient();
                registerStompTopic();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStompClient.send("/app/welcome","{\"name\":\""+editText.getText()+"\"}")
                        .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        toast("发送成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        toast("发送错误");
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStompClient.disconnect();
            }
        });

        cheat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CheatActivity.class));
                if(mStompClient != null) {
                    mStompClient.disconnect();
                }
                finish();
            }
        });
    }

    private void showMessage(final StompMessage stompMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverMessage.setText("stomp command is --->"+stompMessage.getStompCommand() +" body is --->"+stompMessage.getPayload());
            }
        });
    }

    private void createStompClient() {
        mStompClient = Stomp.over(WebSocket.class, "ws://192.168.2.197:8080/hello/websocket");
        mStompClient.connect();
        Toast.makeText(MainActivity.this,"开始连接 92.168.2.197:8080",Toast.LENGTH_SHORT).show();
        mStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "Stomp connection opened");
                        toast("连接已开启");
                        break;

                    case ERROR:
                        Log.e(TAG, "Stomp Error", lifecycleEvent.getException());
                        toast("连接出错");
                        break;
                    case CLOSED:
                        Log.d(TAG, "Stomp connection closed");
                        toast("连接关闭");
                        break;
                }
            }
        });
    }

    private void registerStompTopic() {
        mStompClient.topic("/topic/getResponse").subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage stompMessage) {
                Log.e(TAG, "call: " +stompMessage.getPayload() );
                showMessage(stompMessage);
            }
        });

    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindView() {
        serverMessage = (TextView) findViewById(R.id.serverMessage);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        send = (Button) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.clientMessage);
        cheat = (Button) findViewById(R.id.cheat);
    }
}
