package ir.pay.gateway;

import androidx.appcompat.app.AppCompatActivity;
import ir.pay.gateway.gateway.OnPaymentListener;
import ir.pay.gateway.gateway.OnVerificationListener;
import ir.pay.gateway.gateway.Payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonPayClicked(View view) {
        EditText editTextAmount = findViewById(R.id.editTextAmount);
        String amount = editTextAmount.getText().toString();

        final TextView textViewResponse = findViewById(R.id.textViewResponse);
        textViewResponse.setText("");

        Payment payment = new Payment(getApplicationContext());
        payment.setApi("test");
        payment.setAmount(amount);
        payment.setRedirect("payment-testing://pay.ir");

        payment.Pay(new OnPaymentListener() {
            @Override
            public void onSuccess(Intent intent) {
                startActivity(intent);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onError(int errorCode, String errorMessage) {
                textViewResponse.setText(errorCode + " : " + errorMessage);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        final TextView textViewResponse = findViewById(R.id.textViewResponse);

        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith("payment-testing://pay.ir")) {
            Payment payment = new Payment(getApplicationContext());
            payment.Verify(uri, new OnVerificationListener() {
                @Override
                public void onSuccess(String transId) {
                    textViewResponse.setText(transId);
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onError(int errorCode, String errorMessage) {
                    textViewResponse.setText(errorCode + " : " + errorMessage);
                }
            });
        }
    }
}
