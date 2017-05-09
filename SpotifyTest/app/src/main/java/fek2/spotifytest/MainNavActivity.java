package fek2.spotifytest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;

class SearchSongTask extends AsyncTask<String, Void, String> {
    TextView textView;
    JSONObject searchResults;

    public SearchSongTask(TextView textView, JSONObject searchResults) {
        this.textView = textView;
        this.searchResults = searchResults;
    }

    protected String doInBackground(String... urls) {
        URL url;
        String result = "";
        String https_url = urls[0];
        try {

            url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

            //dump all the content
            if(con!=null){

                try {

                    System.out.println("****** Content of the URL ********");
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));

                    String input;
                    while ((input = br.readLine()) != null){
                        result += input;
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected void onPostExecute(String result) {
        String listResult = "";
        try {
            this.searchResults = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonResult = searchResults.getJSONObject("tracks");
            JSONArray tracks = jsonResult.getJSONArray("items");
            for (int i = 0; i < tracks.length(); i++)
            {
                JSONObject track = tracks.getJSONObject(i);
                String name = (String)track.get("name");
                listResult += name + "\n";

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.textView.setText(listResult);
    }
}

public class MainNavActivity extends AppCompatActivity {

    JSONObject searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);
        final Button searcButton = (Button) findViewById(R.id.searchBtn);
        searcButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String https_url = "https://api.spotify.com/v1/search?q=track:in%20da%20club&type=track&limit=2";
                TextView textView = (TextView) findViewById(R.id.songListTextView);
                new SearchSongTask(textView, searchResults).execute(https_url);
            }
        });
    }
}
