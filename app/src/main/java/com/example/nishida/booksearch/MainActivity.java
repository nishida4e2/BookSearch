package com.example.nishida.booksearch;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import android.app.ProgressDialog;
import android.os.AsyncTask;

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
    ArrayAdapter<String> adapter;
    String inputTxt;
    String res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        ListView lv = (ListView)findViewById(R.id.listViewBooks);
        ArrayList<String> arrayBooks = new ArrayList<>();
        arrayBooks.addAll(Arrays.asList(getResources().getStringArray(R.array.array_test)));

        adapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                arrayBooks);
        lv.setAdapter(adapter);
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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

    //ToDo
    public void btn_Click(View v) {

        //Toast.makeText(getApplicationContext(), inputTxt, Toast.LENGTH_LONG).show();
        if (inputTxt==null){
            inputTxt = "";
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        new TaskBookSearch().execute(inputTxt);

    }

    public class TaskBookSearch extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgressDialog = ProgressDialog.show(MainActivity.this, "しばらくお待ちください", "検索中...", true);
        }

        String retTxt;

        @Override
        protected Integer doInBackground(String... params) {

            //Map<String, String> param = new HashMap<>();

            try {

                // 取得したテキストを格納する変数
                final StringBuilder result = new StringBuilder();
                // アクセス先URL
                final URL url = new URL(Common.SERVICE_API_URL + params[0]);

                HttpURLConnection con = null;
                try {
                    // ローカル処理
                    // コネクション取得
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();

                    // HTTPレスポンスコード
                    final int status = con.getResponseCode();
                    if (status == HttpURLConnection.HTTP_OK) {
                        // 通信に成功した
                        // テキストを取得する
                        final InputStream in = con.getInputStream();
                        //final String encoding = con.getContentEncoding();
                        //final InputStreamReader inReader = new InputStreamReader(in, encoding);
                        final InputStreamReader inReader = new InputStreamReader(in);
                        final BufferedReader bufReader = new BufferedReader(inReader);
                        String line = null;
                        // 1行ずつテキストを読み込む
                        while((line = bufReader.readLine()) != null) {
                            result.append(line);
                        }

                        bufReader.close();
                        inReader.close();
                        in.close();
                    }

                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (ProtocolException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    if (con != null) {
                        // コネクションを切断
                        con.disconnect();
                    }
                }

                res = result.toString();

                return 1;
            } catch (Exception e) {
                return Common.RESULT_ERROR;
            }
        }



        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            m_ProgressDialog.dismiss();
            /*
            if(Common.RESULT_SUCCESS == result) {

            } else {

            }
            */

            if ("".equals(res)){
                Toast.makeText(getApplicationContext(), "インターネットに接続されていません。接続を確認してから再度お試しください。", Toast.LENGTH_LONG).show();
            }

            try{
                parseXml(res);
            }catch (Exception e){

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
                //arrayBooks.add("検索ワード「"+inputTxt+"」に一致する書籍は見つかりませんでした。別の検索ワードをお試しください。");
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
