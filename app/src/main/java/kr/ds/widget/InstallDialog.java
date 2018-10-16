package kr.ds.widget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import kr.ds.karaokesong.R;


@SuppressLint("ValidFragment")
public class InstallDialog extends DialogFragment implements View.OnClickListener {
    private TextView mTextView1, mTextView2;
    private TextView mTextViewMessage;
    private String mMessage;
    private String mUrl;


    public InstallDialog(String message, String url) {
        // TODO Auto-generated constructor stub
        mMessage = message;
        mUrl = url;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater mLayoutInflater = getActivity().getLayoutInflater();
        View view = mLayoutInflater.inflate(R.layout.install_dialog, null);
        (mTextView1 = (TextView)view.findViewById(R.id.textView1)).setOnClickListener(this);
        (mTextView2 = (TextView)view.findViewById(R.id.textView2)).setOnClickListener(this);
        mTextViewMessage = (TextView)view.findViewById(R.id.textView_message);

        mTextViewMessage.setText(mMessage);
        mBuilder.setView(view);




        return mBuilder.create();
    }

    private void MarketLink(){
        try {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(mUrl)));
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(getActivity(), R.string.popupview_null,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.textView1:
                MarketLink();
                break;
            case R.id.textView2:
                dismiss();
                break;
        }
    }

}
