package smo.zooket.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import smo.zooket.FragmentGPSMap;
import smo.zooket.FragmentListGPSSupermarket;

/**
 * Created by Smo on 06/06/2017.
 */
public class ViewPagerGPSAdapter extends FragmentStatePagerAdapter {
    public ViewPagerGPSAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int num) {
        switch (num) {
            case 0:
                return new FragmentGPSMap();
            case 1:
                return new FragmentListGPSSupermarket();
            default:
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = "نقشه";
                break;
            case 1:
                title = "لیست فروشگاه ها";
                break;
            default:
                break;
        }
        return title;
    }





}
