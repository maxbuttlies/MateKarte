package de.maxbuttlies.matekarte.list;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.BoundingBoxE6;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import de.maxbuttlies.matekarte.R;
import de.maxbuttlies.matekarte.api.Dealer;
import de.maxbuttlies.matekarte.api.DealerAPI;

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
		// dealerAsync.execute("");
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
				return dealerAPI.getDealerList(new BoundingBoxE6(0, 0, 0, 0));
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