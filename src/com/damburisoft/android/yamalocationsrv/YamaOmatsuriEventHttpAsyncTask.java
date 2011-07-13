package com.damburisoft.android.yamalocationsrv;

import java.util.List;

import com.damburisoft.android.yamalocationsrv.model.OmatsuriEvent;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriEventUpdateListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class YamaOmatsuriEventHttpAsyncTask extends AsyncTask<Void, String, List<OmatsuriEvent>> {
    
    private OmatsuriEventUpdateListener mListener;
    private ProgressDialog mProgressDialog;
    

    public YamaOmatsuriEventHttpAsyncTask(OmatsuriEventUpdateListener listener) {
        mListener = listener;
    }

    @Override
    protected List<OmatsuriEvent> doInBackground(Void... arg0) {
        YamaInfoHttpClient client = new YamaInfoHttpClient(mListener.getActivity().getBaseContext());
        publishProgress("Getting Event List from Server...");
        return client.getEvents();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mProgressDialog.dismiss();
    }

    @Override
    protected void onPostExecute(List<OmatsuriEvent> result) {
        super.onPostExecute(result);
        String toastMsg;
        if (result == null) {
            toastMsg = "Fail to get Event List";
        } else {
            toastMsg = "Success to get Event List";
        }
        Toast.makeText(mListener.getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
        mProgressDialog.dismiss();
        mListener.updateEvents(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        final Context context = mListener.getActivity();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("In Processing...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        mProgressDialog.setMessage(values[0]);
    }
    
}
