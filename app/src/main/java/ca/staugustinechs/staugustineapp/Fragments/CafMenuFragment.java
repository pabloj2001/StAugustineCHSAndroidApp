package ca.staugustinechs.staugustineapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetCafMenuTask;
import ca.staugustinechs.staugustineapp.Objects.CafMenuItem;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_CafMenu;

public class CafMenuFragment extends Fragment {

    private GetCafMenuTask task, task2;
    private RecyclerView menu, menuRegular;
    private View layout, loadingCircle, cmDivider, offline;
    private TextView cmDailyHeader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cafmenu, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        layout = view.findViewById(R.id.cmLayout);
        loadingCircle = view.findViewById(R.id.cmLoadingCircle);
        offline = getLayoutInflater().inflate(R.layout.offline_layout, null);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        DateFormat date = new SimpleDateFormat("EEEE");
        String today = date.format(cal.getTime());

        menuRegular = view.findViewById(R.id.cmMenuRegular);
        cmDailyHeader = view.findViewById(R.id.cmDailyHeader);
        cmDivider = view.findViewById(R.id.cmDivider);

        if(today.equals("Saturday") || today.equals("Sunday")){
            cmDailyHeader.setVisibility(View.GONE);
            cmDivider.setVisibility(View.GONE);
            menuRegular.setPadding(0, 16, 0, 0);
        }else{
            cmDailyHeader.setText(today + " Menu");
            menu = view.findViewById(R.id.cmMenu);
            menu.setLayoutManager(new LinearLayoutManager(this.getContext()){
                @Override
                public boolean canScrollVertically(){
                    return false;
                }
            });

            task = new GetCafMenuTask(this, true);
            task.execute();
        }

        menuRegular.setLayoutManager(new LinearLayoutManager(this.getContext()){
            @Override
            public boolean canScrollVertically(){
                return false;
            }
        });

        task2 = new GetCafMenuTask(this, false);
        task2.execute();
    }

    public void updateMenu(List<CafMenuItem> items, boolean dailyMenu){
        if(items != null && AppUtils.isNetworkAvailable(this.getActivity())){
            RViewAdapter_CafMenu adapter = new RViewAdapter_CafMenu(items);
            if(dailyMenu){
                if(items.size() > 0){
                    menu.setAdapter(adapter);
                }else{
                    cmDailyHeader.setVisibility(View.GONE);
                    cmDivider.setVisibility(View.GONE);
                    menu.setVisibility(View.GONE);
                    menuRegular.setPadding(0, 16, 0, 0);
                }
            }else{
                menuRegular.setAdapter(adapter);
            }

            loadingCircle.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
            ((LinearLayout) ((ScrollView) getView()).getChildAt(0)).removeView(offline);
        }else{
            setOffline();
        }
    }

    public void setOffline(){
        layout.setVisibility(View.GONE);
        loadingCircle.setVisibility(View.GONE);

        task.cancel(true);
        task2.cancel(true);

        ((LinearLayout) ((ScrollView) getView()).getChildAt(0)).addView(offline);
    }

    @Override
    public void onDetach(){
        if(task != null){
            task.cancel(true);
        }
        super.onDetach();
    }
}
