package info.ishared.android.firewall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import info.ishared.android.firewall.R;
import info.ishared.android.firewall.activity.SettingActivity;
import info.ishared.android.firewall.activity.ViewBlockLogActivity;
import info.ishared.android.firewall.service.FirewallService;
import info.ishared.android.firewall.ui.CustomerProgressDialog;
import info.ishared.android.firewall.util.PageJumpUtils;
import info.ishared.android.firewall.util.SystemUtils;
import info.ishared.android.firewall.util.ViewUtils;
import roboguice.inject.InjectView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends RoboSherlockActivity implements View.OnClickListener {

    @InjectView(R.id.run_btn)
    private Button mRunButton;
    @InjectView(R.id.stop_btn)
    private Button mStopButton;
    @InjectView(R.id.setting_btn)
    private Button mSettingButton;
    @InjectView(R.id.view_log_btn)
    private Button mViewLogBtn;
    @InjectView(R.id.pb_loading)
    private ProgressBar mLoading;
    @InjectView(R.id.service_status_txt)
    private TextView mTextView;

    @InjectView(R.id.block_rule_status_txt)
    private TextView mBlockRuleStatusTextView;

    @InjectView(R.id.transfer_status_txt)
    private TextView mTransferStatusTextView;


    private CustomerProgressDialog mDialog;

    private FirewallService mService;

    private boolean isServiceWorked = false;

    private Handler mHandler;

    private MainController mController;

//    private ServiceConnection mServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mService = ((FirewallService.MyBinder)service).getService();
//        }
//
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mController = new MainController(this);

        mHandler = new Handler();
        mRunButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mSettingButton.setOnClickListener(this);
        mViewLogBtn.setOnClickListener(this);

        isServiceWorked = SystemUtils.isServiceWorked(this, "info.ishared.android.firewall.service.FirewallService");
        refreshButtonAndTextView(isServiceWorked);

        queryBlockRuleAndTransferRule();
    }

    private void queryBlockRuleAndTransferRule() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String blockRuleTxt = "拦截规则:"+mController.getBlockRuleTextViewText();
                mBlockRuleStatusTextView.setText(blockRuleTxt);
                String transferRuleTxt = "运营商和提示音:"+mController.getTransferRuleText();
                mTransferStatusTextView.setText(transferRuleTxt);
            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.run_btn:
                startServiceAndRefreshUI();

                break;
            case R.id.stop_btn:
                stopServiceAndRefreshUI();
                break;
            case R.id.setting_btn:
//                boolean isWork = SystemUtils.isServiceWorked(this, "info.ishared.android.firewall.service.FirewallService");
//                ToastUtils.showMessage(this, isWork + "");
                PageJumpUtils.jump(this, SettingActivity.class);
                this.finish();
                break;
            case R.id.view_log_btn:
                PageJumpUtils.jump(this, ViewBlockLogActivity.class);
                this.finish();
                break;
            default:
                break;
        }
    }

    private void startServiceAndRefreshUI() {
        Intent intent = new Intent(this, FirewallService.class);
        this.startService(intent);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                while (!SystemUtils.isServiceWorked(MainActivity.this, "info.ishared.android.firewall.service.FirewallService")) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                refreshButtonAndTextView(true);
            }
        });
    }

    private void stopServiceAndRefreshUI() {
        Intent intent = new Intent(this, FirewallService.class);
        this.stopService(intent);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                while (SystemUtils.isServiceWorked(MainActivity.this, "info.ishared.android.firewall.service.FirewallService")) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                refreshButtonAndTextView(false);
            }
        });
    }

    private void refreshButtonAndTextView(boolean isServiceWorked) {
        if (isServiceWorked) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText("来电防火墙正在运行中....");
                    ViewUtils.hideView(mRunButton);
                    ViewUtils.showView(mStopButton);
                    ViewUtils.showView(mLoading);
                    ViewUtils.showView(mBlockRuleStatusTextView);
                    ViewUtils.showView(mTransferStatusTextView);
                }
            });

        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText("来电防火墙已停止拦截....");
                    ViewUtils.hideView(mStopButton);
                    ViewUtils.showView(mRunButton);
                    ViewUtils.hideView(mLoading);
                    ViewUtils.hideView(mBlockRuleStatusTextView);
                    ViewUtils.hideView(mTransferStatusTextView);
                }
            });

        }
    }
}
