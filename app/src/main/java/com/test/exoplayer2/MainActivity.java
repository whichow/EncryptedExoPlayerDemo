package com.test.exoplayer2;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;

import java.io.File;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

  public static final String AES_ALGORITHM = "AES";
  public static final String AES_TRANSFORMATION = "AES/CTR/NoPadding";

  private static final String ENCRYPTED_FILE_NAME = "encrypted.mp4";

  private static final String key = "c28540d871bd8ea669098540be58fef5";
  private static final String iv = "857d3a5fca54219a068a5c4dd9615afb";

  private Cipher mCipher;
  private SecretKeySpec mSecretKeySpec;
  private IvParameterSpec mIvParameterSpec;

  private File mEncryptedFile;

  private PlayerView mSimpleExoPlayerView;

  private byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
              + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSimpleExoPlayerView = (PlayerView) findViewById(R.id.simpleexoplayerview);

    mEncryptedFile = new File(getExternalFilesDir(""), ENCRYPTED_FILE_NAME);

    SecureRandom secureRandom = new SecureRandom();
//    byte[] key = new byte[16];
//    byte[] iv = new byte[16];
//    secureRandom.nextBytes(key);
//    secureRandom.nextBytes(iv);

    mSecretKeySpec = new SecretKeySpec(hexStringToByteArray(key), AES_ALGORITHM);
    mIvParameterSpec = new IvParameterSpec(hexStringToByteArray(iv));

    try {
      mCipher = Cipher.getInstance(AES_TRANSFORMATION);
      mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private boolean hasFile() {
    return mEncryptedFile != null
        && mEncryptedFile.exists()
        && mEncryptedFile.length() > 0;
  }

  public void encryptVideo(View view) {
    if (hasFile()) {
      Log.d(getClass().getCanonicalName(), "encrypted file found, no need to recreate");
      return;
    }
    try {
      Cipher encryptionCipher = Cipher.getInstance(AES_TRANSFORMATION);
      encryptionCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
      // TODO:
      // you need to encrypt a video somehow with the same key and iv...  you can do that yourself and update
      // the ciphers, key and iv used in this demo, or to see it from top to bottom,
      // supply a url to a remote unencrypted file - this method will download and encrypt it
      // this first argument needs to be that url, not null or empty...
      new DownloadAndEncryptFileTask("https://goindex.olloollo.workers.dev/0:/0a8b21cb072b5ba29c4203b848fe5f23.mp4", mEncryptedFile, encryptionCipher).execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void playVideo(View view) {
    SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
    mSimpleExoPlayerView.setPlayer(player);
    DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec);
    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
    try {
      Uri uri = Uri.fromFile(mEncryptedFile);
      ProgressiveMediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory,
              new DefaultExtractorsFactory()).createMediaSource(uri);
      player.prepare(videoSource);
      player.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
