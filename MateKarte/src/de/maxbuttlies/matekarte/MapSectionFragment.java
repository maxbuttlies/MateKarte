package de.maxbuttlies.matekarte;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MapSectionFragment extends Fragment {
	private LinearLayout v;
	private MapView mapView;
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
			mapController.setZoom(16);

			LocationManager lm = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);
			Location location = lm
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
						refreshMap(new GeoPoint(50.745146, 7.098541));
					}
				});
				dialog.show();
			} else {
				refreshMap(new GeoPoint(50.745146, 7.098541));
			}

			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void refreshMap(GeoPoint location) {

		mapController.animateTo(location);

		OverlayItem myLocationOverlayItem = new OverlayItem("Here",
				"Current Position", location);
		Drawable myCurrentLocationMarker = this.getResources().getDrawable(
				R.drawable.ic_launcher);
		myLocationOverlayItem.setMarker(myCurrentLocationMarker);

		final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		items.add(myLocationOverlayItem);
		ResourceProxy resourceProxy = new DefaultResourceProxyImpl(
				getActivity());
		ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
				items,
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
					public boolean onItemSingleTapUp(final int index,
							final OverlayItem item) {
						return true;
					}

					public boolean onItemLongPress(final int index,
							final OverlayItem item) {
						return true;
					}
				}, resourceProxy);
		this.mapView.getOverlays().add(currentLocationOverlay);
	}
}