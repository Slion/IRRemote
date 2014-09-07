package org.twinone.irremote.components;

import org.twinone.irremote.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;

public abstract class AnimHelper {
	public static void startActivity(Activity from, Intent to) {
		from.startActivity(to);
		from.overridePendingTransition(R.anim.slide_in_right, 0);
	}

	/**
	 * Will not finish the calling activity
	 * 
	 */
	public static void onFinish(Activity a) {
		a.overridePendingTransition(0, R.anim.slide_out_right);
	}

	public static void showDialog(AlertDialog.Builder ab) {
		showDialog(ab.create());
	}

	/**
	 * Convenience method for setting the dialog animations and showing it
	 * 
	 * @param d
	 */
	public static void showDialog(Dialog d) {
		addAnimations(d);
		d.show();
	}

	public static Dialog addAnimations(Dialog d) {
		d.getWindow().getAttributes().windowAnimations = R.style.DialogAnims;
		return d;
	}
}
