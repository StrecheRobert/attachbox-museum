package com.example.museumbeacons;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private TextView txtRoom;
    private TextView txtMessage;
    private Button btnRoom1;
    private Button btnRoom2;

    private BeaconManager beaconManager;
    private static final int PERMISSION_REQUEST_CODE = 123;

    // cheile beaconilor tăi (UUID:major:minor) - CONVERTITE LA lowercase
    private static final String BEACON1_KEY =
            "ae7101dd-7154-4561-bca1-091919bd7f40:46172:56958".toLowerCase();

    private static final String BEACON2_KEY =
            "b9407f30-f5f8-466e-aff9-25556b57fe6d:49668:48887".toLowerCase();

    private String makeKey(Beacon b) {
        return (b.getId1().toString() + ":" + b.getId2().toString() + ":" + b.getId3().toString()).toLowerCase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRoom = findViewById(R.id.txtRoom);
        txtMessage = findViewById(R.id.txtMessage);
        btnRoom1 = findViewById(R.id.btnRoom1);
        btnRoom2 = findViewById(R.id.btnRoom2);

        txtRoom.setText("Nu esti in nicio camera");
        txtMessage.setText("Apropie-te de o camera pentru a o detecta.");

        btnRoom1.setVisibility(View.GONE);
        btnRoom2.setVisibility(View.GONE);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(
                new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        );

        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            boolean hasBluetoothScan = true;
            boolean hasBluetoothConnect = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                hasBluetoothScan = checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED;
                hasBluetoothConnect = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED;
            }

            if (!hasLocation || !hasBluetoothScan || !hasBluetoothConnect) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                            },
                            PERMISSION_REQUEST_CODE
                    );
                } else {
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            PERMISSION_REQUEST_CODE
                    );
                }
                return;
            }
        }

        startBeaconScanning();
    }

    private void startBeaconScanning() {
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconManager != null) {
            beaconManager.unbind(this);
        }
    }

    private void setupRoomButton(Button button, final String beaconKey, String roomName, double distanceMeters) {
        button.setVisibility(View.VISIBLE);
        button.setText(roomName + String.format(" (%.1f m)", distanceMeters));

        button.setOnClickListener(v -> {
            // deschidem fereastra camerei corespunzătoare
            if (beaconKey.equals(BEACON1_KEY)) {
                Intent intent = new Intent(MainActivity.this, Room1Activity.class);
                startActivity(intent);
            } else if (beaconKey.equals(BEACON2_KEY)) {
                Intent intent = new Intent(MainActivity.this, Room2Activity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                runOnUiThread(() -> {
                    btnRoom1.setVisibility(View.GONE);
                    btnRoom2.setVisibility(View.GONE);

                    if (beacons == null || beacons.isEmpty()) {
                        txtRoom.setText("Nu esti in nicio camera");
                        txtMessage.setText("Niciun beacon detectat.");
                        return;
                    }

                    // filtrăm doar beaconii noștri
                    List<Beacon> knownBeacons = new ArrayList<>();
                    for (Beacon b : beacons) {
                        String key = makeKey(b);
                        if (key.equals(BEACON1_KEY) || key.equals(BEACON2_KEY)) {
                            knownBeacons.add(b);
                        }
                    }

                    if (knownBeacons.isEmpty()) {
                        txtRoom.setText("Beaconuri necunoscute");
                        txtMessage.setText("Nu exista camere configurate pentru beaconii detectati.");
                        return;
                    }

                    // sortăm după distanță (cel mai aproape primul)
                    Collections.sort(knownBeacons, new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon b1, Beacon b2) {
                            return Double.compare(b1.getDistance(), b2.getDistance());
                        }
                    });

                    txtRoom.setText("Camere detectate in apropiere");
                    txtMessage.setText("Alege camera pentru care vrei sa vezi detalii.");

                    // primul beacon -> butonul 1
                    Beacon b1 = knownBeacons.get(0);
                    String key1 = makeKey(b1);
                    if (key1.equals(BEACON1_KEY)) {
                        setupRoomButton(btnRoom1, key1, "Camera 1 – Denumire ", b1.getDistance());
                    } else if (key1.equals(BEACON2_KEY)) {
                        setupRoomButton(btnRoom1, key1, "Camera 2 – Denumire", b1.getDistance());
                    }

                    // al doilea beacon -> butonul 2 (dacă există)
                    if (knownBeacons.size() > 1) {
                        Beacon b2 = knownBeacons.get(1);
                        String key2 = makeKey(b2);
                        if (key2.equals(BEACON1_KEY)) {
                            setupRoomButton(btnRoom2, key2, "Camera 1 – Denumire", b2.getDistance());
                        } else if (key2.equals(BEACON2_KEY)) {
                            setupRoomButton(btnRoom2, key2, "Camera 2 – Denumire", b2.getDistance());
                        }
                    }
                });
            }
        });

        try {
            Region region = new Region("all-beacons-region", null, null, null);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startBeaconScanning();
            } else {
                txtRoom.setText("Permisiuni lipsa");
                txtMessage.setText("Trebuie permisiuni Location/Bluetooth pentru a detecta beaconi.");
            }
        }
    }
}
