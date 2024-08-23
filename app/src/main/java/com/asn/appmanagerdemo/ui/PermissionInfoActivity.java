package com.asn.appmanagerdemo.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.asn.appmanagerdemo.R;
import com.asn.appmanagerdemo.model.AppInfo;
import com.asn.appmanagerdemo.util.AppUtil;


public class PermissionInfoActivity extends AppCompatActivity {

    private ListView permissionInfo_lv;
    private String[] permissionInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_info);
        permissionInfo_lv = findViewById(R.id.permissionInfo_lv);

        AppInfo appInfo = getIntent().getParcelableExtra("appInfo");
        permissionInfos = appInfo.getPermissionInfos();

        setTitle(appInfo.getAppName() + "的權限");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_permissioninfo, permissionInfos);
        permissionInfo_lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.permission_info_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.copy_all) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < permissionInfos.length; i++) {
                if (i != permissionInfos.length - 1) {
                    sb.append(permissionInfos[i]).append("\n");
                } else {
                    sb.append(permissionInfos[i]);
                }
            }
            //取得剪貼簿管理器
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 建立普通字元型ClipData
            ClipData mClipData = ClipData.newPlainText("permissions", sb.toString());
            // 將ClipData內容放到系統剪貼簿裡。
            if (cm != null) {
                Toast.makeText(this, "複製成功", Toast.LENGTH_SHORT).show();
                cm.setPrimaryClip(mClipData);
            } else {
                NullPointerException exception = new NullPointerException();
                AppUtil.exceptionToast(PermissionInfoActivity.this, exception.getMessage());
                throw exception;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
