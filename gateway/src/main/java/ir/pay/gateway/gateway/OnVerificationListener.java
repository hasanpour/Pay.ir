package ir.pay.gateway.gateway;

    public interface OnVerificationListener {
        void onSuccess(String transId);
    void onError(int errorCode, String errorMessage);
}
