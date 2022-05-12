package com.example.bookcleartool.setting;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookcleartool.R;
import com.example.bookcleartool.database.FilterBean;
import com.example.bookcleartool.database.FilterDao;
import com.example.bookcleartool.database.MyDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {
    private ListView mListView;
    private FilterChooseAdapter adapter;
    private ArrayList<FilterBean> mData = new ArrayList<>();

    private LoadDbTask mLoadTask;
    private LoadDbTask mSaveTask;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mListView = findViewById(R.id.listview);
        adapter = new FilterChooseAdapter(SettingActivity.this, mData);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mListView.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //加载数据
        mLoadTask = new LoadDbTask();
        mLoadTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_btn_add:
                Toast.makeText(this, "待开发……", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mLoadTask != null && !mLoadTask.isCancelled()) {
            mLoadTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * 重新设置列表
     *
     * @param values 配置列表
     */
    private void resetListView(List<FilterBean>[] values) {
        mData.clear();
        if (values != null && values.length > 0) {
            mData.addAll(values[0]);
            mListView.setAdapter(new FilterChooseAdapter(SettingActivity.this, values[0]));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    /**
     * 加载任务
     */
    private class LoadDbTask extends AsyncTask<Void, List<FilterBean>, Exception> {
        @Override
        protected Exception doInBackground(Void... voids) {
            try {
                FilterDao dao = MyDatabase.getInstance(SettingActivity.this).getFilterDao();
                List<FilterBean> list = dao.getAll();
                publishProgress(list);
            } catch (Exception e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            if (e != null) {
                Toast.makeText(SettingActivity.this, "异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(List<FilterBean>... values) {
            super.onProgressUpdate(values);
            resetListView(values);
        }
    }

    /**
     * 保存任务，保存完成后重新加载
     */
    private class SaveDbTask extends AsyncTask<FilterBean, List<FilterBean>, Exception> {
        @Override
        protected Exception doInBackground(FilterBean... filterBeans) {
            if (filterBeans == null || filterBeans.length == 0) {
                return new Exception("Save nothing.");
            }

            FilterDao dao = MyDatabase.getInstance(SettingActivity.this).getFilterDao();
            boolean isNeedReload = false;//判断是否需要重新加载。插入和删除才需要重新加载
            Exception exception = null;
            try {
                for (FilterBean filterBean : filterBeans) {
                    if (filterBean.ID == FilterBean.DEFAULT_ID) {
                        dao.insert(filterBean);
                        isNeedReload = true;
                    } else {
                        dao.update(filterBean);
                    }

                }
            } catch (Exception e) {
                exception = e;
            }

            if (isNeedReload) {
                List<FilterBean> list = dao.getAll();
                publishProgress(list);
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            if (e != null) {
                Toast.makeText(SettingActivity.this, "异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(List<FilterBean>... values) {
            super.onProgressUpdate(values);
            resetListView(values);
        }
    }

    /**
     * 列表item适配器
     */
    private class FilterChooseAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<FilterBean> data;

        public FilterChooseAdapter(Context context, List<FilterBean> values) {
            inflater = LayoutInflater.from(context);
            data = values;
        }

        @Override
        public int getCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (data != null) {
                return data.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (data != null) {
                return data.get(position).ID;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_checked, parent, false);
            }

            if (convertView != null && convertView instanceof TextView) {
                ((TextView) convertView).setText(data.get(position).content);
            }

            return convertView;
        }
    }
}
