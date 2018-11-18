package edu.dongyang.cs.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private ArrayList<Map<String, String>> dataList;
    private ListView mListview;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private static final int MY_PERMISSION_STORAGE = 1111;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    itemAdapter itemAdapter;
    String name;
    String number;
    CheckBox check;

    TextView send;
    TextView location;
    TextView message;
    Button gpsfind;
    final String[] sendt = new String[30];
    DBHelper mHelper;
    SQLiteDatabase db;
    Cursor cursor;

    Geocoder mGeoCoder;
    private GpsInfo gps;


    String BluetoothCommand=null;
    private static final int CONNECTED = 1;
    private static final int DISCONNECTED = 2;
    private static final int MESSAGE_READ = 3;
    private static final int MESSAGE_WRITE = 4;

    private static final int REQUEST_CONNECT_DEVICE = 1;

    private ArrayAdapter<String> messageAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChat chat;
    private Button buttonConnect;


    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent it = new Intent(this, LoadingActivity.class);
        startActivity(it);

        checkPermission();

        mHelper = new DBHelper(this);
        db = mHelper.getWritableDatabase();
        cursor = db.rawQuery("SELECT phone FROM contacta", null);

        final DBHelper dbHelper2 = new DBHelper(getApplicationContext());
        itemAdapter = new itemAdapter();
        mListview = findViewById(R.id.listview);
        check = findViewById(R.id.check);
        send = findViewById(R.id.send);
        location = findViewById(R.id.location);
        message = findViewById(R.id.message);
        gpsfind = findViewById(R.id.gpsfind);
        mHelper = new DBHelper(this);
        db = mHelper.getWritableDatabase();

        String ResultNumber = dbHelper2.getResult();
        String[] list = ResultNumber.split(";");
        int i = 0;
        for (String wo : list) {
            sendt[i] = wo;
            i++;
        }
      /*  AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());

        alertDialog.setTitle("어플 사용 안내");
        alertDialog.setMessage("이 어플은 WithYou 아두이노 기기와 같이 사용하는 어플입니다. \n" +
                "이미 구매 하신 분들은 확인 버튼을 눌러주세요. \n" +
                "구매를 원하시는 분은 이동버튼을 눌러주세요");

        alertDialog.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 2000);
                    }
                });
        alertDialog.setNegativeButton("이동",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.naver.com"));
                        startActivity(intent);
                    }
                });
        alertDialog.show();*/
        dataList = new ArrayList<Map<String, String>>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc");

            while (c.moveToNext()) {
                HashMap<String, String> map = new HashMap<String, String>();
                // 연락처 id 값
                String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                // 연락처 대표 이름
                name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                //map.put("name", name);

                String result = "";

                // ID로 전화 정보 조회
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null, null);

                // 데이터가 있는 경우
                if (phoneCursor.moveToFirst()) {
                    number = phoneCursor.getString(phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "");
                }
          /*  if(cursor.moveToNext()){
                if(cursor.getString(1).equals(number)){
                    itemAdapter.addItem(name, number, true);
                }
            }else{
                itemAdapter.addItem(name, number, false);
            }*/
                itemAdapter.addItem(name, number, false);
                mListview.setAdapter(itemAdapter);
                phoneCursor.close();
            }// end while

            c.close();
        }
        gpsfind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // 권한 요청을 해야 함
                if (!isPermission) {
                    callPermission();
                    return;
                } else {
                    gps = new GpsInfo(MainActivity.this);
                    // GPS 사용유무 가져오기
                    if (gps.isGetLocation()) {

                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        mGeoCoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> mResultList = mGeoCoder.getFromLocation(
                                    latitude, longitude, 1
                            );
                            Log.d("TAG", mResultList.get(0).getAddressLine(0));
                            location.setText("현재위치 : " + mResultList.get(0).getAddressLine(0));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("TAG", "주소변환실패");
                        }

                    } else {
                        // GPS 를 사용할수 없으므로
                        gps.showSettingsAlert();
                    }
                }
                callPermission();  // 권한 요청을 해야 함
            }
        });







        if ((bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Toast.makeText(MainActivity.this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
        }
        // ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission is required for Bluetooth from Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled() == false) {
                    // Request to enable bluetooth
                    startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                    return;
                }
                if (chat == null) {
                    // Launch DeviceListActivity to search bluetooth device
                    startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
                } else {
                    chat.close();
                }
            }
        });

        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        ListView messageView = (ListView) findViewById(R.id.message_view);
        messageView.setAdapter(messageAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (chat != null) {
            chat.close();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // When DeviceListActivity returns with a device to connect
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    BluetoothSocket socket;

                    try {
                        socket = device.createRfcommSocketToServiceRecord(uuid);
                    } catch (IOException e) {
                        break;
                    }
                    chat = new BluetoothChat(socket, handler);
                    chat.start();
                }
                break;
        }

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            // 다시 보지 않기 버튼을 만드려면 이 부분에 바로 요청을 하도록 하면 됨 (아래 else{..} 부분 제거)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET,}, MY_PERMISSION_STORAGE);

            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        }
    }

 /*   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_STORAGE:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(MainActivity.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }

        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
        if (requestCode == 0) {
            if (grantResults[0] == 0) {
                Intent it = new Intent(this, LoadingActivity.class);
                startActivity(it);
            } else {
                finish();
            }
        }
    }
*/

    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECTED:
                    buttonConnect.setText("연결 끊기");
                    break;
                case DISCONNECTED:
                    buttonConnect.setText("키링 연결하기");
                    chat = null;
                    break;
                case MESSAGE_READ:
                    try {
                        //스위치 눌리면 여기 케이스문이 실행댐 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        // Encoding with "EUC-KR" to read 한글
                        messageAdapter.add("< " + new String((byte[]) msg.obj, 0, msg.arg1, "EUC-KR"));
                        //이건 원래소스 메세지 주고받는건데 거기에서 출력할때 쓰는거임 아래에서 값출력할때 쓰여서 빼면안댐
                        BluetoothCommand=(String)messageAdapter.getItem(messageAdapter.getCount()-1);
                        //BluetoothCommand 원래 값은 null인데 버튼 눌리면 start라는 값이 들어감
                        Toast.makeText(MainActivity.this, BluetoothCommand, Toast.LENGTH_SHORT).show();
                        //이건 들어가나 실험하려고 넣은거임 지워도댐

                        if(!BluetoothCommand.equals(null)){
                            StringBuffer responseText = new StringBuffer();
                            responseText.append("체크된 연락처 : \n");

                            ArrayList<ItemList> itemLists = itemAdapter.itemLists;
                            for (int i = 0; i < itemLists.size(); i++) {
                                ItemList itemList = itemLists.get(i);
                                if (itemList.isSelected()) {
                                    responseText.append("\n" + itemList.getPhone());
                                    Toast.makeText(MainActivity.this,itemList.getPhone(),Toast.LENGTH_SHORT);
                                }
                            }

                            String sms = location.getText().toString() + "\n" + message.getText().toString();
                            try {
                                SmsManager smsManager = SmsManager.getDefault();
                                for (int j = 0; j < sendt.length; j++) {
                                    Toast.makeText(MainActivity.this, sendt[j], Toast.LENGTH_SHORT).show();
                                    smsManager.sendTextMessage(sendt[j], null, sms, null, null);
                                }
                                //Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                //Toast.makeText(getApplicationContext(), "전송실패", Toast.LENGTH_SHORT).show();
                                Log.d("sms", e.getMessage());
                                e.printStackTrace();
                            }
                        }


                    } catch (UnsupportedEncodingException e) {
                    }
                    break;
                case MESSAGE_WRITE:
                    messageAdapter.add("> " + new String((byte[]) msg.obj));
                    break;
            }
        }
    };

    // This class connect with a bluetooth device and perform data transmissions when connected.
    private class BluetoothChat extends Thread {
        private BluetoothSocket socket;
        private Handler handler;
        private InputStream inputStream;
        private OutputStream outputStream;

        public BluetoothChat(BluetoothSocket socket, Handler handler) {
            this.socket = socket;
            this.handler = handler;
        }

        public void run() {
            try {
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (Exception e) {
                close();
                return;
            }
            handler.obtainMessage(CONNECTED, -1, -1).sendToTarget();

            while (true) {
                try {
                    byte buffer[] = new byte[1024];

                    int bytes = 0;

                    // Read single byte until '\0' is found
                    for (; (buffer[bytes] = (byte) inputStream.read()) != '\0'; bytes++) ;
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    close();
                    break;
                }
            }
        }

        public void close() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                handler.obtainMessage(DISCONNECTED, -1, -1).sendToTarget();
            }
        }

        public void send(byte[] buffer) {
            try {
                outputStream.write(buffer);
                outputStream.write('\n');
                handler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                close();
            }
        }
    }
}

