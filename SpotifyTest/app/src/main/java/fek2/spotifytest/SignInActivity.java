package fek2.spotifytest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.LoginActivity;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
//import com.spotify.sdk.android.player.OperationCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class SignInActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "4acb5304c5ae4bd597166627cd25dd2b";
    private static final String REDIRECT_URI = "lets-party://callback";
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        String message = "Welcome to InTune, the app that allows you and your friends to control a shared playlist!  You need a Spotify account to use this app.";
        final TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(message);
        final Button button = (Button) findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                //AuthenticationClient.openLoginActivity(SignInActivity.this, REQUEST_CODE, request);
            }
        });
    }




}
