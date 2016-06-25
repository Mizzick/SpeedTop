package com.mizzick.speedtop.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.mizzick.speedtop.Constants;
import com.mizzick.speedtop.IBaseGpsListener;
import com.mizzick.speedtop.R;
import com.mizzick.speedtop.UnitConverter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.Locale;

/**
 * Main activity class
 *
 * TODO:v2 Think about current vehicle type top speed. How to show it on main screen?
 * TODO:v2 Make Reset session button instead of Profile?
 * TODO:v2 Add landscape layout
 */
public class MainActivity extends BaseActivity implements IBaseGpsListener {

	private boolean useMetric = true;
	private String kmInHourPostText;
	private String milesInHourPostText;

	private float tripSpeed = 0;
	private float topSpeed = 0;

	SharedPreferences preferences;

	private static final int[] WHITE_DIGITS = new int[]{
			R.drawable.digit_0,
			R.drawable.digit_1,
			R.drawable.digit_2,
			R.drawable.digit_3,
			R.drawable.digit_4,
			R.drawable.digit_5,
			R.drawable.digit_6,
			R.drawable.digit_7,
			R.drawable.digit_8,
			R.drawable.digit_9
	};

	private static final int[] YELLOW_DIGITS = new int[]{
			R.drawable.y_digit_0,
			R.drawable.y_digit_1,
			R.drawable.y_digit_2,
			R.drawable.y_digit_3,
			R.drawable.y_digit_4,
			R.drawable.y_digit_5,
			R.drawable.y_digit_6,
			R.drawable.y_digit_7,
			R.drawable.y_digit_8,
			R.drawable.y_digit_9
	};

	private static final int[] RED_DIGITS = new int[]{
			R.drawable.r_digit_0,
			R.drawable.r_digit_1,
			R.drawable.r_digit_2,
			R.drawable.r_digit_3,
			R.drawable.r_digit_4,
			R.drawable.r_digit_5,
			R.drawable.r_digit_6,
			R.drawable.r_digit_7,
			R.drawable.r_digit_8,
			R.drawable.r_digit_9
	};

	private static final int[] CURRENT_DIGIT_VIEWS = new int[]{
			R.id.currentDigitOneView,
			R.id.currentDigitTwoView,
			R.id.currentDigitThreeView,
	};

	private static final int[] TRIP_DIGIT_VIEWS = new int[]{
			R.id.tripDigitOneView,
			R.id.tripDigitTwoView,
			R.id.tripDigitThreeView,
	};

	private static final int[] TOP_DIGIT_VIEWS = new int[]{
			R.id.topDigitOneView,
			R.id.topDigitTwoView,
			R.id.topDigitThreeView,
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);

		if (getResources().getBoolean(R.bool.portrait_only)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//TODO:v3 Override kmh/mph with locale specific value.

		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		bindButtons();

		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

		kmInHourPostText = getString(R.string.kmInHourPostText);
		milesInHourPostText = getString(R.string.milesInHourPostText);

		refreshProfileInfoView();
		refreshSpeedometerView();
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshSpeedometerView();
		refreshProfileInfoView();
	}

