package info.ishared.android.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import info.ishared.android.R;
import info.ishared.android.bean.ContactsInfo;
import info.ishared.android.bean.NumberType;
import info.ishared.android.util.AlertDialogUtils;
import info.ishared.android.util.ToastUtils;
import info.ishared.android.util.ViewUtils;
import roboguice.inject.InjectView;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Lee
 * Date: 13-3-29
 * Time: 下午7:53
 */
public class BlackContactsListActivity  extends ContactsListActivity {
    @InjectView(R.id.contacts_list_add_btn)
    private Button mAddBtn;

    private EditText mName;
    private EditText mNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.showView(mAddBtn);
        LayoutInflater inflater = (LayoutInflater) BlackContactsListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.dialog_input_layout, null);
        mName=(EditText)layout.findViewById(R.id.dialog_input_name);
        mNumber=(EditText)layout.findViewById(R.id.dialog_input_number);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.showInputDialog(BlackContactsListActivity.this,"",new AlertDialogUtils.Executor() {
                    @Override
                    public void execute() {
                        ToastUtils.showMessage(BlackContactsListActivity.this,mName.getText().toString()+","+mNumber.getText().toString());
                    }
                });
            }
        });
        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "删除联系人");

            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String phoneNumber = contactsData.get(menuInfo.position).get("number");
        mController.deleteContactInfoByPhoneNumber(phoneNumber);
        contactsData.remove(menuInfo.position);
        adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    protected List<ContactsInfo> initData() {
        return mController.queryContactInfoByNumberType(NumberType.BLACK);
    }
}