/*
 * ******************************************************************************
 *   Copyright (c) 2014-2015 Ivan Gaglioti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package org.symptomcheck.capstone.fragments;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;

import com.activeandroid.content.ContentProvider;

import org.symptomcheck.capstone.App;
import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.accounts.GenericAccountService;
import org.symptomcheck.capstone.adapters.DoctorRecyclerCursorAdapter;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.utils.Constants;

import it.gmariotti.cardslib.library.view.CardListView;

//TODO#BPR_6 Doctor Fragment Interface Screen
public class DoctorFragmentRecycler extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>
,IFragmentListener {

    private static String ARG_PATIENT_ID  ="patient_id";
    DoctorRecyclerCursorAdapter mRecyclerCursorAdapter;
    CardListView mListView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;
    private Menu mOptionsMenu;
    private Patient mPatientOwner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_card_doctors_list_recycler, container, false);
        setupListFragment(root);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_DOCTORS);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        //inflater.inflate(R.menu.cards, menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setIconActionBar();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        init();
        super.onActivityCreated(savedInstanceState);
        hideList(false);
    }



    public static PatientsFragment newInstance(long patientId) {
        PatientsFragment fragment = new PatientsFragment();
        Bundle args = new Bundle();
        if (patientId != -1) {
            args.putLong(ARG_PATIENT_ID, patientId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getTitleText() {
        String title = TITLE_NONE;

        if(mPatientOwner != null){
            title =// mPatientOwner.getFirstName() + " " +
                    //mPatientOwner.getLastName() + "'s " +
                          //String.format(getString(R.string.doctor_patient),mPatientOwner.getLastName());
                          getString(R.string.doctors_header);
        }
        return title;
    }

    @Override
    public String getIdentityOwnerId() {
        return Constants.STRINGS.EMPTY;
    }


    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occuring, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                hideList(true);
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                displayList(false);
                OnFilterData(Constants.STRINGS.EMPTY);
                refreshItem.setActionView(null);
            }
        }
    }

    static int count = 0;
    private void init() {

         mListView = (CardListView) getActivity().findViewById(R.id.card_doctors_list_cursor);
        final UserInfo user = DAOManager.get().getUser();

        //TODO#BPR_1
        if(user.getUserType().equals(UserType.PATIENT)){
            mPatientOwner = Patient.getByMedicalNumber(user.getUserIdentification());
        }


        mRecyclerCursorAdapter = new DoctorRecyclerCursorAdapter(null, App.getContext());
        mRecyclerCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return queryAllField(charSequence.toString(), null);
            }
        });

        //mRecyclerCursorAdapter.addEventListener(this);

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.doctor_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        //mRecyclerCursorAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mRecyclerCursorAdapter);

        /*
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return queryAllField(charSequence.toString(),null);
            }
        });
        */

        // Force start background query to load sessions
        getLoaderManager().restartLoader(0, null, this);


    }

    //TODO#BPR_3 Create Cursor over ContentProvider
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Loader<Cursor> loader = null;
        loader = new CursorLoader(getActivity(),
                ContentProvider.createUri(Doctor.class, null),
                null, null, null, ActiveContract.DOCTORS_COLUMNS.LAST_NAME + " asc"
        );
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        //mAdapter.swapCursor(data);
        //if(USE_RECYCLER_VIEW)
            mRecyclerCursorAdapter.swapCursor(data);
        //else
        //    mStandardCursorAdapter.swapCursor(data);

        displayList(data.getCount() <= 0);
        OnFilterData(Constants.STRINGS.EMPTY);

    }
    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() { //TODO#BPR_8
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount();

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, ActiveContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, ActiveContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    public static final int ID_COLUMN = 0;
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mAdapter.swapCursor(null);
        mRecyclerCursorAdapter.swapCursor(null);
    }

    @Override
    public int getFragmentType() {
        return BaseFragment.FRAGMENT_TYPE_DOCTORS;
    }

    @Override
    public void OnFilterData(String textToSearch) {
        //if(mAdapter != null)
            //mAdapter.getFilter().filter(textToSearch);
        if (mRecyclerCursorAdapter != null) {
            mRecyclerCursorAdapter.getFilter().filter(textToSearch);
            mRecyclerCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnSearchOnLine(String textToSearch) {

    }


}
