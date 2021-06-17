package com.example.airplanex2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.String.*;

public class MainActivity extends AppCompatActivity {
    private static final int CONNECTED_RESPONSE = 0;
    private static final int RESPONSE_TIMEOUT = 1;
    private static final int SEND_RESPONSE = 2;
    private static final int RECEIVER_RESPONSE = 3;
    private static final int WifiStateResponse = 4;
    private static final String HOST = "192.168.4.1";
    private static final int PORT = 5000;
    private int i1 = -1;
    private int i2 = -1;
    private int i3 = -1;
    private int i4 = -1;
    private int j1;
    private int j2;
    private int j3;
    private int j4;
    private static final String yaw = "yawww";
    private static final String throttl = "throt";
    private static final String roll = "rolll";
    private static final String pitch = "pitch";
    private static final String mode1 = "fmode293";
    private static final String mode2 = "fmode250";
    private static final String mode3 = "fmode333";
    private static final String seekBarLeft = "seekL";
    private static final String seekBarRight = "seekR";
    //private static final String flag_wifi = "flagg";

    private WifiManager mWifiManager;
    private Timer timer;


    TextView text_tips;
    Socket socket = null;
    Button bt_connect;
    Button bt_disconnect;
    RockerView rockerView_left;
    RockerView rockerView_right;
    TextView LogLeft;
    TextView LogRight;
    TextView textRssi;
    RadioGroup radioGroup;
    SeekBar seekBar_Left;
    SeekBar seekBar_Right;


    private final Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CONNECTED_RESPONSE:
                    bt_connect.setTextColor(Color.parseColor("#216F02"));
                    bt_connect.setText("已连接");
                    bt_connect.setClickable(false);
                    bt_disconnect.setTextColor(Color.BLACK);
                    bt_disconnect.setClickable(true);
                    break;
                case RESPONSE_TIMEOUT:
                    Toast.makeText(getApplicationContext(), "连接失败！", Toast.LENGTH_SHORT).show();
                case WifiStateResponse:

                     //textRssi.setText( String.valueOf(msg.arg1));
                    textRssi.setText(msg.arg1 + "dbm");



                case RECEIVER_RESPONSE:
                case SEND_RESPONSE:

                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textRssi = findViewById(R.id.textRssi);
        bt_connect = findViewById(R.id.button_connect);
        bt_disconnect = findViewById(R.id.button_disconnect);
        text_tips = findViewById(R.id.text_tips);
        rockerView_left = findViewById(R.id.rockerView_left);
        rockerView_right = findViewById(R.id.rockerView_right);
        LogLeft = findViewById(R.id.LogLeft);
        LogRight = findViewById(R.id.LogRight);
        radioGroup = findViewById(R.id.radioG);
        seekBar_Left = findViewById(R.id.seekBar1);
        seekBar_Right = findViewById(R.id.seekBar2);

