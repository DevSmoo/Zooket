package smo.zooket.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import smo.zooket.FragmentGPSMap;
import smo.zooket.FragmentListGPSSupermarket;
import smo.zooket.SupermarketInformationFragment;
import smo.zooket.SupermarketProductFragment;

/**
 * Created by Smo on 07/06/2017.
 */
public class SupermarketTabViewPagerAdapter  extends FragmentStatePagerAdapter {
    public SupermarketTabViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int num) {
        switch (num) {
            case 0:
                return new SupermarketCommentFragment();
            case 1:
                return new SupermarketInformationFragment();
            case 2:
                return new SupermarketProductFragment();
            default:
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = "نظرات";
                break;
            case 1:
                title = "اطلاعات فروشگاه";
                break;
            case 2:
                title = "محصولات";
                break;
            default:
                break;
        }
        return title;
    }





}
