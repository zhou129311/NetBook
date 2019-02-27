package com.xzhou.book.main;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;
import com.xzhou.book.utils.Log;

import org.json.JSONObject;

public class BaseUiListener implements IUiListener {

    protected void doComplete(JSONObject values) {
    }

    @Override
    public void onComplete(Object o) {
        doComplete((JSONObject) o);
    }

    @Override
    public void onError(UiError e) {
        Log.e("onError:", "code:" + e.errorCode + ", msg:"
                + e.errorMessage + ", detail:" + e.errorDetail);
    }

    @Override
    public void onCancel() {
        Log.i("onCancel", "onCancel");
    }
}
