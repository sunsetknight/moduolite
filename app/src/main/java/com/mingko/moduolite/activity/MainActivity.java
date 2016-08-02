package com.mingko.moduolite.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.mingko.moduolite.R;
import com.mingko.moduolite.fragment.ModuoFragment;
import com.mingko.moduolite.fragment.SettingConnectFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    /** 默认IP */
    public static final String IP = "192.168.2.15";
    /** 默认端口 */
    public static final int PORT = 10001;

    /** 点击两次返回按钮退出程序 单位：毫秒 */
    private static final long EXIT_TIME = 1500;
    /** 记录当前点击退出的时间 单位：毫秒 */
    private long exitTime = 0;
    /** 是否显示Actionbar 右上角的3个点菜单 */
    private boolean showMenu = true;

    @BindView(R.id.id_tb)
    Toolbar toolbar;

    private FragmentManager fragmentManager;
    private ModuoFragment moduoFragment;
    private SettingConnectFragment settingConnectFragment;
    private FragmentState currentFragment;

    private enum  FragmentState{
        MODUO_FRAGMENT, SETTING_CONNECT_FRAGMENT
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initView();
        initFragment();
    }

    private void initView() {
        //actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle("魔哆");

    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();
        moduoFragment = new ModuoFragment();
        settingConnectFragment = new SettingConnectFragment();
        currentFragment = FragmentState.MODUO_FRAGMENT;
        //初始化为moduoFragment
        fragmentManager.beginTransaction()
                .add(R.id.id_fragment_container, moduoFragment)
                .add(R.id.id_fragment_container, settingConnectFragment)
                .hide(settingConnectFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        moduoFragment.initClientThread(IP, PORT);
    }

    /**
     * 设置ActionBar 标题显示内容
     *
     * @param strTitle 需要显示的内容
     */
    private void setTitle(String strTitle) {
        TextView tv = (TextView) toolbar.findViewById(R.id.id_tb_title);
        tv.setText(strTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if( showMenu ){
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                switchFragment();
                break;
            case R.id.id_menu_setting_connect:
                switchFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 切换fragment
     * 同时改变Actionbar 的相关状态
     */
    private void switchFragment (){
        Fragment fromFragment = null;
        Fragment toFragment = null;

        switch (this.currentFragment){
            case MODUO_FRAGMENT:
                fromFragment = moduoFragment;
                toFragment = settingConnectFragment;
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                showMenu = false;
                invalidateOptionsMenu();
                currentFragment = FragmentState.SETTING_CONNECT_FRAGMENT;
                break;
            case SETTING_CONNECT_FRAGMENT:
                fromFragment = settingConnectFragment;
                toFragment = moduoFragment;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                showMenu = true;
                invalidateOptionsMenu();
                currentFragment = FragmentState.MODUO_FRAGMENT;
                break;
        }

        fragmentManager.beginTransaction()
                .hide(fromFragment)
                .show(toFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    /**
     * 按返回按钮时
     * 1、连续2次点击可以推出程序，并关闭与服务器连接的线程
     * 2、否则检测如果当前状态不是主界面，则返回主界面
     * 3、否则提示再次点击以退出程序
     */
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() < (exitTime + EXIT_TIME)) {
            moduoFragment.endClientThread();
            super.onBackPressed();
        } else {
            if( currentFragment == FragmentState.MODUO_FRAGMENT){
                exitTime = System.currentTimeMillis();
                Toast.makeText(this, "再次点击退出", Toast.LENGTH_SHORT).show();
            }else{
                switchFragment();
            }
        }
    }
}
