package com.xzhou.book.read;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.xzhou.book.R;
import com.xzhou.book.utils.AppSettings;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadSleepDialog extends AppCompatDialog {

    @BindView(R.id.count_time_iv)
    TextView mCountTimeIv;

    private ReadActivity mActivity;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public ReadSleepDialog(Context context) {
        super(context, R.style.DialogTheme);
        mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        mActivity = (ReadActivity) context;
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
                mActivity.resetFirstReadTime();
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
        mActivity.onBackPressed();
    }
}
