package com.xzhou.book.ui.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.xzhou.book.R;
import com.xzhou.book.datasource.ZhuiShuSQApi;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({ R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5,
            R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_10 })
    public void onViewClicked(final View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (view.getId()) {
                case R.id.btn_1:
                    ZhuiShuSQApi.get().getBookListTags();
                    break;
                case R.id.btn_2:
                    ZhuiShuSQApi.get().getCategoryList();
                    break;
                case R.id.btn_3:
                    ZhuiShuSQApi.get().getAutoComplete("超级");
                    break;
                case R.id.btn_4:
                    ZhuiShuSQApi.get().getRanking();
                    break;
                case R.id.btn_5:
                    ZhuiShuSQApi.get().getHotWord();
                    break;
                case R.id.btn_6:
                    ZhuiShuSQApi.get().getSearchResult("超级", 0, 20);
                    break;
                case R.id.btn_7:
                    ZhuiShuSQApi.get().searchBooksByAuthor("十二翼黑暗炽天使");
                    break;
                case R.id.btn_8:
                    ZhuiShuSQApi.get().getBookDetail("58232ecbe8464ea22f0a7aa0");
                    break;
                case R.id.btn_9:
                    ZhuiShuSQApi.get().getBookMixAToc("58232ecbe8464ea22f0a7aa0");
                    break;
                case R.id.btn_10:
                    String u = "http://book.my716.com/getBooks.aspx?method=content&bookId=1329871&chapterFile=U_1329871_201707191431258172_1441_2.txt";
                    //OkHttpUtils.get(new HttpRequest(u), null, null);
                    ZhuiShuSQApi.get().getChapterRead(u);
                    break;
                }
            }
        }).start();
    }
}
