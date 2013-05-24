package de.maxbuttlies.matekarte.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.maxbuttlies.matekarte.R;
import de.maxbuttlies.matekarte.api.Dealer;
import de.maxbuttlies.matekarte.api.DealerAPI;

public class MapSectionFragment extends Fragment {
	private LinearLayout v;
	MapView mapView;
	private MapController mapController;

	public MapSectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		try {
			v = (LinearLayout) inflater.inflate(R.layout.fragment_map,
					container, false);

			mapView = (MapView) v.findViewById(R.id.openmapview);
			mapView.setMultiTouchControls(true);
			mapView.setBuiltInZoomControls(true);
			mapController = mapView.getController();
			mapController.setZoom(13);

			LocationManager locationManager = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);
			String provider = locationManager.getBestProvider(new Criteria(),
					true);

			Location location = locationManager.getLastKnownLocation(provider);
			List<String> providerList = locationManager.getAllProviders();
			if (location == null) {
				AlertDialog dialog = new AlertDialog.Builder(getActivity())
						.create();
				dialog.setMessage("Konnte keine Koordinaten ermitteln. Wo bist Du denn?");
				final EditText place = new EditText(getActivity());
				// dialog.addContentView(place, null);
				dialog.setTitle("GPS Fehler");
				dialog.setButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getActivity(), place.getText(),
								Toast.LENGTH_LONG).show();
						refreshMap(new GeoPoint(50.745146, 7.098541),
								mapView.getBoundingBox());
					}
				});
				dialog.show();
			} else {
				refreshMap(new GeoPoint(location), mapView.getBoundingBox());
			}

			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void refreshMap(GeoPoint location, BoundingBoxE6 bbox) {

		// Überdnken, vielleicht nicht alle auf einmal laden, eher nur die
		// sichtbaren

		mapController.animateTo(location);

		DealerMarkerASync async = new DealerMarkerASync();
		async.execute(bbox);
	}

	private class DealerMarkerASync extends
			AsyncTask<BoundingBoxE6, Void, List<Dealer>> {
		private ProgressDialog progressCircle = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressCircle = ProgressDialog
					.show(getActivity(),
							"Karte erstellen",
							"Ich suche Dir gerade die Händler in nächster Nähe, das ist harte Arbeit!",
							true, true);
			progressCircle.show();
		}

		@Override
		protected List<Dealer> doInBackground(BoundingBoxE6... bbox) {
			DealerAPI dealerAPI = new DealerAPI();

			try {
				return dealerAPI.getDealerList(bbox[0]);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		protected void onPostExecute(List<Dealer> dealers) {

			ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

			try {
				for (Dealer dealer : dealers) {
					DealerOverlayItem myLocationOverlayItem = new DealerOverlayItem(
							dealer.getName(), "Hier soll die Adresse stehen",
							new GeoPoint(dealer.getLat(), dealer.getLon()),
							dealer);
					Drawable myCurrentLocationMarker = getActivity()
							.getResources().getDrawable(R.drawable.marker);
					myLocationOverlayItem.setMarker(myCurrentLocationMarker);
					items.add(myLocationOverlayItem);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ResourceProxy resourceProxy = new DefaultResourceProxyImpl(
					getActivity());
			ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
					items,
					new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
						public boolean onItemSingleTapUp(final int index,
								final OverlayItem item) {
							if (item instanceof DealerOverlayItem) {
								new DealerInfoASync()
										.execute(((DealerOverlayItem) item)
												.getDealer().getId());
							} else {
								Toast.makeText(getActivity(),
										"§da war ein fehler", Toast.LENGTH_LONG)
										.show();
							}
							return true;
						}

						public boolean onItemLongPress(final int index,
								final OverlayItem item) {
							return true;
						}
					}, resourceProxy);
			mapView.getOverlays().add(currentLocationOverlay);
			progressCircle.hide();

		}
	}

	private class DealerInfoASync extends AsyncTask<String, Void, Dealer> {

		private ProgressDialog progressCircle = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressCircle = ProgressDialog.show(getActivity(),
					"Dealer Informationen laden",
					"Ich bin am Laden, störe bitte mich nicht!", true, true);
			progressCircle.show();
		}

		@Override
		protected Dealer doInBackground(String... id) {
			DealerAPI dealerAPI = new DealerAPI();

			try {
				return dealerAPI.getDealer(id[0]);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		protected void onPostExecute(Dealer dealer) {
			progressCircle.hide();
			AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
			alert.setTitle(dealer.getName());
			alert.setMessage(dealer.getStreetNo() + "\n" + dealer.getZip()
					+ " " + dealer.getCity() + "\n\n" + dealer.getPhone()
					+ "\n" + dealer.getWeb());
			alert.setButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			});
			alert.show();
		}
	}
}