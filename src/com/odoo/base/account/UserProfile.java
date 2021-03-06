/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo.base.account;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.auth.OdooAccountManager;
import com.odoo.orm.OEHelper;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.support.OEUser;
import com.odoo.util.Base64Helper;
import com.odoo.util.drawer.DrawerItem;

public class UserProfile extends BaseFragment {
	View rootView = null;
	EditText password = null;
	TextView txvUserLoginName, txvUsername, txvServerUrl, txvTimeZone,
			txvDatabase;
	ImageView imgUserPic;
	AlertDialog.Builder builder = null;
	Dialog dialog = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.fragment_account_user_profile,
				container, false);
		scope = new AppScope(this);
		scope.main().setTitle(R.string.title_user_profile);

		setupView();
		return rootView;
	}

	private void setupView() {

		imgUserPic = null;
		imgUserPic = (ImageView) rootView.findViewById(R.id.imgUserProfilePic);
		if (!scope.User().getAvatar().equals("false"))
			imgUserPic.setImageBitmap(Base64Helper.getBitmapImage(
					scope.context(), scope.User().getAvatar()));
		txvUserLoginName = (TextView) rootView
				.findViewById(R.id.txvUserLoginName);
		txvUserLoginName.setText(scope.User().getAndroidName());

		txvUsername = (TextView) rootView.findViewById(R.id.txvUserName);
		txvUsername.setText(scope.User().getUsername());

		txvServerUrl = (TextView) rootView.findViewById(R.id.txvServerUrl);
		txvServerUrl.setText(scope.User().getHost());

		txvDatabase = (TextView) rootView.findViewById(R.id.txvDatabase);
		txvDatabase.setText(scope.User().getDatabase());

		txvTimeZone = (TextView) rootView.findViewById(R.id.txvTimeZone);
		String timezone = scope.User().getTimezone();
		txvTimeZone.setText((timezone.equals("false")) ? "GMT" : timezone);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_account_user_profile, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_account_user_profile_sync:
			dialog = inputPasswordDialog();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private Dialog inputPasswordDialog() {
		builder = new Builder(scope.context());
		password = new EditText(scope.context());
		password.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		builder.setTitle(R.string.title_enter_password)
				.setMessage(R.string.toast_provide_password).setView(password);
		builder.setPositiveButton(R.string.label_update_info,
				new OnClickListener() {
					public void onClick(DialogInterface di, int i) {
						OEUser userData = null;
						try {
							OEHelper odoo = new OEHelper(scope.context());
							userData = odoo.login(scope.User().getUsername(),
									password.getText().toString(), scope.User()
											.getDatabase(), scope.User()
											.getHost());
						} catch (Exception e) {
						}
						if (userData != null) {
							if (OdooAccountManager.updateAccountDetails(
									scope.context(), userData)) {
								Toast.makeText(getActivity(),
										"Infomation Updated.",
										Toast.LENGTH_LONG).show();
							}
						} else {
							Toast.makeText(
									getActivity(),
									getResources().getString(
											R.string.toast_invalid_password),
									Toast.LENGTH_LONG).show();
						}
						setupView();
						dialog.cancel();
						dialog = null;
					}
				});
		builder.setNegativeButton(R.string.label_cancel, new OnClickListener() {
			public void onClick(DialogInterface di, int i) {
				dialog.cancel();
				dialog = null;
			}
		});
		return builder.create();

	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}
}
