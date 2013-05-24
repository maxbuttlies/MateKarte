package de.maxbuttlies.matekarte.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import de.maxbuttlies.matekarte.api.Dealer;

public class DealerOverlayItem extends OverlayItem {

	private Dealer dealer = null;

	public DealerOverlayItem(String aTitle, String aDescription,
			GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
	}

	public DealerOverlayItem(String aTitle, String aDescription,
			GeoPoint aGeoPoint, Dealer dealer) {
		super(aTitle, aDescription, aGeoPoint);
		this.dealer = dealer;
	}

	public Dealer getDealer() {
		return dealer;
	}

	public void setDealer(Dealer dealer) {
		this.dealer = dealer;
	}
}
