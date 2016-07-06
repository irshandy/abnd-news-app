package com.example.android.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NewsFragment extends Fragment {
    ArrayList<Article> mArticleList = new ArrayList<Article>();
    private ArticleAdapter mArticleAdapter;

    public NewsFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        new FetchNewsDataTask().execute("Android");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        final TextView noDataView = (TextView) rootView.findViewById(R.id.no_data_text_view);


        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
        } else {
            Toast.makeText(getActivity(), "No Internet Connection Available", Toast.LENGTH_SHORT).show();
            noDataView.setVisibility(View.VISIBLE);
        }

        mArticleAdapter = new ArticleAdapter(getActivity(), mArticleList);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_article);
        listView.setAdapter(mArticleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String articleUrl = mArticleAdapter.getItem(position).getWebUrl();

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                startActivity(browserIntent);
            }
        });

        return rootView;
    }

    public class FetchNewsDataTask extends AsyncTask<String, Void, ArrayList<Article>> {
        private final String LOG_TAG = FetchNewsDataTask.class.getSimpleName();

        private ArrayList<Article> fetchJSON(String result) throws JSONException {
            ArrayList<Article> articleList = new ArrayList<Article>();

            JSONObject object = new JSONObject(result);
            JSONObject responseData = object.getJSONObject("response");
            JSONArray array = responseData.getJSONArray("results");

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);

                String title = item.getString("webTitle");
                String webUrl = item.getString("webUrl");

                JSONObject fields = item.getJSONObject("fields");
                String snippet = fields.getString("standfirst");
                String thumbnailUrl = fields.getString("thumbnail");

                Article article = new Article(title, snippet, thumbnailUrl, webUrl);
                articleList.add(article);
            }

            for (Article i : articleList) {
                Log.v(LOG_TAG, "Entries: " + i.getTitle());
            }
            return articleList;
        }

        @Override
        protected ArrayList<Article> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String dataJsonStr = null;

            try {
                final String BASE_URL = "http://content.guardianapis.com/search";
                final String QUERY_PARAM = "q";
                final String SHOW_FIELDS_PARAM = "show-fields";
                final String KEY_PARAM = "api-key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, "Android")
                        .appendQueryParameter(SHOW_FIELDS_PARAM, "thumbnail,standfirst")
                        .appendQueryParameter(KEY_PARAM, BuildConfig.GUARDIAN_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                dataJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return fetchJSON(dataJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Article> articles) {
            if (articles != null) {
                mArticleAdapter.clear();
                for (Article a : articles) {
                    mArticleAdapter.add(a);
                }
            }
        }
    }
}
