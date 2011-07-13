package com.damburisoft.android.yamalocationsrv;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.damburisoft.android.yamalocationsrv.model.OmatsuriRole;
import com.damburisoft.android.yamalocationsrv.model.OmatsuriRoleUpdateListener;

public class YamaOmatsuriRoleHttpAsyncTask extends AsyncTask<Integer, String, List<OmatsuriRole>> {
    
    private OmatsuriRoleUpdateListener mListener;
    private ProgressDialog mProgressDialog;
    private int mEvent_id;
    

    public YamaOmatsuriRoleHttpAsyncTask(OmatsuriRoleUpdateListener listenner) {
        super();
        this.mListener = listenner;
    }

    @Override
    protected List<OmatsuriRole> doInBackground(Integer... event_ids) {
        YamaInfoHttpClient client = new YamaInfoHttpClient(mListener.getActivity().getBaseContext());
        publishProgress("Getting Role List from Server...");
        mEvent_id = event_ids[0];
        return client.getRoles(mEvent_id);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mProgressDialog.dismiss();
    }

    @Override
    protected void onPostExecute(List<OmatsuriRole> result) {
        super.onPostExecute(result);
        String toastMsg;
        if (result == null) {
            toastMsg = "Fail to get Role List";
        } else {
            toastMsg = "Success to get Role List";
        }
        Toast.makeText(mListener.getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
        mProgressDialog.dismiss();
        mListener.updateRoles(mEvent_id, result);
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
