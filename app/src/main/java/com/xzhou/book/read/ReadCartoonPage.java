package com.xzhou.book.read;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.widget.PhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadCartoonPage extends RelativeLayout {

    @BindView(R.id.photo_view)
    PhotoView mPhotoView;
    @BindView(R.id.page_title)
    TextView mPageTitle;
    @BindView(R.id.page_number)
    TextView mPageNumber;
    @BindView(R.id.wifi_view)
    TextView mWifiView;
    @BindView(R.id.battery_view)
    TextView mBatteryView;

    public ReadCartoonPage(Context context) {
        this(context, null);
    }

    public ReadCartoonPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.read_cartoon_page, this);
        ButterKnife.bind(this, view);

    }

    public void setPageContent(CartoonContent content) {
        mPhotoView.setImageBitmap(content.bitmap);
    }

}
