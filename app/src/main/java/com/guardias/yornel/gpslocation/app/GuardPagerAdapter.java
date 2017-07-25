/*
 * Copyright (C) 2008-2013 The Android Open Source Project,
 * Sean J. Barbeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guardias.yornel.gpslocation.app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;
import com.guardias.yornel.gpslocation.R;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
 * one of the primary sections of the app.
 */
public class GuardPagerAdapter extends FragmentStatePagerAdapter {

    public static final int NUMBER_OF_TABS = 2; // Used to set up TabListener

    // Constants for the different fragments that will be displayed in tabs, in numeric order
    public static final int MAP_FRAGMENT = 0;

    public static final int LIST_FRAGMENT = 1;

    // Maintain handle to Fragments to avoid recreating them if one already
    // exists
    GuardMapFragment map;
    ListViewFragment list;

    public GuardPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case MAP_FRAGMENT:
                if (map == null) {
                    map = new GuardMapFragment();
                }
                return map;
            case LIST_FRAGMENT:
                if (list == null) {
                    list = new ListViewFragment();
                }
                return list;
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUMBER_OF_TABS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case MAP_FRAGMENT:
                return GuardActivity.getInstance().getString(R.string.title_map);
            case LIST_FRAGMENT:
                return GuardActivity.getInstance().getString(R.string.title_register);
        }
        return null;
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Seccion de Lista");
            return rootView;
        }
    }
}
