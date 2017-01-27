package jp.ne.sakura.charilab.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
 * Created by uno on 1/21/17.
 */

public class GridDialog extends DialogFragment {
    private String title;
    private Context context;
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
        final View dialogView = inflater.inflate(R.layout.grid_dialog, null);
        final ImageAdapter adapter = new ImageAdapter(context, items);

        if (title != null) {
            ((TextView) dialogView.findViewById(R.id.gridTitle)).setText(title);
        }
        GridView gv = (GridView) dialogView.findViewById(R.id.gridView);
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                funcs.onSelected(position);
                GridDialog.this.getDialog().dismiss();
            }
        });
        builder.setView(dialogView);
        return builder.create();
    }

    public static GridDialog makeDialog(Context context, String title, menuItem[] items,
                                          eventListeners funcs) {
        GridDialog dialog = new GridDialog();
        dialog.context = context;
        dialog.title = title;
        dialog.items = items;
        dialog.funcs = funcs;
        return dialog;
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private menuItem[] items;

        public ImageAdapter(Context c, menuItem[] list) {
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

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                //imageView.setLayoutParams(new GridView.LayoutParams(256, 256));
                imageView.setLayoutParams(new GridView.LayoutParams(128, 128));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8,8,8,8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(items[position].rid);
            return imageView;
        }
    }
}
