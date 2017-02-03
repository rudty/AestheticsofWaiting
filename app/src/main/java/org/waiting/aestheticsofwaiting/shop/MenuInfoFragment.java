package org.waiting.aestheticsofwaiting.shop;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.viewpagerindicator.CirclePageIndicator;

import org.jetbrains.annotations.NotNull;
import org.waiting.aestheticsofwaiting.R;
import org.waiting.aestheticsofwaiting.firebase.ShopDatabase;

import java.util.ArrayList;
import java.util.Map;


public class MenuInfoFragment extends Fragment implements ShopDatabase.ShopMenuCallback, ShopDatabase.ShopNameCallback {

    private static final String ARG_MY_ID = "my_id";
    private static final String ARG_SHOP_ID = "shop_id";
    private static final String TAG = MenuInfoFragment.class.getName();
    private String mIdToken;
    private String mShopId;


    private ShopDatabase mDatabase = new ShopDatabase();

    private ListView mListView;
    private TextView txt_shop_name;
    private ImageView mStore_image;
    private ViewPager mViewPager;
    private CirclePageIndicator mIndicator;
    private MenuPictureAdapter mMenuPictrueAdapter;
    private Button telBtn;

    public MenuInfoFragment() {
        // Required empty public constructor
    }

    /**
     * shopid랑 nfcparseData랑 둘중에 하나는 null이 아니어야함.
     *
     * @param shopId 이미 예약중일때는 가게의 전화번호 가 바로 넘어옴
     * @param my_id  내 token
     * @return
     */
    public static MenuInfoFragment newInstance(String shopId, String my_id) {
        Bundle bundle = new Bundle();
        MenuInfoFragment fragment = new MenuInfoFragment();
        bundle.putString(ARG_MY_ID, my_id);
        bundle.putString(ARG_SHOP_ID, shopId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mIdToken = getArguments().getString(ARG_MY_ID);
        mShopId = b.getString(ARG_SHOP_ID);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txt_shop_name = (TextView) view.findViewById(R.id.txt_shop_name);
        mListView = (ListView) view.findViewById(R.id.listView);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        telBtn = (Button)view.findViewById(R.id.telBtn);
        mMenuPictrueAdapter = new MenuPictureAdapter(getContext());
        mViewPager.setAdapter(mMenuPictrueAdapter);
        mIndicator.setViewPager(mViewPager);
//        mStore_image = (ImageView) view.findViewById(R.id.store_image);
//        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "computer_wash_r.ttf");
//        txt_shop_name.setTypeface(font);

        mDatabase.getShopName(mShopId, this);
        mDatabase.getShopMenu(mShopId, this);


        //이미지 다운로드를 수행함.(최대 5개)
        for (int i = 1; i <= 5; i++) {
            FirebaseStorage.getInstance().getReference().child(mShopId).child(i + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    mMenuPictrueAdapter.add(uri);
                    mMenuPictrueAdapter.notifyDataSetChanged();
                }
            });
        }
        telBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+mShopId));
                startActivity(intent);
            }
        });
    }


    @Override
    public void onShopNameReceive(@NotNull String name) {
        txt_shop_name.setText("가게명: " + name + "\n전화번호: " + mShopId);
    }

    @Override
    public void onMenuReceive(@NotNull Map<String, Object> menu) {
        MenuListAdapter menuAdapter = new MenuListAdapter(getContext(), new ArrayList<>(menu.entrySet()));
        mListView.setAdapter(menuAdapter);
    }
}
