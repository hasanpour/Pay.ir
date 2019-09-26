package ir.pay.gateway.gateway;

import android.content.Intent;

public interface OnPaymentListener {
    void onSuccess(Intent intent);
    void onError(int errorCode, String errorMessage);
}