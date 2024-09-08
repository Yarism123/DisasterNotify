package com.example.disasternotifyapp.blueToothTest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.disasternotifyapp.PermissionCheck;
import com.example.disasternotifyapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BlueToothActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName(); // log
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 1;
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치
    int pariedDeviceCount;
    private ActivityResultLauncher<Intent> enableBtLauncher;
    private EditText inputText; // 입력할 텍스트
    private Button sendButton; // 보내기 버튼
    private TextView receivedText; // 수신된 텍스트 표시

    private PermissionCheck permissionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth2);
        Log.d(TAG, "BlueToothActivity2가 생성되었습니다.");

//        // 권한 요청 및 블루투스 설정
//        permissionCheck = new PermissionCheck(this);
//        permissionCheck.requestPermissions();

        // UI 요소 초기화
        inputText = findViewById(R.id.inputText);
        sendButton = findViewById(R.id.sendButton);
        receivedText = findViewById(R.id.receivedText);

        // 버튼 클릭 리스너 설정
        sendButton.setOnClickListener(v -> sendData());

        // 블루투스 활성화 요청
        enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        selectBluetoothDevice();
                    } else {
                        Toast.makeText(getApplicationContext(), "블루투스가 활성화되지 않았습니다.", Toast.LENGTH_LONG).show();
                    }
                }
        );
        SetBluetooth();

    }

    @SuppressLint("MissingPermission")
    private void SetBluetooth() {
        Log.d(TAG, "SetBluetooth()");
        // 블루투스 활성화하기
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
        if (bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
            Toast.makeText(getApplicationContext(), "블루투스 미지원 기기입니다.", Toast.LENGTH_LONG).show();
        } else if (!bluetoothAdapter.isEnabled()) { // 블루투스가 비활성화 상태일 때
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(intent);
        } else {
            // 블루투스가 이미 활성화된 경우
            selectBluetoothDevice();
        }
    }

    @SuppressLint("MissingPermission")
    public void selectBluetoothDevice() {
        Log.d(TAG, "selectBluetoothDevice()");
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.

        try{
            devices = bluetoothAdapter.getBondedDevices();

            // 페어링 된 디바이스의 크기를 저장
            pariedDeviceCount = devices.size();

            // 페어링 되어있는 장치가 없는 경우
            if (pariedDeviceCount == 0) {
                // 페어링을 하기위한 함수 호출
                Toast.makeText(getApplicationContext(), "먼저 Bluetooth 설정에 들어가 페어링 해주세요", Toast.LENGTH_SHORT).show();
            }

            // 페어링 되어있는 장치가 있는 경우
            else {
                // 디바이스를 선택하기 위한 다이얼로그 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
                // 페어링 된 각각의 디바이스의 이름과 주소를 저장
                List<String> list = new ArrayList<>();

                // 모든 디바이스의 이름을 리스트에 추가
                for (BluetoothDevice bluetoothDevice : devices) {
                    list.add(bluetoothDevice.getName());
                }

                list.add("취소");


                // List를 CharSequence 배열로 변경
                final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);

                list.toArray(new CharSequence[list.size()]);

                // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너

                builder.setItems(charSequences, (dialog, which) -> {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                });

                // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
                builder.setCancelable(false);

                // 다이얼로그 생성
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }

        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

    }

    @SuppressLint("MissingPermission")
    public void connectDevice(String deviceName) {
        Log.d(TAG, "connectDevice()");
        // 페어링 된 디바이스들을 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }

        Toast.makeText(this, bluetoothDevice.getName() + " 연결 완료", Toast.LENGTH_SHORT).show();

        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            // 연결된 블루투스 이름을 TextView에 설정
            TextView bluetoothNameText = findViewById(R.id.bluetoothNameText);
            bluetoothNameText.setText("연결된 블루투스: " + bluetoothDevice.getName());

            // 데이터 수신 함수 호출
            receiveData();

        } catch (IOException e) {
            Log.e(TAG, "블루투스 연결 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void receiveData() {
        Log.d(TAG, "receiveData()");
        final Handler handler = new Handler();
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                try {
                    int byteAvailable = inputStream.available();
                    if (byteAvailable > 0) {
                        byte[] bytes = new byte[byteAvailable];
                        inputStream.read(bytes);

                        for (int i = 0; i < byteAvailable; i++) {
                            byte tempByte = bytes[i];
                            if (tempByte == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                // 인코딩을 UTF-8로 변경
                                final String text = new String(encodedBytes, "UTF-8");
                                readBufferPosition = 0;
                                handler.post(() -> {
                                    Log.d(TAG, "run: text =" + text);
                                    // 수신된 데이터 표시
                                    receivedText.setText(text);
                                });
                            } else {
                                readBuffer[readBufferPosition++] = tempByte;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        workerThread.start();
    }


    private void sendData() {
        Log.d(TAG, "sendData()");
        String message = inputText.getText().toString();
        if (outputStream != null && !message.isEmpty()) {
            try {
                outputStream.write((message + "\n").getBytes()); // 메시지 전송
                inputText.setText(""); // 입력창 초기화
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        permissionCheck.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
}