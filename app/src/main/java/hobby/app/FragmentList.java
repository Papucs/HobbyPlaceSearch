package hobby.app;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by Papucs on 2014.06.04..
 */
public class FragmentList extends Fragment {

    private String title;
    private int pos;
    private ListView lv;

    public static FragmentList newInstance(int pos, String title){
        FragmentList fl = new FragmentList();
        Bundle args = new Bundle();
        args.putInt("position", pos);
        args.putString("title", title);
        fl.setArguments(args);
        return fl;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pos = getArguments().getInt("position", 0);
        title = getArguments().getString("title");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

         lv = (ListView) view.findViewById(R.id.fragmentList);
        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        lv.setBackgroundResource(R.drawable.list_bgr);
        return view;
    }
    public ListView getlist(){
        return lv;
    }



}
