package org.twinone.irremote.ui;

import java.util.ArrayList;
import java.util.List;

import org.twinone.irremote.R;
import org.twinone.irremote.components.Remote;
import org.twinone.irremote.ir.io.Transmitter;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Displays the remote.
 * 
 * @author twinone
 * 
 */
public abstract class BaseRemoteFragment extends Fragment implements
		Transmitter.OnTransmitListener {

	protected static final String TAG = "RemoteFragment";

	protected Remote mRemote;
	private Transmitter mTransmitter;
	protected List<ButtonView> mButtons = new ArrayList<ButtonView>();
	// protected ComponentUtils mComponentUtils;

	protected RemoteView mRemoteView;
	protected ScrollView mScroll;

	private static final String ARG_REMOTE_NAME = "arg_remote_name";

	/** Use this method just after calling the constructor */
	public final void showFor(Activity a, String remoteName) {

		Bundle b = new Bundle();
		b.putSerializable(ARG_REMOTE_NAME, remoteName);
		setArguments(b);
		a.getFragmentManager().beginTransaction().replace(R.id.container, this)
				.commit();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() == null
				|| !getArguments().containsKey(ARG_REMOTE_NAME)) {
			throw new RuntimeException(
					"You should create this fragment with the showFor method");
		}
		mRemote = Remote.load(getActivity(), (String) getArguments()
				.getSerializable(ARG_REMOTE_NAME));

		mTransmitter = Transmitter.getInstance(getActivity());
		if (mTransmitter != null) {
			mTransmitter.setListener(this);
		}
		// mComponentUtils = new ComponentUtils(getActivity());

	}

	private int getThemeIdFromPrefs() {
		SharedPreferences sp = SettingsActivity.getPreferences(getActivity());
		String theme = sp.getString(getString(R.string.pref_key_theme),
				getString(R.string.pref_def_theme));
		if (theme.equals(getString(R.string.pref_val_theme_sl))) {
			return R.style.theme_solid;
		}
		return R.style.theme_transparent;

	}

	/**
	 * Call super.onCreateView for theming and optionsMenu
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getActivity().setTheme(getThemeIdFromPrefs());
		setHasOptionsMenu(true);

		if (mRemote == null) {
			return new View(getActivity());
		}

		mScroll = (ScrollView) inflater.inflate(R.layout.fragment_remote_new,
				container, false);

		mRemoteView = (RemoteView) mScroll.findViewById(R.id.container);
		mRemoteView.setRemote(mRemote);
		setupButtons();

		return mScroll;

	}

	protected void setupButtons() {
		mRemoteView.removeAllViews();
		mButtons = new ArrayList<ButtonView>(mRemote.buttons.size());
		for (org.twinone.irremote.components.Button b : mRemote.buttons) {
			ButtonView bv = new ButtonView(getActivity());
			// // lp.topMargin = (int) b.y;
			// // lp.leftMargin = (int) b.x;
			// bv.setLayoutParams(lp);
			bv.setButton(b);

			mButtons.add(bv);
			mRemoteView.addView(bv);
			bv.setX(b.x);
			bv.setY(b.y);
			bv.getLayoutParams().width = (int) b.w;
			bv.getLayoutParams().height = (int) b.h;
			bv.requestLayout();

		}
	}

	protected Transmitter getTransmitter() {
		return mTransmitter;
	}

	public Remote getRemote() {
		return mRemote;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		mMenuIcon = menu.findItem(R.id.menu_transmit_feedback);

	}

	private MenuItem mMenuIcon;
	private static final int MINIMUM_SHOW_TIME = 100; // ms
	protected Handler mHandler = new Handler();
	private Runnable mHideFeedbackRunnable = new HideFeedbackRunnable();

	private class HideFeedbackRunnable implements Runnable {
		@Override
		public void run() {
			if (mMenuIcon != null)
				mMenuIcon.setVisible(false);
		}
	}

	@Override
	public void onBeforeTransmit() {
		if (mHandler != null && mHideFeedbackRunnable != null) {
			mHandler.removeCallbacks(mHideFeedbackRunnable);
		}
		if (mMenuIcon != null) {
			mMenuIcon.setVisible(true);
		}
	}

	@Override
	public void onAfterTransmit() {
		mHandler.postDelayed(mHideFeedbackRunnable, MINIMUM_SHOW_TIME);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mTransmitter != null)
			mTransmitter.resume();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mTransmitter != null)
			mTransmitter.pause();
	}

}
