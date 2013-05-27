package de.maxbuttlies.matekarte.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.maxbuttlies.matekarte.R;
import de.maxbuttlies.matekarte.api.Dealer;
import de.maxbuttlies.matekarte.api.DealerAPI;

public class ListSectionFragment extends Fragment {

	LinearLayout v;

	public ListSectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = (LinearLayout) inflater.inflate(R.layout.fragment_list, container,
				false);

		DealerAsync dealerAsync = new DealerAsync();
		dealerAsync.execute("");
		return v;
	}

	private class DealerAsync extends AsyncTask<String, Void, List<Dealer>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected List<Dealer> doInBackground(String... gps) {
			DealerAPI dealerAPI = new DealerAPI();

			try {
				Map<String, String> search = new HashMap<String, String>();
				return dealerAPI.getDealerList(search);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		protected void onPostExecute(List<Dealer> dealer) {

			ListView lv = (ListView) v.findViewById(R.id.dealerList);

			List<String> tvs = new ArrayList<String>();
			for (Dealer item : dealer) {

				tvs.add(item.getName());
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1, tvs);

			lv.setAdapter(adapter);

		}
	}

}