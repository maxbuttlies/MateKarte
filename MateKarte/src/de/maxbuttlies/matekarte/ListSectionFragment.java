package de.maxbuttlies.matekarte;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ListView;

public class ListSectionFragment extends Fragment {

	HorizontalScrollView v;

	public ListSectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = (HorizontalScrollView) inflater.inflate(R.layout.fragment_list,
				container, false);

		DealerAsync dealerAsync = new DealerAsync();
		dealerAsync.execute("");
		return v;
	}

	private class DealerAsync extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(String... gps) {
			DealerAPI dealerAPI = new DealerAPI();

			try {
				return dealerAPI.getMapDataJSON();
			} catch (Exception e) {
				e.printStackTrace();
				return "-1_" + e.getMessage();
			}

		}

		protected void onPostExecute(String json) {

			List<Item> items = parseJSON(json);

			ListView lv = (ListView) v.findViewById(R.id.dealerList);

			List<String> tvs = new ArrayList<String>();
			for (Item item : items) {

				tvs.add(item.getName());
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1, tvs);

			lv.setAdapter(adapter);

		}
	}

	public List<Item> parseJSON(String json) {

		List<Item> items = new ArrayList<Item>();

		try {
			JSONObject jsonObject = new JSONObject(json);
			Log.i(MainActivity.class.getName(), "Number of entries "
					+ jsonObject.length());
			Iterator keys = jsonObject.keys();
			while (keys.hasNext()) {
				JSONObject obj = jsonObject.getJSONObject(keys.next()
						.toString());
				Item i = new Item();
				i.setName(obj.getString("n"));
				String coord = obj.getString("c");
				coord = coord.substring(1, coord.length() - 1);
				String[] c = coord.split(",");
				i.setLat(Double.valueOf(c[0]));
				i.setLon(Double.valueOf(c[1]));
				items.add(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}
}