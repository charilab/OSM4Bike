package jp.ne.sakura.charilab.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/*
MIT License

Copyright (c) 2017 Shunji Uno <uno@charilab.sakura.ne.jp>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
/**
 * Created by uno on 12/25/15.
 */
public class ListDialog extends DialogFragment {
    private Context context;
    private String title;
    private menuItem[] items;
    private eventListeners funcs;

    public interface eventListeners {
        void onSelected(int i);
        void onCancel();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.list_dialog, null);
        final itemAdapter adapter = new itemAdapter(context, items);

        if (title != null) {
            ((TextView) dialogView.findViewById(R.id.listTitle)).setText(title);
        }
        ListView lv = (ListView) dialogView.findViewById(R.id.listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                funcs.onSelected(position);
                ListDialog.this.getDialog().dismiss();
            }
        });
        builder.setView(dialogView);

        Dialog dialog = builder.create();
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                        == InputDevice.SOURCE_GAMEPAD) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (event.getRepeatCount() == 0) {
                            switch (keyCode) {
                                case KeyEvent.KEYCODE_BUTTON_B:
                                    funcs.onCancel();
                                    ListDialog.this.getDialog().dismiss();
                                    return true;

                                default:
                            }
                        }
                    }
                }
                return false;
            }
        });

        return dialog;
    }

    public static ListDialog makeDialog(Context context, String title, menuItem[] items,
                                        eventListeners funcs) {
        ListDialog dialog = new ListDialog();
        dialog.context = context;
        dialog.title = title;
        dialog.items = items;
        dialog.funcs = funcs;
        return dialog;
    }

    public class itemAdapter extends BaseAdapter {
        private Context mContext;
        private menuItem[] items;
        private LayoutInflater inflater = null;

        public itemAdapter(Context c, menuItem[] list) {
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = c;
            items = list;
        }

        public int getCount() {
            return items.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_large, parent, false);
            }
            Button btnItem = (Button) convertView.findViewById(R.id.item_name);
            btnItem.setText(items[position].title);
            btnItem.setTag(position);
            btnItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg) {
                    funcs.onSelected(position);
                    ListDialog.this.getDialog().dismiss();
                }
            });
            return convertView;
        }
    }
}
