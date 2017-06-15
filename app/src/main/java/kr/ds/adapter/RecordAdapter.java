package kr.ds.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import kr.ds.data.BaseResultListener;
import kr.ds.db.RecordDB;
import kr.ds.handler.RecordHandler;
import kr.ds.karaokesong.R;
import kr.ds.utils.DsObjectUtils;

/**
 * Created by Administrator on 2016-08-31.
 */
public class RecordAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<RecordHandler> mData;
    private LayoutInflater mInflater;
    private RecordDB mRecordDB;


    public RecordAdapter(Context context, ArrayList<RecordHandler> data) {
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.fragment_list1_item3,null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.circularImageView);
            holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
            holder.textView2 = (TextView) convertView.findViewById(R.id.textView2);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(!DsObjectUtils.isEmpty(mData.get(position).getImage())){


            Glide.with(mContext)
                    .load(mData.get(position).getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        }else{
            holder.imageView.setVisibility(View.GONE);
        }

        if(!DsObjectUtils.isEmpty(mData.get(position).getTitle())){
            holder.textView1.setVisibility(View.VISIBLE);
            holder.textView1.setText(mData.get(position).getTitle());
        }else {
            holder.textView1.setText("");
        }

        if(!DsObjectUtils.isEmpty(mData.get(position).getRegdate())){
            holder.textView2.setVisibility(View.VISIBLE);
            holder.textView2.setText(mData.get(position).getRegdate());
        }else {
            holder.textView2.setText("");
        }






        return convertView;
    }


    class ViewHolder {
        ImageView imageView;
        TextView textView1, textView2;
    }

}