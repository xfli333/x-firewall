package info.ishared.android.firewall.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import info.ishared.android.firewall.MainActivity;
import info.ishared.android.firewall.R;
import info.ishared.android.firewall.bean.BlockLog;
import info.ishared.android.firewall.util.AlertDialogUtils;
import info.ishared.android.firewall.util.PageJumpUtils;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Seven
 * Date: 13-3-29
 * Time: AM11:03
 */
public class ViewBlockLogActivity extends RoboSherlockActivity implements View.OnClickListener {
    @InjectView(R.id.block_log_list_view)
    private ListView mListView;

    private SimpleAdapter adapter;
    private List<Map<String,String>> logsData=new ArrayList<Map<String,String>>();

    private ProgressDialog mProgressDialog=null;

    @InjectView(R.id.block_log_back_btn)
    private Button mBackBtn;
    @InjectView(R.id.block_log_clean_btn)
    private Button mCleanBtn;

    private ViewBlockLogController mController;
    private Handler mHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_block_log_list);
        mController = new ViewBlockLogController(this);
        mHandler = new Handler();
        mBackBtn.setOnClickListener(this);
        mCleanBtn.setOnClickListener(this);
        mProgressDialog= AlertDialogUtils.createProgressDialog(this);
        mProgressDialog.setMessage("正在加载拦截记录....");
        mProgressDialog.show();
        initListViewData();
        initListViewGUI();
    }

    private void initListViewGUI() {
        adapter = new SimpleAdapter(this,logsData, R.layout.view_block_log_list_item, new String[]{"info","time"},new int[]{R.id.block_log_item_text1,R.id.block_log_item_text2}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view=super.getView(position, convertView, parent);
                if (position % 2 != 0)
                    view.setBackgroundResource(R.drawable.table_background_selector);
                else
                    view.setBackgroundResource(R.drawable.table_background_alternate_selector);
                return view;
            }
        };
        mListView.setAdapter(adapter);
        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "回拨");

            }
        });
    }

    private void initListViewData() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                logsData.clear();
                List<BlockLog> blockLogs=mController.queryBlockLogs();
                if(blockLogs.isEmpty()){
                    Map<String,String> map=new HashMap<String, String>(2);
                    map.put("info","无拦截记录");
                    map.put("time","");
                    logsData.add(map);
                }
                for(BlockLog log : blockLogs){
                    Map<String,String> map=new HashMap<String, String>(2);
                    map.put("info",log.getContactsName()+","+log.getPhoneNumber());
                    map.put("time","拦截时间:"+log.getBlockDate());
                    logsData.add(map);
                }
                adapter.notifyDataSetChanged();
                mProgressDialog.hide();
            }
        });

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String info = logsData.get(menuInfo.position).get("info");
        if(!"无拦截记录".equals(info)){
            String phoneNumber=info.split(",")[1];
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.intent.action.CALL");
            Uri uri = Uri.parse("tel:" + phoneNumber);
            localIntent.setData(uri);
            this.startActivity(localIntent);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.block_log_back_btn:
                PageJumpUtils.jump(this, MainActivity.class);
                this.finish();
                break;
            case R.id.block_log_clean_btn:
                this.mController.cleanBlockLogs();
                initListViewData();
                break;
            default:
                break;
        }
    }
    @Override
    public void onBackPressed() {
        PageJumpUtils.jump(this,MainActivity.class);
        this.finish();
    }
}
