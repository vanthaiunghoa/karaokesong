package kr.ds.karaokesong;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import kr.ds.fragment.BaseFragment;
import kr.ds.fragment.ChannelFragment;
import kr.ds.fragment.ChannelListFragment;
import kr.ds.handler.ChannelHandler;
import kr.ds.handler.ListHandler;

/**
 * Created by Administrator on 2017-06-14.
 */
public class ChannelListActivity extends BaseActivity{

    private BaseFragment mFragment = null;
    private FragmentManager mFm;
    private FragmentTransaction mFt;

    private ChannelHandler mSavedata;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            mSavedata = (ChannelHandler) savedInstanceState.getParcelable("data");
        }else{
            mSavedata = (ChannelHandler) getIntent().getParcelableExtra("data");
        }

        setContentView(R.layout.activity_channel_list);

        mFm = getSupportFragmentManager();
        mFt = mFm.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", mSavedata);
        mFragment = BaseFragment.newInstance(ChannelListFragment.class, bundle);
        mFt.replace(R.id.content_frame, mFragment);
        mFt.commitAllowingStateLoss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("data", mSavedata);
        super.onSaveInstanceState(outState);
    }
}
