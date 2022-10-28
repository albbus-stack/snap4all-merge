package com.termux;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.termux.app.TermuxActivity;
import com.termux.app.TermuxService;

import java.io.File;

public class MainActivity extends Activity {

    public static MainActivity activity;
    
    public AlertDialog alertDialog;

    public static TextView text_welcome;
    
    public Button btnConsole;
    public Button btnInfo;
    public static Button btnNodeRed;
    public static Button btnDashboard;

    private Intent info_intent;
    private Intent console;
    private Intent node_red;
    
    public static boolean enableNodeRed = false;
    public static String ActualActivity = "MainActivity";

    // This allows Termux to enable the buttons on MainActivity once the installation is completed
    public void enableButtons() {
        Log.d("termux", "EnableButtons is being called");

        MainActivity.enableNodeRed = true;

        if (btnNodeRed != null && !btnNodeRed.isEnabled()) {
            try {
                btnNodeRed.setEnabled(true);
            } catch (Exception e) {
                Log.d("termux", "Exception from enabling btnNodeRed:" + e.getMessage());
            }

            try {
                btnNodeRed.refreshDrawableState();
            } catch (Exception e) {
                Log.d("termux", "Exception from refreshing btnNodeRed:" + e.getMessage());
            }
        }

        if (btnDashboard != null && !btnDashboard.isEnabled()) {
            try {
                btnDashboard.setEnabled(true);
            } catch (Exception e) {
                Log.d("termux", "Exception from enabling btnDashboard:" + e.getMessage());
            }

            try {
                btnDashboard.refreshDrawableState();
            } catch (Exception e) {
                Log.d("termux", "Exception from refreshing btnDashBoard:" + e.getMessage());
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        node_red = new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:1880"));
        btnNodeRed = (Button) findViewById(R.id.btn_node_red);

        btnNodeRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(node_red);
            }
        });
        btnNodeRed.setEnabled(false);

        btnDashboard = (Button) findViewById(R.id.btn_dashboard);
        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:1880/ui/")));
            }
        });
        btnDashboard.setEnabled(false);

        btnConsole = (Button) findViewById(R.id.btn_console);
        btnConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActualActivity = "TermuxConsole";
                moveToConsole();
            }
        });
        btnConsole.setEnabled(false);

        info_intent = new Intent(this, InfoActivity.class);
        btnInfo = (Button) findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(info_intent);
            }
        });

        File rebooted = new File(TermuxService.HOME_PATH + "/rebooted");
        if (!rebooted.exists()) {
            btnNodeRed.setVisibility(View.INVISIBLE);
            btnDashboard.setVisibility(View.INVISIBLE);
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Information");
            alertDialog.setMessage("To finalize the installation of Snap4all the device must be restarted.\n\nWithout restarting you can use the Termux Console or read the Information page.\n\n-> If the Termux Console is disabled please check your internet connection, then close and reopen the app before restarting.");
            try {
                alertDialog.show();
            } catch (WindowManager.BadTokenException e) {
                // Activity already dismissed - ignore.
            }
        }
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
