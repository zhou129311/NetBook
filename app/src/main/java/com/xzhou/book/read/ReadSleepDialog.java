package com.xzhou.book.read;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppSettings;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadSleepDialog extends AppCompatDialog {

    @BindView(R.id.count_time_iv)
    TextView mCountTimeIv;

    private Activity mActivity;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    public ReadSleepDialog(Context context) {
        super(context, R.style.DialogTheme);
        mActivity = (Activity) context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_read_sleep, null);
        ButterKnife.bind(this, view);
        setContentView(view);
        setCanceledOnTouchOutside(false);
    }

    public void updateCountTime(long time) {
        CountDownTimer countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCountTimeIv.setText(mDateFormat.format(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                dismiss();
                AppSettings.setStartSleepTime(0);
            }
        };
        countDownTimer.start();
        mCountTimeIv.setText(mDateFormat.format(time));
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @OnClick(R.id.ok_tv)
    public void onViewClicked() {
        dismiss();
        mActivity.finish();
    }
}
