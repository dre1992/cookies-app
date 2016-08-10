package app.com.example.drepc.cookies_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
// The MainActivity class just inflates its view. Its customary not to bulk up the main thread with code as it slows down the UI
//and makes the app unresponsive. For that reason a Fragment is used to deal with the complex code and set the UI layout.
//The Fragment is connected to the MainActivity through the activity's layout
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
