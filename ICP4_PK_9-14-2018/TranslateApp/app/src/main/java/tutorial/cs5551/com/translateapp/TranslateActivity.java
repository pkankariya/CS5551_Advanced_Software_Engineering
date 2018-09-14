package tutorial.cs5551.com.translateapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslateActivity extends AppCompatActivity {

    String API_URL = "https://api.fullcontact.com/v2/person.json?";
    String API_KEY = "b29103a702edd6a";
    String sourceText;
    TextView outputTextView;
    // Adding spinner variables for language selection
    Spinner spinFrom;
    Spinner spinTo;
    Context mContext;

    // Adding class member variable
    Map<String, String> langToCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        outputTextView = (TextView) findViewById(R.id.txt_Result);

        // Creating spinner variables to list the languages available from translation
        spinFrom = (Spinner) findViewById(R.id.spinFrom);
        spinTo = (Spinner) findViewById(R.id.spinTo);

        // Calling function to display languages list of options available
        displaySupportedLang();

        setSupportActionBar(toolbar);

        //Initializing class member variable
        langToCode = new HashMap<String, String>();
    }

    // Function to display languages within spinner variables created (dropdown list)
    public void displaySupportedLang()
    {
        // Variable to stored the url for language code API
        String langURL = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=trnsl.1.1.20151023T145251Z.bf1ca7097253ff7e.c0b0a88bea31ba51f72504cc0cc42cf891ed90d2&ui=en";

        // Functions to read and translate the user input
        OkHttpClient client = new OkHttpClient();
        try { // attempt to do the following. Catch the error (below) if there's an exception (error).

            // Request sent via langURL variable to make API call
            Request request = new Request.Builder()
                    .url(langURL)
                    .build();

            // Callback the request made resulting in onResponse function trigger upon successful return
            client.newCall(request).enqueue(new Callback() {

                // API call response failed action
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println(e.getMessage());
                }

                // API call response success action
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Variables created to store results obtained from API call
                    final String result = response.body().string();
                    final JSONObject jsonResult;
                    final JSONObject langMap;
                    final Iterator<String> keys;

                    try {
                        // ** ADD RETRIEVAL CODE HERE **
                        jsonResult = new JSONObject(result);
                        langMap = jsonResult.optJSONObject("langs");
                        keys = langMap.keys();

                        // Create array variable to convert keys (iterator data) and store language names as a list of strings
                        List<String> languageList = new ArrayList<>();

                        //Adding values to spinners while accessing the language data for the same
                        while (keys.hasNext())
                        {
                            String key = keys.next();
                            String value = langMap.getString(key);
                            languageList.add(value);
                            langToCode.put(value, key);
                        }

                        // Creating adapter to link the array list of languages to the spinner variable
                        final ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(TranslateActivity.this, android.R.layout.simple_spinner_item, languageList);

                        // Execute this once the UI thread is ready to update.
                        runOnUiThread(new Runnable() {
                            @Override
                            // Updating UI elements
                            public void run() {
                                // Syncing language list to spinner variables
                                spinFrom.setAdapter(languageAdapter);
                                spinTo.setAdapter(languageAdapter);

                                // Set default FROM language to English
                                int sourceLang = languageAdapter.getPosition("English");
                                spinFrom.setSelection(sourceLang);

                                //Set defualt TO language to Hindi
                                int destinationLang = languageAdapter.getPosition("Hindi");
                                spinTo.setSelection(destinationLang);
                            }
                        }); // callback

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }); // callback
        } // external try
        catch (Exception ex) {
            outputTextView.setText(ex.getMessage());
        }
    }

    public void translateText(View v) {
        TextView sourceTextView = (TextView) findViewById(R.id.txt_Email);

        sourceText = sourceTextView.getText().toString();

        // Mapping spinner variable to the language code
        // Source language mapping
        String fromLang = spinFrom.getSelectedItem().toString();
        String fromCode = langToCode.get(fromLang);

        // Destination language mapping
        String toLang = spinTo.getSelectedItem().toString();
        String toCode = langToCode.get(toLang);

        // Updating URL to access language based on user selection thru mapping via spinner variables
        String langURL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20151023T145251Z.bf1ca7097253ff7e.c0b0a88bea31ba51f72504cc0cc42cf891ed90d2&text=" + sourceText +"&" +
                "lang=" + fromCode + "-" + toCode + "&[format=plain]&[options=1]&[callback=set]";

        final String response1 = "";
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                    .url(langURL)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println(e.getMessage());
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final JSONObject jsonResult;
                    final String result = response.body().string();
                    try {
                        jsonResult = new JSONObject(result);
                        JSONArray convertedTextArray = jsonResult.getJSONArray("text");
                        final String convertedText = convertedTextArray.get(0).toString();
                        Log.d("okHttp", jsonResult.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                outputTextView.setText(convertedText);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (Exception ex) {
            outputTextView.setText(ex.getMessage());

        }

    }
    public void logout(View v)
    {
        Intent redirect = new Intent(TranslateActivity.this, LoginActivity.class);
        mContext.startActivity(redirect);
    }
}
