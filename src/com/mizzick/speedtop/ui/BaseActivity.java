package com.mizzick.speedtop.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.android.gms.plus.Plus;
import com.mizzick.speedtop.R;
import com.mizzick.speedtop.gameutils.BaseGameActivity;

/**
 * Base activity class
 */
public abstract class BaseActivity extends BaseGameActivity {
	public BaseActivity() {
		super(BaseGameActivity.CLIENT_GAMES | BaseGameActivity.CLIENT_PLUS);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getGameHelper().setPlusApiOptions(
				new Plus.PlusOptions.Builder()
						.addActivityTypes("http://schemas.google.com/AddActivity")
						.build());

		super.onCreate(savedInstanceState);

		//TODO:v3 Not support on api level 10
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			case R.id.quitMenuItem:
				confirmFinish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void finish() {
		super.finish();
		System.exit(0);
	}

	@Override
	public void onSignInFailed() {
		//Toast.makeText(getApplicationContext(), getString(R.string.signInFailedMessage), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSignInSucceeded() {
		//Toast.makeText(getApplicationContext(), getString(R.string.signInSuccessMessage), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Shows confirmation dialog to finish app
	 */
	protected void confirmFinish() {
		new AlertDialog.Builder(this)
				.setMessage(getString(R.string.confirmFinishMessage))
				.setCancelable(true)
				.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				})
				.setNegativeButton(getString(R.string.no), null)
				.show();
	}

	/**
	 * Show specified activity
	 *
	 * @param activityClass desired activity
	 */
	protected void redirectToActivity(Class activityClass) {
		Intent intent = new Intent(getApplicationContext(), activityClass);
		startActivity(intent);
	}
}
