package com.termux;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import com.termux.app.TermuxActivity;

public class MainActivity extends Activity {

    public static MainActivity activity;
    
    public AlertDialog alertDialog;

    public Button btnConsole;
    public Button btnInfo;
    public Button btnNodeRed;
    public Button btnDashboard;

    private Intent info_intent;
    private Intent node_red;

    public static boolean firstRender = true;
    public static boolean enableNodeRed = false;
    public static String ActualActivity = "MainActivity";

    // This allows Termux to enable the buttons on MainActivity once the installation is completed
    public void enableButtons() {
        MainActivity.enableNodeRed = true;

        if (btnNodeRed != null && !btnNodeRed.isEnabled() && btnDashboard != null && !btnDashboard.isEnabled()) {
            try{
                // Wait a little longer to allow the node-red server to fully start
                Thread.sleep(5000);

                btnNodeRed.setEnabled(true);
                btnNodeRed.refreshDrawableState();

                btnDashboard.setEnabled(true);
                btnDashboard.refreshDrawableState();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Allows enableButtons to rerun when the page is resumed
        activity = this;
        if (enableNodeRed) {
            this.enableButtons();
            if (!btnConsole.isEnabled()) btnConsole.setEnabled(true);
        }

        // Enables the console button if Termux is installed
        if (TermuxActivity.installed && !btnConsole.isEnabled()) btnConsole.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        // Remembers the last activity used, in this case the MainActivity
        ActualActivity = "MainActivity";

        // This minimizes the app without closing it when pressing back on the main screen
        minimizeApp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        node_red = new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:1880"));
        btnNodeRed = (Button) findViewById(R.id.btn_node_red);

        btnNodeRed.setOnClickListener(v -> startActivity(node_red));
        btnNodeRed.setEnabled(false);

        btnDashboard = (Button) findViewById(R.id.btn_dashboard);
        btnDashboard.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:1880/ui/"))));
        btnDashboard.setEnabled(false);

        btnConsole = (Button) findViewById(R.id.btn_console);
        btnConsole.setOnClickListener(v -> {
            ActualActivity = "TermuxConsole";
            moveToConsole();
        });
        btnConsole.setEnabled(false);

        info_intent = new Intent(this, InfoActivity.class);
        btnInfo = (Button) findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(v -> startActivity(info_intent));

        if(firstRender){
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Installation");
            alertDialog.setMessage("All the necessary packages are being installed, this process takes a couple of minutes.\n\nYou will be notified with a vibration and a toast message everytime the installation progresses.\n\nIn the meantime you can use the Termux console to check the ongoing installation logs or read the information page for more insights.");
            alertDialog.show();
        }
        firstRender = false;
    }

    public void moveToConsole() {
        super.onBackPressed();
    }

    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    protected void onDestroy() {
        TermuxActivity.first_activity_options = false;
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        super.onStop();
    }
}
