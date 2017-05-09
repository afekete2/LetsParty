package fek2.spotifytest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
//import com.spotify.sdk.android.player.OperationCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import static com.spotify.sdk.android.player.PlayerEvent.kSpPlaybackNotifyTrackChanged;

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}

public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{

    private boolean isPlaying;
    private static final String CLIENT_ID = "4acb5304c5ae4bd597166627cd25dd2b";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "lets-party://callback";

    private Player mPlayer;
    Handler handler = new Handler();
    Handler imageHandler = new Handler();
    Thread progressThread = null;
    ProgressBar progress;
    boolean newSong = false;
    boolean loggedIn = false;

    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isPlaying = false;
        final Button loginButton = (Button) findViewById(R.id.loginBtn);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        loginButton.setText("Log Out");

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPlaying) {
                    mPlayer.pause(null);
                    setPlayButton();
                    isPlaying = false;
                }

                else {
                    mPlayer.resume(null);
                    setPauseButton();
                    isPlaying = true;
                }
                setSongInfo();
            }
        });

        final Button searcButton = (Button) findViewById(R.id.button2);
        searcButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MainNavActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }

    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyPlay:

            case kSpPlaybackNotifyTrackChanged:
                setSongInfo();
                if (progressThread != null && progressThread.isAlive()) {
                    progressThread.interrupt();
                    progressThread = null;
                }
                runProgressBar();

            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        mPlayer.playUri(null, "spotify:user:egovani:playlist:3Ehcxx56EC0BasBrTkFxyT", 0, 0);
        mPlayer.pause(null);
        loggedIn = true;
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }


    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public void PreviousTrack(View view) {
        newSong = true;
        mPlayer.skipToPrevious(null);
        setPauseButton();
    }
    public void NextTrack(View view) {
        newSong = true;
        mPlayer.skipToNext(null);
        setPauseButton();
    }

    public void LogInOut(View view) {
        final Button button = (Button) findViewById(R.id.loginBtn);
        if (!loggedIn) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN,
                    REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            button.setText("Log Out");
        }
        else {
            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

            builder.setScopes(new String[]{"streaming"});
            builder.setShowDialog(true);
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

            // TODO: determine if user actually logged out in onActivityResult function
            button.setText("Log In");
        }
    }


    public void setSongInfo() {
        Metadata metadata = mPlayer.getMetadata();
        TextView song = (TextView) findViewById(R.id.songTextView);
        song.setText(metadata.currentTrack.name);
        TextView artist = (TextView) findViewById(R.id.artistTextView);
        artist.setText(metadata.currentTrack.artistName);
        setAlbumCover(metadata.currentTrack.albumCoverWebUrl);
    }

    public void runProgressBar() {
        newSong = false;
        progress = (ProgressBar) findViewById(R.id.progressBar);
        long temp =  mPlayer.getMetadata().currentTrack.durationMs;
        final int length = (int)temp;
        progress.setMax(length);

        progressThread = new Thread(new Runnable() {
            long progressStatus = mPlayer.getPlaybackState().positionMs;
            long max =  mPlayer.getMetadata().currentTrack.durationMs;
            public void run() {
                while (progressStatus <= max) {
                    max =  mPlayer.getMetadata().currentTrack.durationMs;
                    progressStatus = mPlayer.getPlaybackState().positionMs;
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Update the progress bar
                    handler.post(new Runnable() {
                        public void run() {
                            progress.setProgress((int)progressStatus);
                        }
                    });
                }
            }

        });
        progressThread.start();
    }

    public void setPlayButton() {
        final Button button = (Button) findViewById(R.id.button);
        Drawable myIcon = getResources().getDrawable( R.drawable.ic_media_play);
        button.setBackgroundDrawable(myIcon);
    }

    public void setPauseButton() {
        final Button button = (Button) findViewById(R.id.button);
        Drawable myIcon = getResources().getDrawable( R.drawable.ic_media_pause);
        button.setBackgroundDrawable(myIcon);
    }

    public void setAlbumCover(String uri) {
        final ImageView imageView;
        imageView = (ImageView) findViewById(R.id.albumCover);
        //String url = mPlayer.getMetadata().currentTrack.albumCoverWebUrl;
        new DownloadImageTask(imageView).execute(uri);
    }
}
