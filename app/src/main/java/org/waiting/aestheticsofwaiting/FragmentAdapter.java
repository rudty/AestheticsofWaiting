package org.waiting.aestheticsofwaiting;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.daum.mf.map.api.MapView;

import org.waiting.aestheticsofwaiting.shop.MapFragment;
import org.waiting.aestheticsofwaiting.shop.MenuInfoFragment;
import org.waiting.aestheticsofwaiting.shop.ShopInfoFragment;

/**
 * Created by d on 2017-02-01.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    private final String reservationId;
    private final String myId;
    private ShopInfoFragment shopInfoFragment;
    private MenuInfoFragment menuInfoFragment;
    private MapFragment mapFragment;


    public FragmentAdapter(FragmentManager fm, String reservationId, String myId) {
        super(fm);
        this.reservationId = reservationId;
        this.myId = myId;
        shopInfoFragment = ShopInfoFragment.newInstance(reservationId, myId);
        menuInfoFragment = MenuInfoFragment.newInstance(reservationId, myId);
        mapFragment = MapFragment.newInstance(reservationId, myId);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            return shopInfoFragment;
        }
        else if(position == 1){
            return menuInfoFragment;
        }
        else if(position == 2){
            return mapFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