        //Spannable sp = (Spannable) textRssi.getText();
        //sp.setSpan(new ForegroundColorSpan(Color.RED), 0 ,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mWifiManager == null)
                    return;
                WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
                //获得信号强度值
                int rssi = mWifiInfo.getRssi();
                Message message = new Message();
                message.what = WifiStateResponse;
                message.arg1 = rssi;
                ///Log.d("wwwwwww", valueOf(rssi));
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 0, 500);




        if (bt_disconnect != null) {
            bt_disconnect.setTextColor(Color.GRAY);
            bt_disconnect.setClickable(false);
        }

        bt_connect.setOnClickListener(v -> {
            Connect();
        });

        bt_disconnect.setOnClickListener(v -> {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
            bt_connect.setText("连接");
            bt_connect.setClickable(true);
            bt_disconnect.setTextColor(Color.GRAY);
            bt_disconnect.setClickable(false);
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton1) {
                TCPSend(mode1);
            }
            if (checkedId == R.id.radioButton2) {
                TCPSend(mode2);
            }
            if (checkedId == R.id.radioButton3) {
                TCPSend(mode3);
            }

        });
        seekBar_Left.setProgress(50);
        TCPSend(seekBarLeft + "293"); //293-50*3=143
            seekBar_Left.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TCPSend(seekBarLeft + (progress*3 + 143) );

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        seekBar_Right.setProgress(50);
        TCPSend(seekBarRight + "293"); //293-50*3=143
        seekBar_Right.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TCPSend(seekBarRight + (progress*3.2 + 143) );

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        if (rockerView_left != null) {
            rockerView_left.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            rockerView_left.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45, new RockerView.OnShakeListener() {
                @Override
                public void onStart() {
                    LogLeft.setText("多旋翼控制");
                }

                @Override
                public void direction(RockerView.Direction direction) {
                    String message = "";
                    switch (direction) {
                        case DIRECTION_LEFT:
                            message = "左旋转";
                            //TCPSend("yaww1");
                            break;
                        case DIRECTION_RIGHT:
                            message = "右旋转";
                            //TCPSend("yaww2");
                            break;
                        case DIRECTION_UP:
                            message = "微加油门";       /////radian反向了！
                            //TCPSend("throu");
                            break;
                        case DIRECTION_DOWN:
                            message = "大加油门";      /////radian反向了！
                            //TCPSend("throd");
                            break;
                        default:
                            break;
                    }
                    LogLeft.setText(message);
                }

                @Override
                public void onFinish() {
                    LogLeft.setText("回底");
                }
            });
            rockerView_left.setOnChannelChangeListener(new RockerView.OnChannelChangeListener() {

                @Override
                public void len_ver(int ReturnVer) {
                    ///竖直方向 532【68，600】， 原点，【竖直334】532 / 2.8 =190 最低：【68/2.8=24】，中间：【334/2.8=119】最高：【600/28 =214】     esp8266--293-119=174
                    ///最底198，最高388 198+388=586
                    //Log.d("channel_left_竖直", String.valueOf(ReturnVer));
                    //ReturnVer = (int)(ReturnVer/2.8 + 174);
                    //Log.d("channel_left_竖直__筛重后", String.valueOf(ReturnVer));

                    ReturnVer = (int) (ReturnVer / 3.2 + 189);
                    if(i1 == -1)
                    {j1 = ReturnVer;}
                    if (i1 != ReturnVer && Math.abs(j1- ReturnVer) < 50) {

                        i1 = ReturnVer;
                        j1 = ReturnVer;
                        Log.d("channel_Left_竖直__筛重后",throttl+ (586 - i1) );
                        TCPSend(throttl + (586 - i1));
                        //198--388[293]
                    }

                }

                @Override
                public void len_hor(int ReturnHor) {
                    ///水平方向 532【137，669】， 原点【水平403，】最低：【137/2.8=50】，中间：【403/2.8=144】，最高：【669/2.8=240】  esp8266--293-144=[149]
                    //Log.d("channel_left_水平", String.valueOf(ReturnHor));
                    //ReturnHor = (int)(ReturnHor/2.8 + 149);
                    //Log.d("channel_left_水平__筛重后", String.valueOf(ReturnHor));
                    ReturnHor = (int) (ReturnHor / 3.2 + 168);
                    if(i2 == -1)
                    {j2 = ReturnHor;}
                    if (i2 != ReturnHor && Math.abs(j2- ReturnHor) < 50) {
                        i2 = ReturnHor;
                        j2 = ReturnHor;
                        //if(i2<288 || i2>298) {
                        Log.d("channel_Left_水平__筛重后", String.valueOf(i2));
                        //    TCPSend(yaw + i2);
                        //}
                        TCPSend(yaw + i2);
                        //197--387---[293]
                    }
                }

                @Override
                public void onFinish(Point point) {
                    //Log.d("channel_Left_竖直_/", String.valueOf( (int) ((point.y)/2.8+174)) );
                    //Log.d("channel_Left_水平__/", String.valueOf( (int) ((point.x)/3.2+168)) );
                    TCPSend(throttl + (int) (586 - (point.y) / 3.2 + 189));
                    TCPSend(yaw + (int) ((point.x) / 3.2 + 168));


                }
            });
        }

        if (rockerView_right != null) {
            rockerView_right.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            rockerView_right.setOnShakeListener(RockerView.DirectionMode.DIRECTION_4_ROTATE_45, new RockerView.OnShakeListener() {
                @Override
                public void onStart() {
                    LogRight.setText("多旋翼控制");
                }

                @Override
                public void direction(RockerView.Direction direction) {
                    String message = "";
                    switch (direction) {
                        case DIRECTION_LEFT:
                            message = "左翻滚";
                            break;
                        case DIRECTION_RIGHT:
                            message = "右翻滚";
                            break;
                        case DIRECTION_UP:
                            message = "后退"; /////radian反向了！
                            break;
                        case DIRECTION_DOWN:
                            message = "前进";
                            break;
                        default:
                            break;
                    }
                    LogRight.setText(message);
                }

                @Override
                public void onFinish() {
                    LogRight.setText("回中");
                }
            });
            rockerView_right.setOnChannelChangeListener(new RockerView.OnChannelChangeListener() {

                @Override
                public void len_ver(int ReturnVer) {
                    //竖直方向 532【68，600】
                    //ReturnVer = (int) (ReturnVer / 2.8 + 174);
                    //if (i3 != ReturnVer) {
                    //    i3 = ReturnVer;
                        //Log.d("channel_Right_竖直__筛重后", String.valueOf(i3));
                    //    TCPSend(pitch + i3);
                        //198--388[293]
                    //}
                    ///中间334/3.2 = 104.4，293-104.4 = 188.6=189，
                    ReturnVer = (int) (ReturnVer / 3.2 + 189);
                    if(i3 == -1)
                    {j3 = ReturnVer;}
                    if (i3 != ReturnVer && Math.abs(j3- ReturnVer) < 50) {
                        i3 = ReturnVer;
                        Log.d("_Right_竖直_", String.valueOf(Math.abs(j3- ReturnVer)));
                        j3 = ReturnVer;
                        Log.d("channel_Right_竖直__筛重后", String.valueOf(i3));

                        TCPSend(pitch + i3);
                    }



                }

                @Override
                public void len_hor(int ReturnHor) {
                    //水平方向 532【137，669】
                    //ReturnHor = (int) (ReturnHor / 2.8 + 149);
                    //if (i4 != ReturnHor) {
                    //    i4 = ReturnHor;
                    //Log.d("channel_Right_水平__筛重后", String.valueOf(i4));
                    //    TCPSend(roll + i4);
                    //198--388[293]
                    //}
                    ///403/3.2=126,293-126=~168
                    ReturnHor = (int) (ReturnHor / 3.2 + 168);
                    if(i4 == -1)
                    {j4 = ReturnHor;}
                    if (i4 != ReturnHor && Math.abs(j4- ReturnHor) < 50) {
                        i4 = ReturnHor;
                        Log.d("_Right_水平_", String.valueOf(Math.abs(j4- ReturnHor)));
                        j4 = ReturnHor;

                        Log.d("channel_Right_水平__筛重后", String.valueOf(i4));

                        TCPSend(roll + i4);
                    }


                }

                @Override
                public void onFinish(Point point) {
                    //Log.d("channel_Right_竖直_/", String.valueOf( (int) ((point.y)/3.2+189)) );
                    //Log.d("channel_Right_水平__/", String.valueOf( (int) ((point.x)/3.2+168)) );
                    TCPSend(pitch + (int) ((point.y) / 3.2 + 189));
                    TCPSend(roll + (int) ((point.x) / 3.2 + 168));
                }
            });
        }


    }

    protected void onDestroy() {

        super.onDestroy();
        timer.cancel();
    }

    private void Connect() {
        // 开启线程来发起网络请求
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(HOST, PORT), 3000);
                if (socket != null) {
                    Message message = new Message();
                    message.what = CONNECTED_RESPONSE;
                    handler.sendMessage(message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Message message = new Message();
                message.what = RESPONSE_TIMEOUT;
                handler.sendMessage(message);
            }
        }).start();
    }


    private void TCPSend(final String data) {
        if (socket == null)
            return;
        // 开启线程来发起网络请求
        new Thread(() -> {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.println(data);
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
                Message message = new Message();
                message.what = SEND_RESPONSE;
                // 将服务器返回的结果存放到Message中
                message.obj = "操作失败！";
                handler.sendMessage(message);
            }
        }).start();
    }

}