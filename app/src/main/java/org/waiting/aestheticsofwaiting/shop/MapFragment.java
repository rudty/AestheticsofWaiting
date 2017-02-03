package org.waiting.aestheticsofwaiting.shop;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.daum.mf.map.api.CameraPosition;
import net.daum.mf.map.api.CameraUpdate;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.waiting.aestheticsofwaiting.R;
import org.waiting.aestheticsofwaiting.firebase.ShopDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements ShopDatabase.LocationCallback {
    private static final String ARG_MY_ID = "my_id";
    private static final String ARG_SHOP_ID = "shop_id";
    private static final String TAG = MapFragment.class.getName();
    private String mIdToken;
    private String mShopId;
    private ShopDatabase mShopDatabase = new ShopDatabase();
    private MapView mapView;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String shopId, String my_id) {
        Bundle bundle = new Bundle();
        MapFragment fragment = new MapFragment();
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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.setDaumMapApiKey("API_KEY");
        mShopDatabase.getShopLocation(mShopId, this);
    }

    @Override
    public void onLocationReceive(double lat, double lng) {
        Log.e(TAG, lat+" "+lng);
        MapPoint point = MapPoint.mapPointWithGeoCoord(lat, lng);
        mapView.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(point,0)));

        MapPOIItem customMarker = new MapPOIItem();
        customMarker.setItemName("위치");
        customMarker.setTag(1);
        customMarker.setMapPoint(point);
        customMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 기본으로 제공하는 BluePin 마커 모양.
        customMarker.setCustomImageResourceId(R.drawable.map_marker); // 마커 이미지.
        customMarker.setCustomImageAutoscale(true);
        customMarker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

        mapView.addPOIItem(customMarker);
    }
}
