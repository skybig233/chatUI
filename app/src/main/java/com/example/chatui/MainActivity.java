package com.example.chatui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatui.adapter.MessageAdapter;
import com.example.chatui.util.MessageInfoConstant;
import com.example.chatui.util.TimeUtil;
import com.example.chatui.util.ZipUtil;
import com.iceteck.silicompressorr.SiliCompressor;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


import static com.example.chatui.util.MessageInfoConstant.CHOOSE_PHOTO;
import static com.example.chatui.util.MessageInfoConstant.TAKE_PHOTO;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "imagepath";
    private TextView userName;//点击toolbar上的username会触发事件
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private EditText inputText;
    private Button sendTextButton;
    private ImageView voiceIcon;
    private ImageView imageIcon;
    private ImageView photoIcon;
    private ImageView messageImage;
    private Uri imageUri;
    private TextView messageReceiveTime;
    private TextView MessageSendTime;
    private ImageView userIconLeft;//点击用户图标触发事件
    private ImageView userIconRight;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
////            透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
////            透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        通过toolbar去掉title，
//        改AndroidManifest中的label会导致程序应用名为空（app进入分栏看不到app名字）
        toolbar.setTitle("");
        userName = (TextView) findViewById(R.id.chat_title);
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                这里的context不能直接用this
                Toast.makeText(getBaseContext(), "title", Toast.LENGTH_SHORT).show();
            }
        });
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_chevron_left_black_24dp);
        }

        messageRecyclerView = (RecyclerView) findViewById(R.id.message_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(messageList);
        messageRecyclerView.setAdapter(messageAdapter);

//实现了退出程序之前，编辑框有内容就缓存的功能
// 如果之前有草稿则加载
        inputText = (EditText) findViewById(R.id.reply_layout_input_text);
        final String inputTextCache = load("inputTextCache");
        if (!TextUtils.isEmpty(inputTextCache)) {
            inputText.setText(inputTextCache);
            inputText.setSelection(inputText.length());
        }

//        发送文字功能
        sendTextButton = (Button) findViewById(R.id.reply_layout_send_button);
        sendTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Message message = new Message();
                    message.setContent(content);
                    message.setMessageType(MessageInfoConstant.Message_Type_Send);
                    message.setMessageContentType(MessageInfoConstant.Message_Content_Text);
                    message.setMessageTime(new TimeUtil().getCurrentTime());
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                    inputText.setText("");
                }
            }
        });

        //拍照功能
        photoIcon = (ImageView) findViewById(R.id.reply_layout_photo_icon);
        photoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Message message=new Message();
//                message.setMessageType(MessageInfoConstant.Message_Type_Send);
//                message.setMessageTime(new TimeUtil().getCurrentTime());
                TimeUtil t = new TimeUtil();
                String s = t.getCurrentTime();
                File outputImage = new File(getExternalCacheDir(), "output_image" + t.getCurrentTime() + ".jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.chatui.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

        voiceIcon = (ImageView) findViewById(R.id.reply_layout_voice_icon);
        voiceIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "voice", Toast.LENGTH_SHORT).show();
            }
        });

//        选择照片功能
        imageIcon = (ImageView) findViewById(R.id.reply_layout_image_icon);
        imageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
                    //requestCode为1是否是hardcode？
                } else {
                    openAlbum();
                }
            }
        });

        initMessage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Message message = new Message();
                    message.setMessageType(MessageInfoConstant.Message_Type_Send);
                    message.setMessageContentType(MessageInfoConstant.Message_Content_Image);
                    message.setMessageTime(new TimeUtil().getCurrentTime());
//                    String s=getImagePath(imageUri,null);
//                    Log.d(TAG, "onActivityResult: "+s);
                    Log.d(TAG, "onActivityResult: "+imageUri);
                    Log.d(TAG, "onActivityResult: "+getExternalCacheDir().toString());
                    message.setImageUri(imageUri);
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    final String imagePath;
                    Message message = new Message();
                    message.setMessageType(MessageInfoConstant.Message_Type_Send);
                    message.setMessageContentType(MessageInfoConstant.Message_Content_Image);
                    message.setMessageTime(new TimeUtil().getCurrentTime());
                    if(Build.VERSION.SDK_INT>=19){
                        imagePath=handleImageOnKitKat(data);
                        Log.d(TAG, "onActivityResult: "+imagePath);
                    }else {
                        imagePath=handleImageBeforeKitKat(data);
                    }
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
//                                String path= SiliCompressor.with(MainActivity.this).compress(imagePath,getExternalCacheDir());
                            String path= ZipUtil.compressImage(imagePath,getExternalCacheDir());
                            Log.d(TAG, "onActivityResult: "+path);
                        }
                    }.start();
                    message.setFilepath(imagePath);
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d(TAG, "handleImageOnKitKat: "+uri);
//        document类型uri
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri=ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }

//            content类型uri
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath=getImagePath(uri,null);

//            file类型uri
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath=uri.getPath();
        }
        return imagePath;
    }
    private String handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        return imagePath;
    }
    private String getImagePath(Uri uri,String selection){
        String path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if (cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            //hardcode
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "open album failed", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String inputTextCache = inputText.getText().toString();
        save(inputTextCache, "inputTextCache");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "back", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_icon:
                Toast.makeText(this, "menu", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    //聊天编辑框的草稿
    public void save(String inputText, String fileName) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String load(String fileName) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    //测试
    public void initMessage() {
        Message msg0 = new Message();
        msg0.setMessageTime(new TimeUtil().getCurrentTime());
        msg0.setMessageContentType(MessageInfoConstant.Message_Content_Text);
        msg0.setContent("hello");
        msg0.setMessageType(MessageInfoConstant.Message_Type_Received);
        messageList.add(msg0);
    }
}