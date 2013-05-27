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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
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

	int zoom = 0;

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
			if (location == null) {
				AlertDialog dialog = new AlertDialog.Builder(getActivity())
						.create();
				dialog.setMessage("Konnte keine Koordinaten ermitteln. Wo bist Du denn?");
				final EditText place = new EditText(getActivity());
				dialog.setView(place);
				dialog.setTitle("GPS Fehler");
				dialog.setButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						refreshMap(place.getText(), mapView.getBoundingBox());
					}

				});
				dialog.show();
			} else {
				refreshMap(new GeoPoint(location));
			}

			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void refreshMap(Editable text, BoundingBoxE6 boundingBox) {
		PlaceSearchASync async = new PlaceSearchASync();
		async.execute("");
	}

	private void refreshMap(GeoPoint location) {

		// Überdnken, vielleicht nicht alle auf einmal laden, eher nur die
		// sichtbaren

		mapController.animateTo(location);

		DealerMarkerASync async = new DealerMarkerASync();
		async.execute("");
	}

	private class DealerMarkerASync extends
			AsyncTask<String, Void, List<Dealer>> {
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
		protected List<Dealer> doInBackground(String... s) {
			DealerAPI dealerAPI = new DealerAPI();

			try {
				return dealerAPI.getDealerList();
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
			mapView.refreshDrawableState();
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

		protected void onPostExecute(final Dealer dealer) {
			progressCircle.hide();
			AlertDialog alert = new AlertDialog.Builder(getActivity()).create();

			LayoutInflater inflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			LinearLayout layout = (LinearLayout) inflater.inflate(
					R.layout.dialog_dealerinfo, null);
			alert.setView(layout);

			alert.setTitle(dealer.getName());

			alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});

			alert.setButton(AlertDialog.BUTTON_NEUTRAL, "Route",
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(
									android.content.Intent.ACTION_VIEW,
									Uri.parse("geo:0,0?q=37.423156,-122.084917 ("
											+ dealer.getName() + ")"));
							getActivity().startActivity(intent);
						}
					});

			alert.show();
		}
	}

	private class PlaceSearchASync extends AsyncTask<String, Void, GeoPoint> {
		private ProgressDialog progressCircle = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressCircle = ProgressDialog
					.show(getActivity(),
							"Karte erstellen",
							"Ich suche gerade die die gewählte Stadt verwirre mich bitte nicht!",
							true, true);
			progressCircle.show();
		}

		@Override
		protected GeoPoint doInBackground(String... city) {
			DealerAPI dealerAPI = new DealerAPI();

			try {
				return dealerAPI.findCity(city[0]);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		protected void onPostExecute(GeoPoint geoPoint) {

			refreshMap(geoPoint);
			progressCircle.hide();

		}
	}

}