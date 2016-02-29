package ko.Cribbage.activity;

/**
 * Created by Speck on 11/11/2015.
 */

import ko.Cribbage.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class RulesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cribbage, menu);
        return true;
    }

}