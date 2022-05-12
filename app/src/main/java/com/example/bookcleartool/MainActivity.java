package com.example.bookcleartool;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.example.bookcleartool.databinding.ActivityMainBinding;
import com.example.bookcleartool.setting.SettingActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private List<CharSequence> list = new ArrayList();
    private float[] hsv = {1, 1, 1};
    private ProgressDialog progressDialog;
    private ClearTask task;
    private ActivityMainBinding mainViewBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainViewBinding.getRoot());
        if (!verifyStoragePermission()) {
            finish();
            return;
        }
        if (getIntent() != null && getIntent().getData() != null) {
            String path = Utils.turnUri2FilePath(this, getIntent().getData());
            task = new ClearTask();
            task.execute(path);
        } else {
            mainViewBinding.cardViewError.setVisibility(View.VISIBLE);
        }
        mainViewBinding.btnExit.setOnClickListener(v -> {
            finish();
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("处理中……");

        mainViewBinding.listview.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, list));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean verifyStoragePermission() {
        //1.检测权限
        int permission = PermissionChecker.PERMISSION_GRANTED;
        for (String s : PERMISSIONS_STORAGE) {
            permission = ActivityCompat.checkSelfPermission(this, s);
        }
        if (permission != PermissionChecker.PERMISSION_GRANTED) {
            //2.没有权限，弹出对话框申请
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_CODE_STORAGE
            );
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE) {
            int result = PermissionChecker.PERMISSION_GRANTED;
            for (int grantResult : grantResults) {
                result = result ^ grantResult;
            }
            if (result == PermissionChecker.PERMISSION_GRANTED) {
                //权限申请成功
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            } else {
                //权限申请失败
                Toast.makeText(this, "未获取到所有授权，退出。", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private Pair<String, CharSequence> clearText2(String str) {
        if (str == null || str.isEmpty())
            return null;
        Matcher matcher = Pattern.compile("&#\\d*").matcher(str);
        ArrayList<Integer> list1 = new ArrayList<Integer>();
        SpannableString spannableString = new SpannableString(str);
        boolean hasMatched = false;
        while (matcher.find()) {
            hasMatched = true;
            list1.add(matcher.start());
            list1.add(matcher.end());
            spannableString.setSpan(new ForegroundColorSpan(getRandomColor()), matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if (hasMatched)
            return new Pair(matcher.replaceAll(""), spannableString);
        else
            return null;
    }

    private int getRandomColor() {
        hsv[0] = (float) (Math.random() * 360);
        return Color.HSVToColor(hsv);
    }


    class ProgressBeen {
        int curLine;
        CharSequence str;

        public ProgressBeen(int curLine, CharSequence str) {
            this.curLine = curLine;
            this.str = str;
        }

        public CharSequence toCharSequence() {
            SpannableStringBuilder result = new SpannableStringBuilder();
            SpannableString ss = new SpannableString(String.valueOf(curLine));
            ss.setSpan(new RelativeSizeSpan(.7f), 0, ss.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            result.append(ss);
            result.append(str);
            return result;
        }
    }

    class ClearTask extends AsyncTask<String, ProgressBeen, Boolean> {
        public Boolean doInBackground(String... params) {
            final String path = params[0];
            BufferedReader reader = null;
            BufferedWriter writer = null;

            String backPath = path.substring(0, path.lastIndexOf('.')) + '1' + path.substring(path.lastIndexOf('.'));

            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "GBK"));
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backPath), "GBK"));

                String temp;
                Pair<String, CharSequence> pair;
                int lineNum = 0;
                do {
                    temp = reader.readLine();
                    if (temp == null)
                        break;
                    lineNum++;
                    pair = clearText2(temp);
                    if (pair != null) {
                        writer.write(pair.first);
                        publishProgress(new ProgressBeen(lineNum, pair.second));
                    } else
                        writer.write(temp);
                    writer.newLine();
                } while (true);

            } catch (IOException e) {
                publishProgress(new ProgressBeen(-1, e.getMessage()));
                return false;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return true;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            mainViewBinding.listview.setVisibility(result != null && result ? View.VISIBLE : View.GONE);
            mainViewBinding.cardViewError.setVisibility(result != null && result ? View.GONE : View.VISIBLE);

        }

        @Override
        public void onProgressUpdate(ProgressBeen... values) {
            super.onProgressUpdate(values);
            ((ArrayAdapter) mainViewBinding.listview.getAdapter()).notifyDataSetChanged();
            list.add(values[0].toCharSequence());
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }
}
