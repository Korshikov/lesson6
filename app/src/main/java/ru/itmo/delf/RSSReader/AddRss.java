package ru.itmo.delf.RSSReader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Created by delf on 11.11.14.
 */
public class AddRss extends Activity {



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_dialog);

        final DBAdapter myDBAdapter= new DBAdapter(this);
        myDBAdapter.open();


        (findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = ((EditText) findViewById(R.id.channelurl)).getText().toString().trim();
                String name = ((EditText) findViewById(R.id.name)).getText().toString().trim();
                try {
                    URL check = new URL(url);
                    check.toURI();
                } catch (MalformedURLException e) {
                    Toast.makeText(view.getContext(), "URL invalid", Toast.LENGTH_LONG).show();
                    return;
                } catch (URISyntaxException e) {
                    Toast.makeText(view.getContext(), "URL invalid", Toast.LENGTH_LONG).show();
                    return;
                }
                if ("".equals(name)) {
                    Toast.makeText(view.getContext(), "name invalid", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent();
                //intent.putExtra("url", url);
                //intent.putExtra("name", name);

                myDBAdapter.createRssTable(myDBAdapter.createRss(name,url,"never"));
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }
}