    package com.example.artivatic.rapido;

    import android.Manifest;
    import android.app.ProgressDialog;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.location.Criteria;
    import android.location.Location;
    import android.location.LocationManager;
    import android.net.http.HttpResponseCache;
    import android.os.AsyncTask;
    import android.support.v4.app.ActivityCompat;
    import android.support.v4.content.ContextCompat;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.Menu;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.AutoCompleteTextView;
    import android.widget.Button;
    import android.widget.SimpleAdapter;
    import android.widget.Toast;

    import com.google.android.gms.location.places.Place;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.MapFragment;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.BitmapDescriptorFactory;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.gms.maps.model.PolylineOptions;

    import org.apache.http.HttpResponse;
    import org.apache.http.client.HttpClient;
    import org.apache.http.client.methods.HttpPost;
    import org.apache.http.impl.client.DefaultHttpClient;
    import org.apache.http.protocol.BasicHttpContext;
    import org.apache.http.protocol.HttpContext;
    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;
    import org.w3c.dom.Document;
    import org.w3c.dom.Node;
    import org.w3c.dom.NodeList;

    import java.io.BufferedInputStream;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.UnsupportedEncodingException;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.net.URLEncoder;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;

    import javax.xml.parsers.DocumentBuilder;
    import javax.xml.parsers.DocumentBuilderFactory;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;
    import retrofit2.Retrofit;

    import static android.R.attr.logo;
    import static android.R.attr.type;

    public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
        private GoogleMap mMap;
        Location mCurrentLocation;
        double latitude;
        Document document;
        LatLng fromPosition;
        LatLng toPosition;
        GMapV2GetRouteDirection v2GetRouteDirection;
        double longitude;
        Button btnRestaurant;
        ParsingData parsingData;
      //  PlacesTask placesTask;
      ResponseData responseData;
        MarkerOptions markerOptions;
        CustomAutoCompleteTextView autoCompleteTextView;
        private int PROXIMITY_RADIUS = 10000;
       PlacesTask placesTask;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            try {
                // Loading map
                initilizeMap();

            } catch (Exception e) {
                e.printStackTrace();
            }
            initViews();
            searchFunction();
            v2GetRouteDirection=new GMapV2GetRouteDirection();


            btnRestaurant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    build_retrofit_and_get_response("restaurant");
                }
            });


        }

        private void searchFunction() {



            autoCompleteTextView.setThreshold(1);

            autoCompleteTextView.addTextChangedListener(new TextWatcher() {



                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    placesTask = new PlacesTask();
                    placesTask.execute(s.toString());

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            autoCompleteTextView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    autoCompleteTextView.setText("");
                }
            });

        }


        private void initViews() {
            autoCompleteTextView=(CustomAutoCompleteTextView) findViewById(R.id.tv_Search);
            btnRestaurant = (Button) findViewById(R.id.btnRestaurant);
        }

        private void build_retrofit_and_get_response(String school) {




            Call<Example> call = new RestClient().getApiService().getNearbyPlaces(type, mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude()
                    , PROXIMITY_RADIUS);
            call.enqueue(new Callback<Example>() {
                @Override
                public void onResponse(Call<Example> call, Response<Example> response) {
                    try {
                        mMap.clear();
                        // This loop will go through all the results and add marker on each location.
                        for (int i = 0; i < response.body().getResults().size(); i++) {
                            //response.body().getResults().get(i).

                            Double lat = Double.parseDouble(response.body().getResults().get(i).getGeometry().getLocation().getLat());
                            Double lng = Double.parseDouble(response.body().getResults().get(i).getGeometry().getLocation().getLng());
                         //   autoCompleteTextView.setAdapter(new GeoAutoCompleteAdapter(MainActivity.this));
                            String placeName = response.body().getResults().get(i).getName();
                            String vicinity = response.body().getResults().get(i).getVicinity();
                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng latLng = new LatLng(lat, lng);
                            // Position of Marker on Map
                            markerOptions.position(latLng);
                            // Adding Title to the Marker
                            markerOptions.title(placeName + " : " + vicinity);
                            // Adding Marker to the Camera.
                            Marker m = mMap.addMarker(markerOptions);
                            // Adding colour to the marker
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            // move map camera
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                        }
                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Example> call, Throwable t) {
                    Log.d("onFailure", t.toString());
                }
            });



        }

        private void initilizeMap() {
            SupportMapFragment mapFragment = (SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map));
            mapFragment.getMapAsync(this);
        }

        @Override
        protected void onResume() {
            super.onResume();
            initilizeMap();
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Add a marker in Sydney and move the camera
            if (mMap != null) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

                } else {

                }
                mMap.setMyLocationEnabled(true);

                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.setTrafficEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    //
                LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);//use of location services by firstly defining location manager.
                String provider = lm.getBestProvider(new Criteria(), true);

                if (provider == null) {
    //               ;
                }

                if(provider==null)
                {
                    Toast.makeText(getApplicationContext(),"Please open the location ppermission for device your device",Toast
                    .LENGTH_SHORT).show();
                    finish();

                }else
                {
                    mCurrentLocation = lm.getLastKnownLocation(provider);


                    if (mCurrentLocation != null) {
                        onLocationChanged(mCurrentLocation);

                    }
                    markerOptions = new MarkerOptions();
                    fromPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    //



                }


            }

        }

        private void onLocationChanged(Location mCurrentLocation) {
            {

                final LatLng latlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());// This methods gets the users current longitude and latitude.

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, (float) 10));//Moves the camera to users current longitude and latitude





            }
        }


            private class GetRouteTask extends AsyncTask<String, Void, String> {

                private ProgressDialog Dialog;
                String response = "";

                @Override
                protected void onPreExecute() {
                    Dialog = new ProgressDialog(MainActivity.this);
                    Dialog.setMessage("Loading route...");
                    Dialog.show();
                }

                @Override
                protected String doInBackground(String... urls) {
                    //Get All Route values
                    document = v2GetRouteDirection.getDocument(fromPosition, toPosition, GMapV2GetRouteDirection.MODE_DRIVING);
                    response = "Success";
                    return response;

                }

                @Override
                protected void onPostExecute(String result) {
                    mMap.clear();
                    if (response.equalsIgnoreCase("Success")) {
                        ArrayList<LatLng> directionPoint = v2GetRouteDirection.getDirection(document);
                        PolylineOptions rectLine = new PolylineOptions().width(10).color(
                                Color.BLUE);

                        for (int i = 0; i < directionPoint.size(); i++) {
                            rectLine.add(directionPoint.get(i));
                        }
                        // Adding route on the map
                        mMap.addPolyline(rectLine);
                        markerOptions.position(fromPosition);
                        markerOptions.position(toPosition);
                        markerOptions.draggable(true);
                        mMap.addMarker(markerOptions);

                    }

                    Dialog.dismiss();
                }



        }


        private class GMapV2GetRouteDirection {
            public final static String MODE_DRIVING = "driving";
            public final static String MODE_WALKING = "walking";

            public Document getDocument(LatLng start, LatLng end, String mode) {
                String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                        + "origin=" + start.latitude + "," + start.longitude
                        + "&destination=" + end.latitude + "," + end.longitude
                        + "&sensor=false&units=metric&mode=driving";

                try {

                    URL url1 = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection)url1.openConnection();
                    connection.setRequestProperty("User-Agent", "");
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.connect();
                    System.out.println("Response Code: " + connection.getResponseCode());
                    InputStream in = new BufferedInputStream(connection.getInputStream());

                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.parse(in);

                    return doc;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            public ArrayList<LatLng> getDirection(Document doc) {
                {
                    NodeList nl1, nl2, nl3;
                    ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
                    nl1 = doc.getElementsByTagName("step");
                    if(nl1!=null)
                    if (nl1.getLength() > 0) {
                        for (int i = 0; i < nl1.getLength(); i++) {
                            Node node1 = nl1.item(i);
                            nl2 = node1.getChildNodes();

                            Node locationNode = nl2.item(getNodeIndex(nl2, "start_location"));
                            nl3 = locationNode.getChildNodes();
                            Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                            double lat = Double.parseDouble(latNode.getTextContent());
                            Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                            double lng = Double.parseDouble(lngNode.getTextContent());
                            listGeopoints.add(new LatLng(lat, lng));

                            locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                            nl3 = locationNode.getChildNodes();
                            latNode = nl3.item(getNodeIndex(nl3, "points"));
                            ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                            for (int j = 0; j < arr.size(); j++) {
                                listGeopoints.add(new LatLng(arr.get(j).latitude, arr.get(j).longitude));
                            }

                            locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                            nl3 = locationNode.getChildNodes();
                            latNode = nl3.item(getNodeIndex(nl3, "lat"));
                            lat = Double.parseDouble(latNode.getTextContent());
                            lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                            lng = Double.parseDouble(lngNode.getTextContent());
                            listGeopoints.add(new LatLng(lat, lng));
                        }
                    }

                    return listGeopoints;
                }

            }
        }
        private int getNodeIndex(NodeList nl, String nodename) {
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeName().equals(nodename))
                    return i;
            }
            return -1;
        }
        private ArrayList<LatLng> decodePoly(String encoded) {
            ArrayList<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;
                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
                poly.add(position);
            }
            return poly;
        }

            private class ParsingData extends AsyncTask<String, Integer, List<HashMap<String,String>>> {

                JSONObject jObject;

                @Override
                protected List<HashMap<String, String>> doInBackground(String... jsonData) {

                    List<HashMap<String, String>> places = null;

                    PlaceJSONParser placeJsonParser = new PlaceJSONParser();

                    try {


                        jObject=new JSONObject(jsonData[0]);

                        places = placeJsonParser.parse(jObject);

                    } catch (Exception e) {
                        Log.d("Exception_dattttt", e.toString());
                    }
                    return places;
                }

                @Override
                protected void onPostExecute(final List<HashMap<String, String>> result) {
                   // mPlaces = result;



                        String[] from = new String[]{"description"};
                        int[] to = new int[]{android.R.id.text1};



                        // Creating a SimpleAdapter for the AutoCompleteTextView
                        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);


                        autoCompleteTextView.setAdapter(adapter);
        //
                        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                                    long arg3) {

                                Call<ResponseData> call = new RestClient().getApiService().getPlaceDetail(result.get(position).get("place_id"));
                                call.enqueue(new Callback<ResponseData>() {
                                    @Override
                                    public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {

                                         responseData=new ResponseData();
                                        responseData=response.body();



                                        Double lat = Double.parseDouble(responseData.getResult().getGeometry().getLocation().getLat());
                                        Double lng = Double.parseDouble(responseData.getResult().getGeometry().getLocation().getLng());
                                        //   autoCompleteTextView.setAdapter(new GeoAutoCompleteAdapter(MainActivity.this));


                                        toPosition = new LatLng(lat,lng);
                                GetRouteTask getRoute = new GetRouteTask();
                                getRoute.execute();

                                    }

                                    @Override
                                    public void onFailure(Call<ResponseData> call, Throwable t) {
                                        Toast.makeText(getApplicationContext()
                                        ,"Not Able To Get The Location",Toast.LENGTH_SHORT).show();

                                    }
                                });



//



                            }
                        });
                    }
                }





            private class PlacesTask extends AsyncTask<String, Void, String>{

                @Override
                protected String doInBackground(String... place) {
                    // For storing data from web service
                    String data = "";


                    // Obtain browser key from https://code.google.com/apis/console
                    String key = "AIzaSyBCQCxI6tR2vDEyLE2oZUJ_GOIbzCkgfEM";

                    String input="";

                    try {
                        input = "input=" + URLEncoder.encode(place[0], "utf-8");
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    // place type to be searched
                    String types = "types=geocode";

                    // Sensor enabled
                    String sensor = "sensor=false";

                    // Building the parameters to the web service
                    String parameters = input+"&"+types+"&"+sensor+"&"+"key="+key;

                    // Output format
                    String output = "json";


                    // Building the url to the web service
                    String url = "https://maps.googleapis.com/maps/api/place/queryautocomplete/"+output+"?"+parameters;

                    try{
                        // Fetching the data from we service
                       data = downloadUrl(url);
                    }catch(Exception e){

                    }

                    return data;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);

                    // Creating ParserTask
                    parsingData = new ParsingData();


                    // Starting Parsing the JSON string returned by Web Service
                    parsingData.execute(result);
                }

        }


            private String downloadUrl(String strUrl) throws IOException {

                String data = "";
                InputStream iStream = null;
                HttpURLConnection urlConnection = null;
                try{
                    URL url = new URL(strUrl);

                    // Creating an http connection to communicate with url
                    urlConnection = (HttpURLConnection) url.openConnection();

                    // Connecting to url
                    urlConnection.connect();

                    // Reading data from url
                    iStream = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                    StringBuffer sb = new StringBuffer();

                    String line = "";
                    while( ( line = br.readLine()) != null){
                        sb.append(line);
                    }

                    data = sb.toString();

                    br.close();

                }catch(Exception e){

                }finally{
                    iStream.close();
                    urlConnection.disconnect();
                }
                return data;

        }


            public class PlaceJSONParser {

                /** Receives a JSONObject and returns a list */
                public List<HashMap<String,String>> parse(JSONObject jObject){

                    JSONArray jPlaces = null;
                    try {
                        /** Retrieves all the elements in the 'places' array */
                        jPlaces = jObject.getJSONArray("predictions");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /** Invoking getPlaces with the array of json object
                     * where each json object represent a place
                     */
                    return getPlaces(jPlaces);
                }

                private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
                    int placesCount = jPlaces.length();
                    List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
                    HashMap<String, String> place = null;

                    /** Taking each place, parses and adds to list object */
                    for(int i=0; i<placesCount;i++){
                        try {
                            /** Call getPlace with place JSON object to parse the place */
                            place = getPlace((JSONObject)jPlaces.get(i));
                            placesList.add(place);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    return placesList;
                }

                /** Parsing the Place JSON object */
                private HashMap<String, String> getPlace(JSONObject jPlace){

                    HashMap<String, String> place = new HashMap<String, String>();

                    String id="";
                    String reference="";
                    String description="";
                    String placeId="";
                    String latitude="";
                    String longitude="";

                    try {

                        description = jPlace.getString("description");
                        id = jPlace.getString("id");
                        reference = jPlace.getString("reference");
                        placeId=jPlace.getString("place_id");


                        place.put("description", description);
                        place.put("_id",id);
                        place.put("reference",reference);
                        place.put("place_id",placeId);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return place;
                }

        }
    }
