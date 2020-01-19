package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pop.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class nfcTest extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_test);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter.disableReaderMode(this);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] response = new byte[0];
        try {
            response = isoDep.transceive(Utils.hexStringToByteArray("00A4040007A0000002471001"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String stringResponse = "";

        try {
            stringResponse = new String(response, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String finalStringResponse = stringResponse;

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                TextView v = findViewById(R.id.textView);
                v.append("\nCard Response: " + finalStringResponse);
            }
        });
        System.out.println("\nCard Response: " + Utils.toHex(response));

        try {
            isoDep.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
