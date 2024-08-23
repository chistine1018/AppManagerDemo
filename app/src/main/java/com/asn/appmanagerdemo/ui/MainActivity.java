package com.asn.appmanagerdemo.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;

import com.asn.appmanagerdemo.R;
import com.asn.appmanagerdemo.adapter.AppInfoAdapter;
import com.asn.appmanagerdemo.model.AppInfo;
import com.asn.appmanagerdemo.receiver.MyReceiver;
import com.asn.appmanagerdemo.util.AppUtil;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    private LinearLayout ll_loading;
    private MyReceiver myReceiver;
    private static final String alipay_person_qr = "https://qr.alipay.com/fkx15162b9anoefd3ddrt98";
    List<AppInfo> userAppInfos;
    List<AppInfo> userAppInfosOld;
    List<AppInfo> systemAppInfos;
    List<AppInfo> systemAppInfosOld;
    private ListView listView;
    private AppInfoAdapter appInfoAdapter;
    private MenuItem searchItem;
    private MenuItem firstMenuItem;
    private SearchView mSearchView;
    private final String TAG = MainActivity.class.getSimpleName();

    private int sortByName = 1;
    private int sortByPermissions = 1;
    private int sortByAPKSize = 1;
    private int sortByALLSize = 1;
    private int communication = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll_loading = findViewById(R.id.ll_loading);
        ll_loading.setVisibility(View.VISIBLE);
        AppUtil.requestPermissions(this);

        new Thread(runnable).start();


        Disposable disposable = mInput.publishSubject1.subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                ll_loading.setVisibility(View.VISIBLE);
                new Thread(runnable).start();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });

        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    private List<String> checkPermissions() {
        List<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        return permissions;
    }

    private void requestPermissions() {
        List<String> permissions = checkPermissions();
        Log.i("test", "requestPermissions: " + permissions.size());
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(uri);
                getApplicationContext().startActivity(intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
    }

    //在子執行緒中初始化資料並載入listview，因為使用者手機中的應用程式越多載入時間就越長，所以不能在主執行緒中載入listview
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            long datainitstart = SystemClock.currentThreadTimeMillis();
            //取得已安裝的app信息
            userAppInfos = AppUtil.getAppInfo(AppUtil.USER_APP, MainActivity.this);
            systemAppInfos = AppUtil.getAppInfo(AppUtil.SYSTEM_APP, MainActivity.this);
            userAppInfosOld = userAppInfos;
            systemAppInfosOld = systemAppInfos;
            //預設按名稱升序排序
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            long datainitend = SystemClock.currentThreadTimeMillis();
            Log.i("datainittime", datainitend - datainitstart + "");
            long uiinitstart = SystemClock.currentThreadTimeMillis();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView = findViewById(R.id.lv_baseAdapter);

                    appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);

                    ll_loading.setVisibility(View.GONE);
                    searchItem.setVisible(true);
                    listView.setAdapter(appInfoAdapter);

                    //給listView設定item點擊監聽
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        /**
                         * @param parent listview
                         * @param view 目前行的item的view對象
                         * @param position 目前行的下標
                         * @param id
                         */
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = userAppInfos.get(position);
                            Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                            intent.putExtra("appInfo", appInfo);

                            byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                            intent.putExtra("appIcon", appIcon);
                            startActivity(intent);
                        }
                    });
                }
            });
            long uiinitend = SystemClock.currentThreadTimeMillis();
            Log.i("uiinittime", uiinitend - uiinitstart + "");

            if (!AppUtil.hasUsageStatsPermission(MainActivity.this)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "請授予本應用程式允許存取使用記錄的權限,否則無法統計app的資料、快取大小等信息", Toast.LENGTH_LONG).show();
                    }
                });
                startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY), 1);
            } else {
                //取得應用程式的總大小、資料大小、快取大小等數據，耗時長,在新的子執行緒中執行
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppUtil.getSize(MainActivity.this, systemAppInfos);
                        AppUtil.getSize(MainActivity.this, userAppInfos);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                appInfoAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            }
        }
    };

    //創建並初始化OptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        requestPermissions();

        //得到選單載入器對象
        MenuInflater menuInflater = getMenuInflater();
        //載入選單文件
        menuInflater.inflate(R.menu.option_menu, menu);

        firstMenuItem = menu.findItem(R.id.showApp);

        //標題列搜尋框功能實現
        searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint("請輸入要搜尋的應用程式名稱");//設定輸入框提示語
        mSearchView.setIconified(true);//設定searchView是否處於展開狀態 false:展開
        searchItem.setVisible(false);
        //mSearchView.setIconifiedByDefault(false);
        //mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //提交按鈕的點擊事件
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            //當輸入框內容改變的時候回調方法
            @Override
            public boolean onQueryTextChange(String newText) {
                //如果第一個選單項目是‘顯示系統應用程式’，表示目前顯示的是使用者應用
                try {
                    if (firstMenuItem.getTitle().toString().equals("顯示系統應用")) {
                        if (!newText.equals("")) {
                            userAppInfos = userAppInfosOld;
                            List<AppInfo> userAppInfosNew = new ArrayList<>();

                            long searchstart = System.currentTimeMillis();
                            AppInfo appInfo;
                            for (int i = 0; i < userAppInfos.size(); i++) {
                                appInfo = userAppInfos.get(i);
                                //支援透過拼音進行模糊搜尋帶有中文名稱的app PinyinTool.getPinyinString(appInfo.getAppName()).contains(newText)||
                                if (appInfo.getAppName().toLowerCase().contains(newText) || appInfo.getAppNamePinyin().toLowerCase().contains(newText)) {
                                    userAppInfosNew.add(appInfo);
                                }

                            }
                            long searchend = System.currentTimeMillis();
                            Log.e("searchtimeused", searchend - searchstart + "");
                            userAppInfos = userAppInfosNew;
                            //appInfoAdapter.notifyDataSetInvalidated();
                            appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        } else {//若輸入框內容為空，則顯示全部app
                            userAppInfos = userAppInfosOld;
                            //appInfoAdapter.notifyDataSetInvalidated();
                            appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        }
                    } else {
                        if (!newText.equals("")) {
                            systemAppInfos = systemAppInfosOld;
                            List<AppInfo> systemAppInfosNew = new ArrayList<>();
                            AppInfo appInfo;
                            for (int i = 0; i < systemAppInfos.size(); i++) {
                                appInfo = systemAppInfos.get(i);
                                //支援透過拼音進行模糊搜尋有中文名字的app PinyinTool.getPinyinString(appInfo.getAppName()).contains(newText)||
                                if (appInfo.getAppName().toLowerCase().contains(newText) || appInfo.getAppNamePinyin().toLowerCase().contains(newText)) {
                                    systemAppInfosNew.add(appInfo);
                                }
                            }
                            systemAppInfos = systemAppInfosNew;
                            appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        } else {//若輸入框內容為空，則顯示全部app
                            systemAppInfos = systemAppInfosOld;
                            appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                            listView.setAdapter(appInfoAdapter);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.showApp) {
            //如果第一個選單項目是‘顯示系統應用程式’，表示當前顯示是使用者應用
            if (item.getTitle().toString().equals("顯示系統應用")) {
                systemAppInfos = systemAppInfosOld;
                appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                listView.setAdapter(appInfoAdapter);
                item.setTitle("显示用户应用");
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = systemAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);


                        byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                        intent.putExtra("appIcon", appIcon);
                        startActivity(intent);

                    }
                });
            } else {
                userAppInfos = userAppInfosOld;
                appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                listView.setAdapter(appInfoAdapter);
                item.setTitle("顯示系統應用");
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = userAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);

                        byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                        intent.putExtra("appIcon", appIcon);
                        startActivity(intent);
                    }
                });
            }
        } else if (itemId == R.id.sortByName) {
            AppUtil.sortByName(sortByName, userAppInfos, systemAppInfos);
            sortByName++;
            //如果第一個選單項目是‘顯示系統應用程式’，表示目前顯示是使用者應用
            if (firstMenuItem.getTitle().toString().equals("顯示系統應用")) {
                userAppInfos = userAppInfosOld;
                appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = userAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);

                        Drawable drawable = appInfo.getAppIcon();
                        if (drawable instanceof BitmapDrawable) {
                            BitmapDrawable bd = (BitmapDrawable) drawable;
                            Bitmap bm = bd.getBitmap();
                            intent.putExtra("appIcon", bm);
                            startActivity(intent);
                        } else {
                            startActivity(intent);
                        }
                    }
                });
            } else if (firstMenuItem.getTitle().toString().equals("顯示用戶應用")) {
                systemAppInfos = systemAppInfosOld;
                appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = systemAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);

                        byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                        intent.putExtra("appIcon", appIcon);
                        startActivity(intent);
                    }
                });
            }

            listView.setAdapter(appInfoAdapter);
        } else if (itemId == R.id.sortByPermissions) {
            AppUtil.sortByPermissions(sortByPermissions, userAppInfos, systemAppInfos);
            sortByPermissions++;
            //如果第一個選單項目是‘顯示系統應用程式’，表示當前顯示是使用者應用
            if (firstMenuItem.getTitle().toString().equals("顯示系統應用")) {
                userAppInfos = userAppInfosOld;
                appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = userAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);

                        byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                        intent.putExtra("appIcon", appIcon);
                        startActivity(intent);
                    }
                });
            } else if (firstMenuItem.getTitle().toString().equals("顯示用戶應用")) {
                systemAppInfos = systemAppInfosOld;
                appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = systemAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);

                        byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                        intent.putExtra("appIcon", appIcon);
                        startActivity(intent);
                    }
                });
            }
            listView.setAdapter(appInfoAdapter);
        } else if (itemId == R.id.sortByALLSize) {
            AppUtil.sortByALLSize(sortByALLSize, userAppInfos, systemAppInfos);
            sortByALLSize++;
            //如果第一個選單項目是‘顯示系統應用程式’，表示目前顯示的是使用者應用
            if (firstMenuItem.getTitle().toString().equals("顯示系統應用")) {
                appInfoAdapter = new AppInfoAdapter(userAppInfos, MainActivity.this);
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = userAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);

                        byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                        intent.putExtra("appIcon", appIcon);
                        startActivity(intent);
                    }
                });
            } else if (firstMenuItem.getTitle().toString().equals("顯示用戶應用")) {
                appInfoAdapter = new AppInfoAdapter(systemAppInfos, MainActivity.this);
                //給listView設定item點擊監聽
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = systemAppInfos.get(position);
                        Intent intent = new Intent(MainActivity.this, AppOperatingActivity.class);
                        intent.putExtra("appInfo", appInfo);

                        byte[] appIcon = bitmap2Bytes(drawableToBitamp(appInfo.getAppIcon()));
                        intent.putExtra("appIcon", appIcon);
                        startActivity(intent);
                    }
                });
            }
            listView.setAdapter(appInfoAdapter);
        }

        return super.onOptionsItemSelected(item);
    }


    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable转Bitmap");
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(w, h, config);

        //注意，下面三行程式碼要用到，否在View或surfaceview裡的canvas.drawBitmap會看不到圖
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    //檢測是否安裝了支付寶
    public static boolean checkAliPayInstalled(Context context) {
        Uri uri = Uri.parse("alipays://platformapi/startApp");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        return componentName != null;
    }

    private void jumpToMessageActivity() {
//        Intent intent = new Intent(this, MessageActivity.class);
//        intent.putExtra(MessageUtil.INTENT_EXTRA_IS_PEER_MODE, false);
//        intent.putExtra(MessageUtil.INTENT_EXTRA_TARGET_NAME, "appManagerRoom");
//        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, mUserId);
//        startActivityForResult(intent, 1);
    }

    public Input mInput = new Input();

    static Bitmap bitmap;

    public static class Input {

        public static PublishSubject<Boolean> publishSubject1 = PublishSubject.create();
        public PublishSubject<Boolean> publishSubject2 = PublishSubject.create();
    }

    public class Output {

    }
}