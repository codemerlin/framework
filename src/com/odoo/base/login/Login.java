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
package com.odoo.base.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.auth.OdooAccountManager;
import com.odoo.orm.OEHelper;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.support.OEDialog;
import com.odoo.support.OEUser;
import com.odoo.support.fragment.FragmentListener;
import com.odoo.util.drawer.DrawerItem;

/**
 * The Class Login.
 */
public class Login extends BaseFragment {

	/** The item arr. */
	String[] itemArr = null;

	/** The context. */
	Context context = null;

	/** The m action mode. */
	ActionMode mActionMode;

	/** The odooserver url. */
	String odooServerURL = "";
	boolean mAllowSelfSignedSSL = false;

	/** The edt server url. */
	EditText edtServerUrl = null;

	/** The arguments. */
	Bundle arguments = null;

	/** The db list spinner. */
	Spinner dbListSpinner = null;

	/** The root view. */
	View rootView = null;

	/** The login user a sync. */
	LoginUser loginUserASync = null;

	/** The edt username. */
	EditText edtUsername = null;

	/** The edt password. */
	EditText edtPassword = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		this.context = getActivity();
		scope = new AppScope(this);

		// Inflate the layout for this fragment
		rootView = inflater.inflate(R.layout.fragment_login, container, false);
		dbListSpinner = (Spinner) rootView.findViewById(R.id.lstDatabases);
		handleArguments((Bundle) getArguments());
		getActivity().setTitle(R.string.label_login);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		getActivity().getActionBar().setHomeButtonEnabled(false);
		edtUsername = (EditText) rootView.findViewById(R.id.edtUsername);
		edtPassword = (EditText) rootView.findViewById(R.id.edtPassword);
		edtPassword.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					goNext();
				}
				return false;
			}
		});
		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.odoo.support.FragmentHelper#handleArguments(android.os.Bundle)
	 */
	public void handleArguments(Bundle bundle) {
		arguments = bundle;
		if (arguments != null && arguments.size() > 0) {
			if (arguments.containsKey("odooServerURL")) {
				odooServerURL = arguments.getString("odooServerURL");
				mAllowSelfSignedSSL = arguments
						.getBoolean("allow_self_signed_ssl");
				String[] databases = arguments.getStringArray("databases");
				List<String> dbLists = new ArrayList<String>();
				dbLists.addAll(Arrays.asList(databases));
				loadDatabaseList(dbLists);
			}
		}
	}

	/**
	 * Load database list.
	 */
	private void loadDatabaseList(List<String> dbList) {
		try {
			dbList.add(0,
					getActivity().getString(R.string.login_select_database));
			ArrayAdapter<String> dbAdapter = new ArrayAdapter<String>(
					getActivity(), R.layout.spinner_custom_layout, dbList);
			dbAdapter.setDropDownViewResource(R.layout.spinner_custom_layout);
			dbListSpinner.setAdapter(dbAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
	 * android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_login, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection

		switch (item.getItemId()) {
		case R.id.menu_login_account:
			Log.d("LoginFragment()->ActionBarMenuClicked", "menu_login_account");
			goNext();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void goNext() {
		edtUsername.setError(null);
		edtPassword.setError(null);
		if (TextUtils.isEmpty(edtUsername.getText())) {
			edtUsername.setError(getResources().getString(
					R.string.toast_provide_username));
		} else if (TextUtils.isEmpty(edtPassword.getText())) {
			edtPassword.setError(getResources().getString(
					R.string.toast_provide_password));
		} else if (dbListSpinner.getSelectedItemPosition() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.toast_select_database),
					Toast.LENGTH_LONG).show();
		} else {
			loginUserASync = new LoginUser();
			loginUserASync.execute((Void) null);
		}
	}

	/**
	 * The Class LoginUser.
	 */
	private class LoginUser extends AsyncTask<Void, Void, Boolean> {

		/** The pdialog. */
		OEDialog pdialog;

		/** The error msg. */
		String errorMsg = "";

		/** The user data. */
		OEUser userData = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			pdialog = new OEDialog(getActivity(), false, getResources()
					.getString(R.string.title_loggin_in));
			pdialog.show();
			edtPassword.setError(null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Boolean doInBackground(Void... params) {

			String userName = edtUsername.getText().toString();
			String password = edtPassword.getText().toString();
			String database = dbListSpinner.getSelectedItem().toString();
			OEHelper odoo = new OEHelper(getActivity(), false,
					mAllowSelfSignedSSL);
			App app = (App) scope.context().getApplicationContext();
			app.setOEInstance(null);
			userData = odoo.login(userName, password, database, odooServerURL);
			if (userData != null) {
				return true;
			} else {
				errorMsg = getResources().getString(
						R.string.toast_invalid_username_password);
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {
				Log.v("Creating Account For Username :",
						userData.getAndroidName());
				if (OdooAccountManager.fetchAllAccounts(getActivity()) != null) {
					if (OdooAccountManager.isAnyUser(getActivity())) {
						OdooAccountManager.logoutUser(getActivity(),
								OdooAccountManager.currentUser(getActivity())
										.getAndroidName());
					}
				}
				if (OdooAccountManager.createAccount(getActivity(), userData)) {
					loginUserASync.cancel(true);
					pdialog.hide();
					SyncWizard syncWizard = new SyncWizard();
					FragmentListener mFragment = (FragmentListener) getActivity();
					mFragment.startMainFragment(syncWizard, false);

				}
			} else {
				edtPassword.setError(errorMsg);
			}
			loginUserASync.cancel(true);
			pdialog.hide();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			loginUserASync.cancel(true);
			pdialog.hide();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		rootView = null; // now cleaning up!
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
