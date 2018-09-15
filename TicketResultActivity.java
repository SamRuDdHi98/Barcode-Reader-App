package info.androidhive.movietickets;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

public class TicketResultActivity extends AppCompatActivity {
    private static final String TAG = TicketResultActivity.class.getSimpleName();

    // url to search barcode
    private static final String URL = "http://192.168.1.186/search.php?code=";

    private TextView txtName, txtProduct, txtBrand, txtCalories, txtWeight, txtPrice, txtError;
    private ImageView imgPoster;
    private Button btnBuy;
    private ProgressBar progressBar;
    private TicketView ticketView;
    private TextToSpeech t1;
    private TextToSpeech t2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        t2=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t2.setLanguage(Locale.UK);
                }
            }
        });


        txtName = findViewById(R.id.name);
        txtProduct = findViewById(R.id.product);
        txtBrand = findViewById(R.id.brand);
        txtPrice = findViewById(R.id.price);
        txtCalories = findViewById(R.id.calories);
        imgPoster = findViewById(R.id.poster);
        txtWeight = findViewById(R.id.weight);
        btnBuy = findViewById(R.id.btn_buy);
        imgPoster = findViewById(R.id.poster);
        txtError = findViewById(R.id.txt_error);
        ticketView = findViewById(R.id.layout_ticket);
        progressBar = findViewById(R.id.progressBar);



        String barcode = getIntent().getStringExtra("code");

        // close the activity in case of empty barcode
        if (TextUtils.isEmpty(barcode)) {
            Toast.makeText(getApplicationContext(), "Barcode is empty!", Toast.LENGTH_LONG).show();
            finish();
        }

        // search the barcode
        searchBarcode(barcode);
    }

    /**
     * Searches the barcode by making http call
     * Request was made using Volley network library but the library is
     * not suggested in production, consider using Retrofit
     */
    private void searchBarcode(String barcode) {
        // making volley's json request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL + barcode, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "Product response: " + response.toString());

                        // check for success status
                        if (!response.has("error")) {
                            // received movie response
                            renderProduct(response);
                        } else {
                            // no movie found
                            showNoProduct();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                showNoProduct();
            }
        });

        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showNoProduct() {
        txtError.setVisibility(View.VISIBLE);
        ticketView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void renderProduct(JSONObject response) {
        try {

            // converting json to product object
            Product prod = new Gson().fromJson(response.toString(), Product.class);

            if (prod != null) {
                txtName.setText(prod.getName());
                t1.speak("Product is "+prod.getName()+"Brand is "+prod.getBrand(), TextToSpeech.QUEUE_FLUSH, null);
                txtProduct.setText(prod.getProduct());
                t2.speak("Price is Rupees"+prod.getPrice(), TextToSpeech.QUEUE_FLUSH, null);
                txtBrand.setText(prod.getBrand());
                txtWeight.setText(prod.getWeight());
                txtCalories.setText("" + prod.getCalories());
                txtPrice.setText(prod.getPrice());
                Glide.with(this).load(prod.getPoster()).into(imgPoster);

                if (prod.isReleased()) {
                    btnBuy.setText(getString(R.string.btn_buy_now));
                    btnBuy.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                }
                ticketView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {

                showNoProduct();
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON Exception: " + e.getMessage());
            showNoProduct();
            Toast.makeText(getApplicationContext(), "Error occurred. Check your LogCat for full report", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // exception
            showNoProduct();
            Toast.makeText(getApplicationContext(), "Error occurred. Check your LogCat for full report", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class Product {
        String name;
        String product;
        String poster;
        String brand;
        String weight;
        String price;
        float calories;

        @SerializedName("released")
        boolean isReleased;

        public String getName() {
            return name;
        }

        public String getProduct() {
            return product;
        }

        public String getPoster() {
            return poster;
        }

        public String getBrand() {
            return brand;
        }

        public String getWeight() {
            return weight;
        }

        public String getPrice() {
            return price;
        }

        public float getCalories() {
            return calories;
        }

        public boolean isReleased() {
            return isReleased;
        }
    }
}
