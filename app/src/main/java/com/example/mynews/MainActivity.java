package com.example.mynews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
ListView listView;
ArrayAdapter adapter;
ArrayList<String> titleArrayList=new ArrayList<>();
static ArrayList<String> urlArrayList=new ArrayList<>();
SQLiteDatabase newsDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView)findViewById(R.id.listView);
        adapter=new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,titleArrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(MainActivity.this,NewsActivity.class);
                intent.putExtra("pos",position);
                startActivity(intent);
            }
        });

        newsDB=this.openOrCreateDatabase("NewsArticles",MODE_PRIVATE,null);
        newsDB.execSQL("CREATE TABLE IF NOT EXISTS newsArticle (id INTEGER PRIMARY KEY , articleid INTEGER , title VARCHAR , content VARCHAR)");

        updateListView();

        DownloadTask task=new DownloadTask();
        task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");


    }
    public void updateListView(){
        Cursor c=newsDB.rawQuery("SELECT * FROM newsArticle",null);
        int content_index=c.getColumnIndex("content");
        int title_index=c.getColumnIndex("title");
        if(c.moveToFirst()){
            titleArrayList.clear();
            urlArrayList.clear();
            c.moveToFirst();
            do{
                titleArrayList.add(c.getString(title_index));
                urlArrayList.add(c.getString(content_index));
            }
            while (c.moveToNext());
            adapter.notifyDataSetChanged();
        }


    }
    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            String res="";
            URL url;
            HttpURLConnection connection;
            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char cc = (char) data;
                    res += cc;
                    data = reader.read();
                }
                //Log.i( "resContent",res);
                JSONArray jsonArray=new JSONArray(res);
                int length=30;
                if(length>jsonArray.length()){
                    length=jsonArray.length();
                }
                newsDB.execSQL("DELETE FROM newsArticle");
                for(int i=0;i<length;i++) {
                    String articleId = jsonArray.getString(i);
                    //https://hacker-news.firebaseio.com/v0/item/8863.json?print=pretty
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                    connection = (HttpURLConnection) url.openConnection();
                    inputStream = connection.getInputStream();
                    reader = new InputStreamReader(inputStream);
                    data = reader.read();
                    String articleInfo = "";
                    while (data != -1) {
                        char cc = (char) data;
                        articleInfo += cc;
                        data = reader.read();
                    }
                    //Log.i("articleContent",articleInfo);

                    JSONObject jsonObject = new JSONObject(articleInfo);
                    if (!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        String article_title = jsonObject.getString("title");
                        String article_url = jsonObject.getString("url");
                         Log.i("article_title",article_title);
                         Log.i("article_url",article_url);

                        String sql="INSERT INTO newsArticle (articleid , title  , content) VALUES (? , ? , ?)";
                        SQLiteStatement statement=newsDB.compileStatement(sql);
                        statement.bindString(1,articleId);
                        statement.bindString(2,article_title);
                        statement.bindString(3,article_url);
                        statement.execute();

                    }
                }


            }catch (MalformedURLException e) {
                Log.e("malformed Exception:","in-> do in background",e );
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("IO Exception:","in-> do in background",e );
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateListView();
        }
    }

}