package com.example.nishida.booksearch;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog m_ProgressDialog;
    private String inputTxt;
    private String retXml;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (inputTxt==null)inputTxt = "";
                new TaskBookSearch().execute(inputTxt);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                inputTxt = newText;
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public class TaskBookSearch extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgressDialog = ProgressDialog.show(MainActivity.this, "しばらくお待ちください", "検索中...", true);
        }
        @Override
        protected Integer doInBackground(String... params) {
            try {
                final StringBuilder result = new StringBuilder();
                final URL url = new URL(Common.SERVICE_API_URL + params[0]);
                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();
                    final int status = con.getResponseCode();
                    if (status == HttpURLConnection.HTTP_OK) {
                        final InputStream in = con.getInputStream();
                        final InputStreamReader inReader = new InputStreamReader(in);
                        final BufferedReader bufReader = new BufferedReader(inReader);
                        String line = null;
                        while((line = bufReader.readLine()) != null) {
                            result.append(line);
                        }
                        bufReader.close();
                        inReader.close();
                        in.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) con.disconnect();
                }
                retXml = result.toString();
                return Common.RESULT_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                return Common.RESULT_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            m_ProgressDialog.dismiss();
            if (Common.RESULT_ERROR == result || "".equals(retXml)){
                Toast.makeText(getApplicationContext(), "インターネットに接続されていません。接続を確認してから再度お試しください。", Toast.LENGTH_LONG).show();
            }
            try{
                parseXml(retXml);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void parseXml(String xmlStr) throws XmlPullParserException, IOException {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlStr));
            int eventType = parser.getEventType();
            String elementName;
            setContentView(R.layout.activity_main);
            ListView lv = (ListView)findViewById(R.id.listViewBooks);
            ArrayList<String> arrayBooks = new ArrayList<>();
            int cnt = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    elementName = parser.getName();
                    if(elementName.equals("title")){
                        eventType = parser.next();
                        if(eventType == XmlPullParser.TEXT){
                            if(cnt!=0) arrayBooks.add(parser.getText());
                            cnt++;
                        }
                    }
                }
                eventType = parser.next();
            }
            if(cnt==1){
                Toast.makeText(getApplicationContext(), "検索ワード「"+inputTxt+"」に一致する書籍は見つかりませんでした。別の検索ワードをお試しください。", Toast.LENGTH_LONG).show();
            }

            adapter = new ArrayAdapter<>(
                    MainActivity.this,
                    android.R.layout.simple_list_item_1,
                    arrayBooks);
            lv.setAdapter(adapter);
        }
    }
}
