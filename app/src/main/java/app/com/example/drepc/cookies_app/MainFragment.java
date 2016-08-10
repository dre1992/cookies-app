package app.com.example.drepc.cookies_app;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class MainFragment extends Fragment {


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    //With the class onCreateView we infalte the fragment's layout and set tha listener for the "pressed button events"
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Button button = (Button) rootView.findViewById(R.id.email_sign_in_button);
        final EditText ed1 = (EditText) rootView.findViewById(R.id.editTextMail);
        final EditText ed2 = (EditText) rootView.findViewById(R.id.editTextPass);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doPost(ed1.getText().toString(), ed2.getText().toString());
            }
        });


        return rootView;
    }
//doPost is an intermediate method to help call the AsyncTask with the correct parameters
    private void doPost(String ed1, String ed2) {
        String[] temp = new String[2];
        temp[0] = ed1;
        temp[1] = ed2;
        doPostHttp httprequest = new doPostHttp();
        httprequest.execute(temp);

    }

//An AsyncTask is used to make the connection to the server and do the http POST requests.
// It takes as parameters the typed email and password and returns a String array with some information and the access token
// that was acquired from the server ("NOTOKEN" otherwise)

// the method onPostExecute shows a toast with information on the success or failure to communicate with the server
// and also stores the token on the sharedPreferences for future use

// With HttpUrlConnection you can only make a single request at a time and it makes it a bit greedy on memory but for the purpose of
// this test is was enough.
//originally i used HttpCline but it is deprecated so i decided to avoid it. Google suggets the use of Volley library but for 
//now HttpUrlConnection was enough and robust enough to make all the requests.

// The session should be close and a new request can be made this time with the token that is stored and the appropriate parameters
//!!!!in a full developed app we should encrypt the token so that is not accessible to malignant use
    public class doPostHttp extends AsyncTask<String[], Void, String[]> {


        @Override
        protected String[] doInBackground(String[]... str) {
            String[] edtxt=str[0];
            //the following code snippet trusts all the certificates for this app.I encountered a problem trying to access the server
            //as  the intermediate certificates were missing and android didnt trust the source
            // This is highly insecure and the correct practice should be to use TrustManager to create a new Keystore and add the certifacate
            //Because this is a more convoluted process i decided to use the simple but unsafe approach for the purpose of testing
            try {
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new X509TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(
                        context.getSocketFactory());
            } catch (Exception e) { // should never happen
                e.printStackTrace();
            }


            String[] result = new String[2];
            // Using httpUrlConnection i set the headers and the mesage body and make a Post request to the server
            // I use the response code and response message to make a toast to the user and inform him of the request status
            // and add the token to the results String array
            // The toast could be made immediately when the response arrives but i considered it to be more straight forward
            //to add it after the processing in doInBackground is finished
            try {
                final String TOKEN = "access_token";
                URL url = new URL("https://dev-api.cookies-app.com/oauth/token");
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("username", edtxt[0]);
                params.put("password", edtxt[1]);
                params.put("grant_type", "password");

                  Log.d("text", String.valueOf(edtxt[0]));
                  Log.d("pass", String.valueOf(edtxt[1]));

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Authorization", "Basic YW5kcm9pZDpzZWNyZXQ=");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.getOutputStream().write(postDataBytes);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();

                    for (int c; (c = in.read()) >= 0; )
                        sb.append((char) c);
                    String response = sb.toString();
                    JSONObject obj = new JSONObject(response);

                    String tok = obj.getString(TOKEN);

                    result[0] = "Value: "+responseCode+"\n"+"Message:"+conn.getResponseMessage();
                    result[1] = tok;


                } else {
                    //
                    String response = conn.getResponseMessage();
                    result[0] ="Value: "+responseCode+"\n"+"Message: "+ response;
                    result[1]="NOTOKEN";
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                return result;
            }


        }
//OnPostExecute handles the toast and the addition of the token to the preferences
        @Override
        protected void onPostExecute(String[] s) {
            if(s!=null){

                Toast.makeText(getActivity(), s[0],
                        Toast.LENGTH_LONG).show();
            }
            if (s[1]!="NOTOKEN") {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("access_token", s[1]);
                editor.commit();
            }
        }
    }
}
