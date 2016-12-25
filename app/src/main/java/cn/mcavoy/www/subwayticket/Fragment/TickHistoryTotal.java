package cn.mcavoy.www.subwayticket.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.victor.loading.rotate.RotateLoading;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import java.util.ArrayList;
import java.util.List;

import cn.mcavoy.www.subwayticket.Adapter.TicketListAdapter;
import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.CallServer;
import cn.mcavoy.www.subwayticket.Model.TicketModel;
import cn.mcavoy.www.subwayticket.R;

public class TickHistoryTotal extends Fragment {
    private RecyclerView mrecyclerView;
    private TicketModel ticketModel;
    private List<TicketModel.TicketsEntity> mlists = new ArrayList<>();
    private List<TicketModel.TicketsEntity> mAlllists = new ArrayList<>();
    private TicketListAdapter ticketListAdapter;

    private View view;
    private RelativeLayout noDateView;
    private RotateLoading rotateLoading;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ticket_history_total, container, false);
        noDateView = (RelativeLayout) view.findViewById(R.id.no_history_tip_total);
        mrecyclerView = (RecyclerView) view.findViewById(R.id.totaltravel_recycler_view);
        rotateLoading = (RotateLoading) view.findViewById(R.id.totalTravelOrder_loading);

        getDate();
        return view;
    }

    private void getDate() {
        SharedPreferences sp = getActivity().getSharedPreferences("user_validate", Context.MODE_PRIVATE);
        //假如存在
        if (sp.contains("user_token")) {
            Request<String> request = new StringRequest(MetroApplication.getTotalTravelOrderApi);
            request.addHeader("ownerId", MetroApplication.userModel.getId());
            request.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
            CallServer.getInstance().add(0, request, listener);
        } else {

        }
    }

    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            rotateLoading.start();
        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 0) {
                if (response.responseCode() == 200) {
                    rotateLoading.stop();
                    String tempDate = response.get().toString();
                    if (!tempDate.equals("[]")) {
                        noDateView.setVisibility(View.GONE);
                        try {
                            Gson gson = new Gson();
                            List<TicketModel.TicketsEntity> ticketsEntities = gson.fromJson(tempDate, new TypeToken<List<TicketModel.TicketsEntity>>() {
                            }.getType());
                            ticketModel = new TicketModel();
                            ticketModel.setTicketsEntities(ticketsEntities);
                            initUi();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        noDateView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            Exception exception = response.getException();
            if (exception instanceof TimeoutError) {
                if (what == 0) {
                    Toast.makeText(getActivity().getBaseContext(), "服务器连接失败!", Toast.LENGTH_SHORT).show();
                    rotateLoading.stop();
                }
            }
        }

        @Override
        public void onFinish(int what) {

        }
    };

    private void initUi() {
        seperateLists(ticketModel);
        if (ticketListAdapter == null) {
            ticketListAdapter = new TicketListAdapter(view.getContext(), mAlllists);
            int orientation = LinearLayoutManager.VERTICAL;
            LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(), orientation, false);
            mrecyclerView.setLayoutManager(layoutManager);
            mrecyclerView.setHasFixedSize(true);
            mrecyclerView.setAdapter(ticketListAdapter);
        } else {
            ticketListAdapter.notifyDataSetChanged();
        }

        ticketListAdapter.setmOnItemClickListener(new TicketListAdapter.OnRecyclerViewItemListener() {
            @Override
            public void onItemClick(View view, TicketModel.TicketsEntity ticketsEntity) {
                Toast.makeText(view.getContext(), "点击" + ticketsEntity.getId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seperateLists(TicketModel model) {
        if (model.getTicketsEntities() != null && model.getTicketsEntities().size() > 0) {
            for (int i = 0; i < model.getTicketsEntities().size(); ++i) {
                TicketModel.TicketsEntity entity = new TicketModel.TicketsEntity();
                entity.setId(model.getTicketsEntities().get(i).getId());
                entity.setoStationName(model.getTicketsEntities().get(i).getoStationName());
                entity.settStationName(model.getTicketsEntities().get(i).gettStationName());
                entity.setOwnerId(model.getTicketsEntities().get(i).getOwnerId());
                entity.setTicketNum(model.getTicketsEntities().get(i).getTicketNum());
                entity.setTicketPrice(model.getTicketsEntities().get(i).getTicketPrice());
                entity.setTicketStatus(model.getTicketsEntities().get(i).getTicketStatus());
                entity.setPayDate(model.getTicketsEntities().get(i).getPayDate());
                mlists.add(entity);
            }
            mAlllists.addAll(mlists);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
