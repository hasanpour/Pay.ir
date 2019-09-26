package ir.pay.gateway.gateway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Payment {

    private Context context;
    private final String sendUrl = "https://pay.ir/pg/send";
    private final String sendUrlShare = "https://pay.ir/pg/share/send";
    private final String redirectUrl = "https://pay.ir/pg/";
    private final String verifyUrl = "https://pay.ir/pg/verify";
    private String api;
    private String amount;
    private String redirect;
    private String mobile;
    private String factorNumber;
    private String description;
    private JSONArray merchants;
    private String validCardNumber;

    public Payment(Context context){
        this.context = context;
    }

    public void setApi(String api) {
        this.api = api;
        SharedPreferences sharedPref =
                context.getSharedPreferences("ir.pay.gateway.gateway.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("APIKey", api);
        editor.apply();
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setFactorNumber(String factorNumber) {
        this.factorNumber = factorNumber;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMerchants(JSONArray merchants) {
        this.merchants = merchants;
    }

    public void setValidCardNumber(String validCardNumber) {
        this.validCardNumber = validCardNumber;
    }

    public void Pay(final OnPaymentListener payListener) {
        final JSONObject parameters = new JSONObject();
        try {
            parameters.put("api", api);
            parameters.put("amount", amount);
            parameters.put("redirect", redirect);
            parameters.put("mobile", mobile);
            parameters.put("factorNumber", factorNumber);
            parameters.put("description", description);
            if(merchants != null && merchants.length() > 0){
                parameters.put("merchants", merchants);
            }
            parameters.put("validCardNumber", validCardNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = sendUrl;
        if(merchants != null && merchants.length() > 0){
            url = sendUrlShare;
        }

        final JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, url, parameters,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    final Uri uri = Uri.parse(redirectUrl + response.getString("token"));
                                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    payListener.onSuccess(intent);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                if(error.networkResponse != null && error.networkResponse.statusCode == 422){
                                    try {
                                        final JSONObject object =  new JSONObject(new String(error.networkResponse.data));
                                        payListener.onError(object.getInt("errorCode"),object.getString("errorMessage"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else if(error instanceof NetworkError || error instanceof TimeoutError) {
                                    payListener.onError(-100, "Cannot connect to Internet");
                                }
                                else if(error instanceof ServerError){
                                    payListener.onError(-101, "Server Error");
                                }
                                else {
                                    payListener.onError(-102, "Unknown Error");
                                }
                            }
                        }
                );
        final RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public void Verify(Uri uri, final OnVerificationListener verifyListener){
        if (api == null){
            SharedPreferences sharedPref =
                    context.getSharedPreferences("ir.pay.gateway.gateway.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
            api = sharedPref.getString("APIKey", "-1");
            if (api.equals("-1")){
                verifyListener.onError(-200, "Something goes wrong");
                return;
            }
        }

        String status = uri.getQueryParameter("status");
        String token = uri.getQueryParameter("token");
        if (status == null || token == null){
            return;
        }

        if (status.equals("0")){
            verifyListener.onError(0, "Failed purchases");
        }
        else if (status.equals("1")){
            final JSONObject parameters = new JSONObject();
            try {
                parameters.put("api", api);
                parameters.put("token", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final JsonObjectRequest request = new JsonObjectRequest
                    (Request.Method.POST, verifyUrl, parameters,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        verifyListener.onSuccess(response.getString("transId"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    if(error.networkResponse != null && error.networkResponse.statusCode == 422){
                                        try {
                                            final JSONObject object =  new JSONObject(new String(error.networkResponse.data));
                                            verifyListener.onError(object.getInt("errorCode"),object.getString("errorMessage"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else if(error instanceof NetworkError || error instanceof TimeoutError) {
                                        verifyListener.onError(-100, "Cannot connect to Internet");
                                    }
                                    else if(error instanceof ServerError){
                                        verifyListener.onError(-101, "Server Error");
                                    }
                                    else {
                                        verifyListener.onError(-102, "Unknown Error");
                                    }
                                }
                            }
                    );
            final RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
        }
    }
}