package org.waiting.aestheticsofwaiting.shop;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import org.waiting.aestheticsofwaiting.MainActivity;
import org.waiting.aestheticsofwaiting.R;
import org.waiting.aestheticsofwaiting.databinding.FragmentShopInfoBinding;
import org.waiting.aestheticsofwaiting.firebase.ShopDatabase;
import org.waiting.aestheticsofwaiting.nfc.NFCActivity;


public class ShopInfoFragment extends Fragment implements
        ShopDatabase.WaitingCallback {

    private static final String TAG = ShopInfoFragment.class.getName();

    private static final String ARG_MY_ID = "my_id";
    private static final String ARG_SHOP_ID = "shop_id";


    private FragmentShopInfoBinding mViewBinder;
    private ProgressDialog mLoadingDialog;

    private MainActivity mainActivity;

    /**
     * 폰번호, 토큰값 등등,,
     */
    private String mIdToken;
    private String mShopId;
    private ShopDatabase mDatabase = new ShopDatabase();

    public ShopInfoFragment() {
        // Required empty public constructor
    }

    /**
     * shopid랑 nfcparseData랑 둘중에 하나는 null이 아니어야함.
     *
     * @param shopId 이미 예약중일때는 가게의 전화번호 가 바로 넘어옴
     * @param my_id  내 token
     * @return
     */
    public static ShopInfoFragment newInstance(String shopId, String my_id) {
        Bundle bundle = new Bundle();
        ShopInfoFragment fragment = new ShopInfoFragment();
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
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewBinder = DataBindingUtil.bind(view);
        if (mShopId == null) {
            //전화번호 받기 실패
            mainActivity.onNfcDataParseFail();
        }

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (userName.length() > 4) {
            userName = userName.substring(0, 4);
        }

        mViewBinder.txtName.setText(userName);

        try {
//        mLoadingDialog = ProgressDialog.show(getActivity(), null, "냐하하하");
            //내 앞에 몇명남았는지 확인 후 출력
            //onWaitingReceive(int time)
            mDatabase.startWatchingMyWaitingTime(mShopId, mIdToken, this);

            mViewBinder.btnPostpone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("맨 뒤로 갑니다 괜찮습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    reservationPostpone();
                                }
                            })
                            .setNegativeButton("아니오", null)
                            .show();
                }

                private void reservationPostpone() {
                    mDatabase.reservationPostpone(mShopId, mIdToken, new ShopDatabase.ReservationCancelCallback() {
                        @Override
                        public void onReservationCancelSuccess(boolean b) {
                            if (b == false) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("다음 대기자가 없으므로 미루기를 할 수 없어요.")
                                        .setPositiveButton("확인", null)
                                        .show();
                            }
                        }
                    });
                }
            });

            mViewBinder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage("예약을 취소하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    reservationCancel();
                                }
                            })
                            .setNegativeButton("아니오", null)
                            .show();
                }

                private void reservationCancel() {
                    //nfc 다시 실행
                    mDatabase.reservationCancel(mShopId, mIdToken);
                    Intent intent = new Intent(getContext(), NFCActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                }
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void onWaitingReceive(int peoples) {
        //내 순번 출력 0이 반환되면 지금임
//        mLoadingDialog.dismiss();
        if (peoples > 0) {
            mViewBinder.txtWait.setText("" + peoples);
            mViewBinder.txtWaitTeam.setVisibility(View.VISIBLE);
            mViewBinder.btnCancel.setVisibility(View.VISIBLE);
            mViewBinder.btnPostpone.setVisibility(View.VISIBLE);
            mViewBinder.txtWaitWait.setVisibility(View.VISIBLE);
        } else {
            //지금이야!
            mViewBinder.txtWait.setText("지금");
            mViewBinder.txtWaitTeam.setVisibility(View.GONE);
            mViewBinder.btnCancel.setVisibility(View.GONE);
            mViewBinder.btnPostpone.setVisibility(View.GONE);
            mViewBinder.txtWaitWait.setVisibility(View.GONE);
        }

//        mViewBinder.location.append("내 순서 " + peoples + "\n");
    }
}
