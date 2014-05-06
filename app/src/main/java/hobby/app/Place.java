package hobby.app;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Papucs on 2014.04.07..
 */
public class Place implements Parcelable {

    private LatLng coord;
    private String address;
    private String name;
    private Double distance=0.0;
    private Double rating=0.0;

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public Place(){

    }

    public Place(Parcel p){
        coord = new LatLng(p.readDouble(),p.readDouble());
        address = p.readString();
        name=p.readString();
        distance=p.readDouble();
        rating=p.readDouble();
    }

    public LatLng getCoord() {
        return coord;
    }

    public void setCoord(double lat, double lng) {
        this.coord = new LatLng(lat, lng);
    }

    public String coordToString(){
        return coord.latitude+","+coord.longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(coord.latitude);
        dest.writeDouble(coord.longitude);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeDouble(distance);
        dest.writeDouble(rating);
    }
}
