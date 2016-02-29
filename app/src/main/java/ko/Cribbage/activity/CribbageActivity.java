package ko.Cribbage.activity;

import ko.Cribbage.view.CribbageView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CribbageActivity extends Activity {

	private CribbageView myView;
	private static final int TOGGLE_SOUND = 1;
	private boolean soundEnabled = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myView = new CribbageView(this);
		setContentView(myView);
		myView.setBackgroundColor(0xFF1e571e);

		Bundle extras = getIntent().getExtras();
		if(extras!=null) {
			myView.soundOn = extras.getBoolean("soundbool");
		}
		myView.soundOn = soundEnabled;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem toggleSound = menu.add(0, TOGGLE_SOUND, 0, "Toggle Sound");
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.cribbage, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case TOGGLE_SOUND:
				String soundEnabledText = "Sound On";
				if (soundEnabled) {
					soundEnabled = false;
					myView.soundOn = false;
					soundEnabledText = "Sound Off";
				} else {
					soundEnabled = true;
					myView.soundOn = true;
				}
				Toast.makeText(this, soundEnabledText, Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
	}
}
