package xin.sunyq.testapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private GridView gridView;
    List<Map<String,Object>> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView=findViewById(R.id.gridView);
        AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                .setTitle("注意事项·请仔细阅读")
                .setMessage("1.这个程序仅为内部版本，可能有各种奇怪的bug，不要外传\n" +
                        "2.历史记录保存在外部储存(sdcard或模拟sdcard)根目录下FPExtractor文件夹下\n"+
                        "3.由于技术原因，没有提供垃圾清理功能，下个版本我提供个清理缓存的功能，不过也不用担心，图片不大的\n" +
                        "4.由于技术原因，本窗口每次启动都会弹出，请谅解\n" +
                        "5.如果在使用中遇到任何问题，请私戳我，并描述遇到问题的场景\n" +
                        "6.本程序仅在小米6(Android8.0.0)，Nexus 5(Android 9.0.0)和Pixel 2XL(Android 8.1.0)上测试过，理论上支持Android4.0以上的所有机型，分辨率可能没有适配，会出现显示不全的状况")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO:......
                    }
                }).create();
        alertDialog2.show();
        getPermission();
        ImageButton imgbtn=findViewById(R.id.imageButton);
        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                getPermission();
                try {
                    loadImages();
                }catch (Exception ignored){}
                hideProgressBar();
            }
        });
        final Switch switch2=findViewById(R.id.switch2);
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadImages();
            }
        });
    }

    private void showProgressBar(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.imageButton).setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar(){
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.imageButton).setVisibility(View.VISIBLE);
    }

    private void loadImages(){
        String filepathQQ=Environment.getExternalStorageDirectory().getPath()+"/tencent/MobileQQ/diskcache";
        String filepathTim=Environment.getExternalStorageDirectory().getPath()+"/tencent/Tim/diskcache";
        String filepathHistory=Environment.getExternalStorageDirectory().getPath()+"/FPExtractor";
        File fileHistory=new File(filepathHistory);
        if (!fileHistory.exists()){
            fileHistory.mkdirs();
        }
        Switch sw=findViewById(R.id.switch1);
        Switch sw2=findViewById(R.id.switch2);
        dataList=new ArrayList<Map<String,Object>>();
        File dir;
        String path;
        if(!sw.isChecked()){
            dir=new File(filepathQQ);
            path=filepathQQ;
        }else{
            dir=new File(filepathTim);
            path=filepathTim;
        }
        if(!dir.exists()){
            Toast.makeText(this,"目录不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!sw2.isChecked()) {
            File[] fileList = orderByDate(dir.listFiles());
            Vector<String> modifiedTime = new Vector<>();
            Vector<String> url = new Vector<>();
            for (File file : fileList) {
                if (file.getName().charAt(file.getName().length() - 2) == 'f' && file.getName().charAt(file.getName().length() - 1) == 'p') {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String result = formatter.format(file.lastModified());
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("pic", Uri.parse(path + "/" + file.getName()));
                    item.put("time", result);
                    dataList.add(item);
                }
            }
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, dataList, R.layout.gridview_item_layout, new String[]{"pic", "time"}, new int[]{R.id.img, R.id.textView2});
            gridView.setAdapter(simpleAdapter);
            gridView.setOnItemClickListener(this);
        }else{
            File[] fileList = orderByDate(fileHistory.listFiles());
            Vector<String> modifiedTime = new Vector<>();
            Vector<String> url = new Vector<>();
            for (File file : fileList) {
                if (file.getName().charAt(file.getName().length() - 3) == 'j'&&file.getName().charAt(file.getName().length() - 2) == 'p' && file.getName().charAt(file.getName().length() - 1) == 'g') {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String result = formatter.format(file.lastModified());
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("pic", Uri.parse(filepathHistory + "/" + file.getName()));
                    item.put("time", result);
                    dataList.add(item);
                }
            }
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, dataList, R.layout.gridview_item_layout, new String[]{"pic", "time"}, new int[]{R.id.img, R.id.textView2});
            gridView.setAdapter(simpleAdapter);
            gridView.setOnItemClickListener(this);
        }
    }

    private void getPermission() {
        int permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    124);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        String filename=((Uri)((HashMap)dataList.toArray()[position]).get("pic")).toString().split("/")[((Uri)((HashMap)dataList.toArray()[position]).get("pic")).toString().split("/").length-1];
        if(((Uri)((HashMap)dataList.toArray()[position]).get("pic")).toString().split(".").length==0){
            copyFile(((Uri) ((HashMap) dataList.toArray()[position]).get("pic")).toString(), Environment.getExternalStorageDirectory().getPath()+"/FPExtractor/"+filename+".jpg");
        }else {

        }
        openFile(this,new File(((Uri)((HashMap)dataList.toArray()[position]).get("pic")).toString()+".jpg"));
    }

    public void openFile(Context context, File file){
        try{
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            String type = getMIMEType(file);
            //设置intent的data和Type属性。
            intent.setDataAndType(FileProvider.getUriForFile(context,"xin.sunyq.testApp.fileProvider",file), type);
            //跳转
            context.startActivity(intent);
//      Intent.createChooser(intent, "请选择对应的软件打开该附件！");
        }catch (ActivityNotFoundException e) {
            // TODO: handle exception
            Toast.makeText(context, "sorry附件不能打开，请下载相关软件！",Toast.LENGTH_SHORT).show();
        }
    }

    private String getMIMEType(File file) {

        String type="*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
        /* 获取文件的后缀名*/
        String end=fName.substring(dotIndex,fName.length()).toLowerCase();
        if(end.equals(""))return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0;i<MIME_MapTable.length;i++){
            if(end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }
    // 可以自己随意添加
    private String [][]  MIME_MapTable={
            //{后缀名，MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",    "image/bmp"},
            {".c",  "text/plain"},
            {".class",  "application/octet-stream"},
            {".conf",   "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls",    "application/vnd.ms-excel"},
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",   "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h",  "text/plain"},
            {".htm",    "text/html"},
            {".html",   "text/html"},
            {".jar",    "application/java-archive"},
            {".java",   "text/plain"},
            {".jpeg",   "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",   "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",   "video/mp4"},
            {".mpga",   "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop",   "text/plain"},
            {".rc", "text/plain"},
            {".rmvb",   "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh", "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            {".xml",    "text/plain"},
            {".z",  "application/x-compress"},
            {".zip",    "application/x-zip-compressed"},
            {"",        "*/*"}
    };

    /**
     * 复制单个文件
     *
     * @param oldPath$Name String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath$Name String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     * <code>false</code> otherwise
     */
    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

        /* 如果不需要打log，可以使用下面的语句
        if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
            return false;
        }
        */

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);    //读入原文件
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File[] orderByDate(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return -1;
                else if (diff == 0)
                    return 0;
                else
                    return 1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }

        });
        return files;

    }
}
