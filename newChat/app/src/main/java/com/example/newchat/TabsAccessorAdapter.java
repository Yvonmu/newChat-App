package com.example.newchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {


    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                CheckFragment CheckFragment=new CheckFragment();
                return CheckFragment;
            case 1:
                groupFragment groupFragment=new groupFragment();
                return groupFragment;
            case 2:
                ContactFragment contactFragment=new ContactFragment();
                return contactFragment;
            case 3:
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Check";

            case 1:

                return "group";
            case 2:

                return "Contacts";

            case 3:

                return "Requests";
            default:
                return null;
        }

    }

}
