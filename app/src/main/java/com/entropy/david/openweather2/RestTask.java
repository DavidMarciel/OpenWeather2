package com.entropy.david.openweather2;

/**
 * Created by david on 2/04/2016.
 */
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class RestTask extends AsyncTask<HttpUriRequest, Void, String>
{
    private Context mContext;
    private HttpClient mClient;
    private String mAction;

    public RestTask(Context context, String action)
    {
        mContext = context;
        mAction = action;
        mClient = new DefaultHttpClient();
    }

    @Override
    protected String doInBackground(HttpUriRequest... params)
    {
        try
        {
            //get the requested info
            //from the server
            HttpUriRequest request = params[0];
            HttpResponse serverResponse = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            return handler.handleResponse(serverResponse);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result)
    {
        Log.d("AsyncTask response:", result);

        //continue with the program
        Intent intent = new Intent(mAction);
        intent.putExtra("httpResponse", result);
        mContext.sendBroadcast(intent);
    }

}
