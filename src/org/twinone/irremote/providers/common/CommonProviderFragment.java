package org.twinone.irremote.providers.common;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import org.twinone.irremote.R;
import org.twinone.irremote.components.AnimHelper;
import org.twinone.irremote.components.Button;
import org.twinone.irremote.components.ComponentUtils;
import org.twinone.irremote.components.Remote;
import org.twinone.irremote.providers.BaseListable;
import org.twinone.irremote.providers.ListableAdapter;
import org.twinone.irremote.providers.ProviderFragment;
import org.twinone.irremote.providers.globalcache.GCProviderActivity;
import org.twinone.irremote.util.FileUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class CommonProviderFragment extends ProviderFragment implements
		OnItemClickListener, OnItemLongClickListener {

	private static final String COMMON_TV_NAME = "TV";
	private static final String COMMON_BLURAY_NAME = "BluRay";
	private static final String COMMON_CABLE_NAME = "Cable";
	private static final String COMMON_AUDIO_AMPLIFIER = "Audio";

	private ListView mListView;
	private ListableAdapter mAdapter;

	public static final String ARG_DATA = "arg.data";
	private Data mTarget;

	public static class Data implements Serializable {

		public Data() {
			targetType = TARGET_DEVICE_TYPE;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -1889643026103427356L;

		public static final int TARGET_DEVICE_TYPE = 0;
		public static final int TARGET_DEVICE_NAME = 1;
		public static final int TARGET_IR_CODE = 2;

		int targetType;
		String deviceType;
		String deviceName;

		public Data clone() {
			Data d = new Data();
			d.targetType = targetType;
			d.deviceType = deviceType;
			d.deviceName = deviceName;
			return d;
		}

	}

	private int getDeviceTypeInt(String deviceType) {
		if (COMMON_TV_NAME.equals(deviceType)) {
			return Remote.TYPE_TV;
		}
		if (COMMON_CABLE_NAME.equals(deviceType)) {
			return Remote.TYPE_CABLE;
		}
		if (COMMON_BLURAY_NAME.equals(deviceType)) {
			return Remote.TYPE_BLURAY;
		}
		if (COMMON_AUDIO_AMPLIFIER.equals(deviceType)) {
			return Remote.TYPE_AUDIO_AMPLIFIER;
		}
		throw new IllegalArgumentException("WTF, no such type" + deviceType);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null && getArguments().containsKey(ARG_DATA)) {
			mTarget = (Data) getArguments().getSerializable(ARG_DATA);
			Log.d("", "mTarget.deviceType = " + mTarget.deviceType);
		} else {
			mTarget = new Data();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		// For navigation
		setCurrentType(mTarget.targetType);

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.fragment_listable, container, false);

		mListView = (ListView) rootView.findViewById(R.id.lvElements);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		mAdapter = new ListableAdapter(getActivity(), getItems());
		mListView.setAdapter(mAdapter);

		String name = getDataName(" > ");
		if (name == null) {
			getActivity().setTitle(R.string.db_select_device_type);
		} else {
			getActivity().setTitle(name);
		}
		return rootView;
	}

	private String getDBPath() {
		final String name = getDataName(File.separator);
		if (name == null) {
			return "db";
		}
		return "db" + File.separator + name;
	}

	private String getDataName(String separator) {
		StringBuilder path = new StringBuilder();
		if (mTarget.deviceType == null)
			return null;
		path.append(mTarget.deviceType);
		if (mTarget.deviceName == null)
			return path.toString();
		path.append(separator).append(mTarget.deviceName);
		return path.toString();
	}

	@SuppressWarnings("serial")
	private class MyListable extends BaseListable {

		public MyListable(String text) {
			this.text = text;
		}

		private String text;

		public int id;

		@Override
		public String getDisplayName() {
			return text;
		}

	}

	private MyListable[] getItems() {
		ArrayList<MyListable> items = new ArrayList<MyListable>();
		if (mTarget.targetType == Data.TARGET_IR_CODE) {
			mRemote = buildRemote();
			for (Button b : mRemote.buttons) {
				MyListable l = new MyListable(b.text);
				l.id = b.uid;
				items.add(l);
			}
		} else {
			for (String s : listAssets(getDBPath())) {
				items.add(new MyListable(s));
			}
		}
		return items.toArray(new MyListable[items.size()]);
	}

	private String[] listAssets(String path) {
		try {
			return getActivity().getAssets().list(path);
		} catch (Exception e) {
			return new String[] {};
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			saveRemote();
			break;
		case R.id.menu_more:
			getActivity().finish();
			Intent i = new Intent(getActivity(), GCProviderActivity.class);
			i.setAction(getProvider().getAction());
			AnimHelper.startActivity(getActivity(), i);
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long viewId) {
		MyListable item = (MyListable) mListView.getAdapter().getItem(position);
		if (mTarget.targetType == Data.TARGET_DEVICE_TYPE) {
			Data clone = mTarget.clone();
			clone.deviceType = item.getDisplayName();
			clone.targetType = Data.TARGET_DEVICE_NAME;
			((CommonProviderActivity) getActivity()).addFragment(clone);
		} else if (mTarget.targetType == Data.TARGET_DEVICE_NAME) {
			mTarget.deviceName = item.getDisplayName();
			if (ACTION_SAVE_REMOTE.equals(getProvider().getAction())) {
				mRemote = buildRemote();
				saveRemote();
			} else {
				mTarget.targetType = Data.TARGET_IR_CODE;
				((CommonProviderActivity) getActivity()).addFragment(mTarget
						.clone());
			}
		} else if (mTarget.targetType == Data.TARGET_IR_CODE) {
			Button b = mRemote.getButton(item.id);
			getProvider().saveButton(b);
		}
	}

	private Remote buildRemote() {
		Remote r = new Remote();
		r.name = mTarget.deviceName + " " + mTarget.deviceType;
		final String remotedir = getDBPath();
		for (String name : listAssets(getDBPath())) {
			int id = Integer.parseInt(name.substring(2).split("\\.")[0]);
			Button b = new Button(id);
			b.code = FileUtils.read(getActivity().getAssets(), remotedir
					+ File.separator + name);
			b.text = ComponentUtils.getCommonButtonDisplyaName(b.id,
					getActivity());
			r.addButton(b);
			Log.d("TEST", "Adding button " + b.text + " to remote");
		}
		r.options.type = getDeviceTypeInt(mTarget.deviceType);
		return r;
	}

	private Remote mRemote;

	private void saveRemote() {
		getProvider().saveRemote(mRemote);
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		mListView.setItemChecked(position, true);
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.common_menu, menu);
		MenuItem save = menu.findItem(R.id.menu_save);
		MenuItem more = menu.findItem(R.id.menu_more);
		boolean ircode = mTarget.targetType == Data.TARGET_IR_CODE;
		boolean remote = getProvider().getAction().equals(ACTION_SAVE_REMOTE);
		save.setVisible(ircode && remote);
		more.setVisible(!ircode);
	}
}
