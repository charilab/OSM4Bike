package jp.ne.sakura.charilab.osm4bike;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import jp.ne.sakura.charilab.maplibs.trackLogger.trackDatabase;
import jp.ne.sakura.charilab.maplibs.trackLogger.trackLogger;
import jp.ne.sakura.charilab.maplibs.GeoDatabase.OSMDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/*
MIT License

Copyright (c) 2017 Shunji Uno <uno@charilab.sakura.ne.jp>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*//**
 * Created by uno on 16/05/12.
 */

enum menuType {OVERLAY, MENU, DIRECT};

public class SettingFragment extends ListFragment {
    trackLogger.binder logger;
    Config config;
    MapFragment map;
    List<menuItem> menu_items;

/*
    private String menu_items[] = {
            "表示設定",
            "既登録情報表示",
            "POI情報取得",
            "ルートデータ取得"
    };

    private String menu_items_poi[] = {
            "コンビニ",
            "信号機",
            "道の駅・トイレ",
            "峠・トンネル"
    };

    private String menu_items_overlay[] = {
            "Strava heatmap",
            "雨雲マップ",
            //"気温マップ",
            "ルート"
    };

    private String menu_items_basemap[] = {
            "Charilog Standard",
            "Charilog City",
            "Mapbox Charilog",
            "OpenStreetMap",
            "OpenCycleMap"
    };
*/

    public SettingFragment() {
        Log.d("DEBUG", "SettingFragment is created.");
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        MainActivity activity = (MainActivity) getActivity();
        logger = activity.logger;
        config = activity.config;
        map = activity.map;
    }

    private List<menuItem> getMenuList() {
        List<menuItem> list = new ArrayList<>();
        list.add(new menuItem("コンビニ", menuType.OVERLAY, "convenience"));
        list.add(new menuItem("信号機", menuType.OVERLAY, "signals"));
        list.add(new menuItem("道の駅・トイレ", menuType.OVERLAY, "toilets"));
        list.add(new menuItem("峠・トンネル", menuType.OVERLAY, "mountain pass"));
        list.add(new menuItem("その他", menuType.OVERLAY, "misc"));
        list.add(new menuItem("My POI", menuType.OVERLAY, "collection"));
        list.add(new menuItem("Strava heatmap", menuType.OVERLAY, "heatmap"));
        list.add(new menuItem("雨雲マップ", menuType.OVERLAY, "rainmap"));
        list.add(new menuItem("ルート", menuType.OVERLAY, "route"));
        list.add(new menuItem("Download POI data", menuType.DIRECT, "download"));
        list.add(new menuItem("export track data", menuType.DIRECT, "export"));
        //list.add(new menuItem("画面設定", menuType.MENU, null));
        //list.add(new menuItem("オーバーレイ設定", menuType.MENU, null));
        //list.add(new menuItem("ベースマップ設定", menuType.MENU, null));
        //list.add(new menuItem("Alert設定", menuType.MENU, null));
        //list.add(new menuItem("データ管理", menuType.MENU, null));
        return list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("DEBUG", "SettingFragment.onCreateView is called.");
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        menu_items = getMenuList();
        setListAdapter(new ListViewAdapter(getContext(), R.layout.item_setting, menu_items));
        return rootView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("DEBUG", "changing overlay status.");
        CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.setting_item);
        menuItem item = menu_items.get(position);
        switch (item.type) {
            case OVERLAY:
                if (ctv.isChecked()) {
                    ctv.setChecked(false);
                    config.overlayStatus.put(item.cmd, false);
                    Log.d("DEBUG", "Disable " + item.cmd + " overlay.");
                } else {
                    ctv.setChecked(true);
                    config.overlayStatus.put(item.cmd, true);
                    Log.d("DEBUG", "Enable " + item.cmd + " overlay.");
                }
                map.redrawOverlay();
                break;
            case MENU:
                break;
            case DIRECT:
                if (item.cmd.equals("download")) {
                    OSMDatabaseHelper helper = new OSMDatabaseHelper(getContext(), "poi.db");
                    helper.downloadDatabase();
                } else if (item.cmd.equals("export")) {
                    trackDatabase.export();
                    Toast.makeText(getContext(), "Track data was stored into SDcard.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class menuItem {
        public String title;
        public menuType type;
        public String cmd;

        public menuItem(String title_str, menuType menutype, String arg) {
            title = title_str;
            type = menutype;
            cmd = arg;
        }
    }

    public class ListViewAdapter extends ArrayAdapter<menuItem> {
        private LayoutInflater inflater;
        private int itemLayout;

        public ListViewAdapter(Context context, int itemLayout, List<menuItem> list) {
            super(context, 0, list);
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.itemLayout = itemLayout;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView ctv;
            if (convertView == null) {
                convertView = inflater.inflate(itemLayout, parent, false);
                ctv = (CheckedTextView) convertView.findViewById(R.id.setting_item);
                convertView.setTag(ctv);
            } else {
                ctv = (CheckedTextView) convertView.getTag();
            }

            menuItem item = getItem(position);
            ctv.setText(item.title);
            switch (item.type) {
                case OVERLAY:
                    ctv.setChecked(config.overlayStatus.get(item.cmd));
                    break;
                case MENU:
                case DIRECT:
                    break;
            }
            return convertView;
        }
    }
}