	/**
	 * Binds controls callbacks
	 */
	private void bindButtons() {
		final Button profileButton = (Button) findViewById(R.id.profileButton);
		profileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				redirectToActivity(ProfileActivity.class);
			}
		});

		final Button shareButton = (Button) findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sharePost();
			}
		});

		final Button leaderboardButton = (Button) findViewById(R.id.leaderboardButton);
		leaderboardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSignedIn()) {
					submitTopSpeed();
					showLeaderboards();
				} else {
					if (preferences.getBoolean(Constants.GOOGLE_SIGNIN_CONFIRMED, false)) {
						mHelper.onStart(MainActivity.this);
						submitTopSpeed();
						showLeaderboards();
					} else {
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(getString(R.string.pleaseSigninLeaderboardMessage))
								.setCancelable(true)
								.setPositiveButton(getString(R.string.signinMessage), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										final SharedPreferences.Editor editor = preferences.edit();
										editor.putBoolean(Constants.GOOGLE_SIGNIN_CONFIRMED, true);
										editor.commit();

										mHelper.onStart(MainActivity.this);
										beginUserInitiatedSignIn();
									}
								})
								.setNegativeButton(getString(R.string.cancelMessage), null)
								.setTitle(getString(R.string.pleaseSigninTitle))
								.show();
					}

					//Toast.makeText(getApplicationContext(), getString(R.string.pleaseSignUpMessage), Toast.LENGTH_SHORT);
				}
			}
		});

		final View profileInfoFragment = findViewById(R.id.profileInfoFragment);
		profileInfoFragment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				redirectToActivity(ProfileActivity.class);
			}
		});

		final View tripSpeedDigitsView = findViewById(R.id.tripSpeedDigitsView);
		tripSpeedDigitsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetTripSpeed();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		if(isNetworkAvailable() &&
				preferences.getBoolean(Constants.GOOGLE_SIGNIN_CONFIRMED, false)) {
			mHelper.onStart(MainActivity.this);
		}
	}

	/**
	 * Refreshes profile info controls from preferences
	 */
	private void refreshProfileInfoView() {
		final TextView pilotInfoTextView = (TextView) findViewById(R.id.pilotInfoTextView);
		pilotInfoTextView.setText(preferences.getString(Constants.PREF_PILOT_NAME, getString(R.string.pilotDefaultName)));

		final String vehicleInfo = preferences.getString(Constants.PREF_VEHICLE_NAME, getString(R.string.vehicleModelDefaultName));
		final String vehicleType = preferences.getString(Constants.PREF_VEHICLE_TYPE, "Other");
		int resId = getResources().getIdentifier("pref_vehicle_type_" + vehicleType.toLowerCase(), "string", getPackageName());
		final String vehicleTypeString = getResources().getString(resId);

		final TextView vehicleInfoTextView = (TextView) findViewById(R.id.vehicleInfoTextView);
		vehicleInfoTextView.setText("{0} ({1})".replace("{0}", vehicleInfo).replace("{1}", vehicleTypeString));
	}

	/**
	 * Refreshes speed text views
	 */
	private void refreshSpeedometerView() {
		useMetric = preferences.getBoolean(Constants.PREF_USE_METRIC_UNITS, true);

		topSpeed = preferences.getFloat(Constants.PREF_TOP_SPEED, 0);
		updateTopSpeed();
		updateTripSpeed();

		updateSpeed(null);
	}

	/**
	 * Shows leaderboard
	 */
	private void showLeaderboards() {
		if (!isSignedIn()) {
			return;
		}

		//TODO:v2 Add popup with vehicle types leaderboards
		startActivityForResult(
				Games.Leaderboards.getLeaderboardIntent(getApiClient(),
						useMetric ? Constants.LEADERBOARD_KMH_ID : Constants.LEADERBOARD_MPH_ID), 1);
	}


	public void onLocationChanged(Location location) {
		if (location != null) {
			this.updateSpeed(location);
		}
	}

	public void onProviderDisabled(String provider) {
		Toast.makeText(getApplicationContext(), getString(R.string.gpsDisabledMessage), Toast.LENGTH_LONG);
	}

	public void onProviderEnabled(String provider) {
		Toast.makeText(getApplicationContext(), getString(R.string.gpsEnabledMessage), Toast.LENGTH_SHORT);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		//do nothing
	}

	public void onGpsStatusChanged(int event) {
		switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				//Toast.makeText(getApplicationContext(), "onGpsStatusChanged:" + event, Toast.LENGTH_SHORT);
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				//Toast.makeText(getApplicationContext(), "onGpsStatusChanged:" + event, Toast.LENGTH_SHORT);
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Toast.makeText(getApplicationContext(), getString(R.string.GpsSatelliteStatus), Toast.LENGTH_SHORT);
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				//Toast.makeText(getApplicationContext(), "onGpsStatusChanged:" + event, Toast.LENGTH_SHORT);
				break;
			default:
				break;
		}
	}


	private void updateSpeed(Location location) {
		float speed = 0;

		if (location != null) {
			speed = UnitConverter.convertMetersInSecondToKmInHour(location.getSpeed());
		}

		if (speed > tripSpeed) {
			tripSpeed = speed;
			updateTripSpeed();
		}

		if (speed > topSpeed) {
			topSpeed = speed;
			updateTopSpeed();
			submitTopSpeed();
		}

		updateSpeedDigits(speed, CURRENT_DIGIT_VIEWS, WHITE_DIGITS);
	}

	private void resetTripSpeed() {
		new AlertDialog.Builder(MainActivity.this)
				.setMessage(getString(R.string.confirmResetTripSpeedMessage))
				.setCancelable(true)
				.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						tripSpeed = 0;
						updateTripSpeed();
					}
				})
				.setNegativeButton(getString(R.string.no), null)
				.show();
	}

	private void submitTopSpeed() {
		if (isSignedIn()) {
			//TODO:v2 Think about how to use vehicle name
			final long scoreKmh = Math.round(topSpeed * 10);
			final long scoreMph = Math.round(UnitConverter.convertKmInHourToMilesInHour(topSpeed) * 10);
			Games.Leaderboards.submitScoreImmediate(getApiClient(), Constants.LEADERBOARD_KMH_ID, scoreKmh);
			Games.Leaderboards.submitScoreImmediate(getApiClient(), Constants.LEADERBOARD_MPH_ID, scoreMph);
			//TODO:v2 Submit to vehicle type leaderboard
		}
	}

	private void updateTripSpeed() {
		updateSpeedDigits(tripSpeed, TRIP_DIGIT_VIEWS, YELLOW_DIGITS);
	}

	private void updateTopSpeed() {
		updateSpeedDigits(topSpeed, TOP_DIGIT_VIEWS, RED_DIGITS);

		final SharedPreferences.Editor editor = preferences.edit();
		editor.putFloat(Constants.PREF_TOP_SPEED, topSpeed);
		editor.commit();
	}

	private void updateSpeedDigits(float speed, int[] digitViews, int[] digits) {
		float convertedSpeed = speed;
		if (!useMetric) {
			convertedSpeed = UnitConverter.convertKmInHourToMilesInHour(speed);
		}

		//TODO:v2 Dirty
		String stringSpeed = formatSpeedText(convertedSpeed);
		final char[] speedChars = stringSpeed.toCharArray();

		final ImageView digitOneView = (ImageView) findViewById(digitViews[0]);
		final ImageView digitTwoView = (ImageView) findViewById(digitViews[1]);
		final ImageView digitThreeView = (ImageView) findViewById(digitViews[2]);

		digitOneView.setImageResource(digits[speedChars[0] - '0']);
		digitTwoView.setImageResource(digits[speedChars[1] - '0']);
		digitThreeView.setImageResource(digits[speedChars[2] - '0']);
	}

	private String formatSpeedText(float number) {
		return formatSpeedText(number, true);
	}

	private String formatSpeedText(float number, boolean addHeadingZeros) {
		final Formatter fmt = new Formatter(new StringBuilder());
		fmt.format(Locale.US, "%5.1f", number);

		String strCurrentSpeed = fmt.toString();
		if (addHeadingZeros) {
			strCurrentSpeed = strCurrentSpeed.replace(' ', '0');
		} else {
			strCurrentSpeed = strCurrentSpeed.trim();
		}

		final String strUnits = useMetric ? kmInHourPostText : milesInHourPostText;
		return strCurrentSpeed + " " + strUnits;
	}

	@Override
	public void onSignInSucceeded() {
		super.onSignInSucceeded();

		if (!isSignedIn()) {
			return;
		}

		Person currentPerson = Plus.PeopleApi.getCurrentPerson(getApiClient());
		if (currentPerson == null) {
			return;
		}

		String accountName = currentPerson.getDisplayName();
		if (accountName == null || accountName.isEmpty()) {
			return;
		}

		final String defaultPilotName = getString(R.string.pilotDefaultName);
		final String pilotName = preferences.getString(Constants.PREF_PILOT_NAME, defaultPilotName);
		if (pilotName == null || pilotName.isEmpty() || pilotName.equals(defaultPilotName)) {
			//Update preferences
			final SharedPreferences.Editor editor = preferences.edit();
			editor.putString(Constants.PREF_PILOT_NAME, accountName);
			editor.commit();

			//Refresh profile info text views from preferences
			refreshProfileInfoView();
		}
	}

	/**
	 * Shows share dialog
	 */
	private void sharePost() {
		float convertedSpeed = tripSpeed;
		if (!useMetric) {
			convertedSpeed = UnitConverter.convertKmInHourToMilesInHour(tripSpeed);
		}

		try {
			final File file = makeScreenShot();
			final Uri uri = Uri.fromFile(file);

			final String speedString = formatSpeedText(convertedSpeed, false);
			final String subject = getString(R.string.shareSubjectText)
					.replace("{0}", speedString);
			final String message = getString(R.string.shareMessageText)
					.replace("{0}", speedString)
					.replace("{1}", getString(R.string.app_market_link));

			final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("image/*");
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			sharingIntent.putExtra(Intent.EXTRA_TEXT, message);
			sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
			startActivity(Intent.createChooser(sharingIntent, getString(R.string.shareDialogTitle)));
		} catch (IOException ex) {
			Log.e(MainActivity.class.getName(), "Error sharing post", ex);
			Toast.makeText(getApplicationContext(), getString(R.string.shareErrorMessage), Toast.LENGTH_LONG);
		}
	}

	/**
	 * Makes speed view screenshot
	 *
	 * @return screenshot file
	 * @throws IOException
	 */
	private File makeScreenShot() throws IOException {
		final View view = getWindow().getDecorView().findViewById(R.id.speedometerView);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			view.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_wide));
		} else {
			view.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
		}

		view.setDrawingCacheEnabled(true);
		final Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setBackgroundDrawable(null);
		view.setDrawingCacheEnabled(false);

		final String fileName = "share_screenshot.jpg";
		final OutputStream outStream = getApplicationContext().openFileOutput(fileName, MODE_WORLD_READABLE);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
		outStream.flush();
		outStream.close();

		return getApplicationContext().getFileStreamPath(fileName);
	}

	@Override
	public void onBackPressed() {
		confirmFinish();
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
}