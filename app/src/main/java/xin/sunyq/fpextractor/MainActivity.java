package xin.sunyq.fpextractor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {
    public GridView gridView;
    public GridView gridView2;
    public static List<Map<String,Object>> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gridView=findViewById(R.id.gridView);
        gridView2=findViewById(R.id.gridView2);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        getPermission();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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
        ImageButton imgbtn2=findViewById(R.id.imageButton2);
        imgbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                getPermission();
                try {
                    loadImages2();
                }catch (Exception ignored){}
                hideProgressBar();
            }
        });
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void loadImages2(){
        String filepathHistory=Environment.getExternalStorageDirectory().getPath()+"/FPExtractor";
        File fileHistory=new File(filepathHistory);
        if (!fileHistory.exists()){
            fileHistory.mkdirs();
        }
        dataList=new ArrayList<Map<String,Object>>();
        File[] fileList = orderByDate(fileHistory.listFiles());
        try {
            Vector<String> modifiedTime = new Vector<>();
            Vector<String> url = new Vector<>();
            for (File file : fileList) {
                if (file.getName().charAt(file.getName().length() - 3) == 'j' && file.getName().charAt(file.getName().length() - 2) == 'p' && file.getName().charAt(file.getName().length() - 1) == 'g') {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String result = formatter.format(file.lastModified());
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("pic", Uri.parse(filepathHistory + "/" + file.getName()));
                    item.put("time", result);
                    dataList.add(item);
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, dataList, R.layout.gridview_item_layout, new String[]{"pic", "time"}, new int[]{R.id.img, R.id.textView2});
        gridView2.setAdapter(simpleAdapter);
        gridView2.setOnItemClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            boolean needUpdate= (boolean) msg.obj;
            hideUpdateProgressBar();
            TextView t=findViewById(R.id.textView3);
            if(needUpdate){
                t.setText(R.string.needupdate);
            }else{
                t.setText(R.string.noneedupdate);
            }
        };
    };

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            findViewById(R.id.main).setVisibility(View.VISIBLE);
            findViewById(R.id.history).setVisibility(View.INVISIBLE);
            findViewById(R.id.update).setVisibility(View.INVISIBLE);
            findViewById(R.id.about).setVisibility(View.INVISIBLE);
            getPermission();
            loadImages();
        } else if (id == R.id.nav_history) {
            findViewById(R.id.main).setVisibility(View.INVISIBLE);
            findViewById(R.id.history).setVisibility(View.VISIBLE);
            findViewById(R.id.update).setVisibility(View.INVISIBLE);
            findViewById(R.id.about).setVisibility(View.INVISIBLE);
            getPermission();
            loadImages2();
        } else if (id == R.id.nav_update) {
            showUpdateProgressBar();
            findViewById(R.id.main).setVisibility(View.INVISIBLE);
            findViewById(R.id.history).setVisibility(View.INVISIBLE);
            findViewById(R.id.update).setVisibility(View.VISIBLE);
            findViewById(R.id.about).setVisibility(View.INVISIBLE);

            new Thread(){
                public void run(){
                    Message msg=Message.obtain();
                    msg.obj= checkForUpdate();
                    handler.sendMessage(msg);
                }
            }.start();

        } else if (id == R.id.nav_about) {
            findViewById(R.id.main).setVisibility(View.INVISIBLE);
            findViewById(R.id.history).setVisibility(View.INVISIBLE);
            findViewById(R.id.update).setVisibility(View.INVISIBLE);
            findViewById(R.id.about).setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showProgressBar() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.imageButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
        findViewById(R.id.imageButton2).setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar() {
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.imageButton).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);
        findViewById(R.id.imageButton2).setVisibility(View.VISIBLE);
    }

    private void showUpdateProgressBar() {
        findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
        findViewById(R.id.progressBar3).setVisibility(View.VISIBLE);
    }

    private void hideUpdateProgressBar() {
        findViewById(R.id.textView4).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar3).setVisibility(View.INVISIBLE);
    }


    private void loadImages() {
        String filepathQQ = Environment.getExternalStorageDirectory().getPath() + "/tencent/MobileQQ/diskcache";
        String filepathTim = Environment.getExternalStorageDirectory().getPath() + "/tencent/Tim/diskcache";
        String filepathHistory = Environment.getExternalStorageDirectory().getPath() + "/FPExtractor";
        File fileHistory = new File(filepathHistory);
        if (!fileHistory.exists()) {
            fileHistory.mkdirs();
        }
        Switch sw = findViewById(R.id.switch1);
        dataList = new ArrayList<Map<String, Object>>();
        File dir;
        String path;
        if (!sw.isChecked()) {
            dir = new File(filepathQQ);
            path = filepathQQ;
        } else {
            dir = new File(filepathTim);
            path = filepathTim;
        }
        if (!dir.exists()) {
            Toast.makeText(this, "目录不存在", Toast.LENGTH_SHORT).show();
            return;
        }
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
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String filename = ((Uri) ((HashMap) dataList.toArray()[position]).get("pic")).toString().split("/")[((Uri) ((HashMap) dataList.toArray()[position]).get("pic")).toString().split("/").length - 1];
        if (findViewById(R.id.main).getVisibility()==View.VISIBLE) {
            copyFile(((Uri) ((HashMap) dataList.toArray()[position]).get("pic")).toString(), Environment.getExternalStorageDirectory().getPath() + "/FPExtractor/" + filename + ".jpg");
        }
        openFile(this, new File(Environment.getExternalStorageDirectory().getPath() + "/FPExtractor/" + filename + ".jpg"));
    }

    public void openFile(Context context, File file) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            String type = getMIMEType(file);
            //设置intent的data和Type属性。
            intent.setDataAndType(FileProvider.getUriForFile(context, "xin.sunyq.fpextractor.fileProvider", file), type);
            //跳转
            context.startActivity(intent);
//      Intent.createChooser(intent, "请选择对应的软件打开该附件！");
        } catch (ActivityNotFoundException e) {
            // TODO: handle exception
            Toast.makeText(context, "sorry附件不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show();
        }
    }

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
        try {
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
        }catch (Exception e){
            return null;
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
    private String[][] MIME_MapTable = {
            //{后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };
    private boolean checkForUpdate(){
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        String httpurl="https://api.github.com/repos/IzaiahSun/FPExtractor/releases/latest";
        int[] current_version={0,0,5};
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            connection.disconnect();// 关闭远程连接
        }
        try {
            result = result.split("\"tag_name\":\"")[1].split("\"")[0];
            for (int v = 0; v < 3; v++) {
                if (result.charAt(v*2) - '0' > current_version[v])
                    return true;
                if (result.charAt(v*2) - '0' < current_version[v])
                    return false;
            }
            return false;
        }catch (NullPointerException e){
            Toast.makeText(this, "超时", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
