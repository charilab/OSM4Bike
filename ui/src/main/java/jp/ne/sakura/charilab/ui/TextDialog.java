package jp.ne.sakura.charilab.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
//import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
 * Created by uno on 15/12/26.
 */
public class TextDialog extends DialogFragment {
    private Context context;
    private String title;
    private eventListeners funcs;
    private InputMethodManager inputMethodManager;

    public interface eventListeners {
        void onDone(String desc);
        void onCancel();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.text_dialog, null);

        if (title != null) {
            ((TextView) dialogView.findViewById(R.id.textTitle)).setText(title);
        }
        final EditText tv = (EditText) dialogView.findViewById(R.id.textDesc);
        inputMethodManager =  (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        Button btnDone = (Button) dialogView.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder)tv.getText();
                funcs.onDone(sb.toString());
                TextDialog.this.getDialog().dismiss();
            }
        });

        Button btnCancel = (Button) dialogView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextDialog.this.getDialog().cancel();
            }
        });

        builder.setView(dialogView);
        return builder.create();
    }

    public static TextDialog makeDialog(Context context, String title, eventListeners funcs) {
        TextDialog dialog = new TextDialog();
        dialog.context = context;
        dialog.title = title;
        dialog.funcs = funcs;
        return dialog;
    }
}
