package com.eventify.xgrenar.eventify;

import android.app.Activity;
import android.util.Log;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeaconUtility {

    private static final String TAG = "BeaconUtility";
    private BeaconManager beaconManager;
    private BeaconRegion beaconRegion;

    private Activity activity;

    private List<BeaconDefinition> listOfBeacons = new ArrayList<>();

    private boolean ranging;
    private boolean monitoring;

    public BeaconUtility(Activity activity) {
        this.activity = activity;
        beaconManager = new BeaconManager(activity);
    }

    public void startRanging(){
        SystemRequirementsChecker.checkWithDefaultDialogs(activity);
        beaconRegion = new BeaconRegion("Renging", null,  null, null);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                Log.i(TAG, "Service connected");
                beaconManager.startRanging(beaconRegion);
                ranging = true;
            }
        });

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                for (Beacon beacon : beacons){
                    for (BeaconDefinition beaconDefinition : listOfBeacons){
                        if (beacon.getMajor() == beaconDefinition.getMajorNumber() && beacon.getMinor() == beaconDefinition.getMinorNumber() && !beaconDefinition.isCodeExecuted()){
                            //Nasel jsem svuj beacon
                            beaconDefinition.execute();
                            beaconDefinition.setCodeExecuted(true);
                        }
                    }
                }
            }
        });
    }

    public void addBeacon(BeaconDefinition beaconDefinition){
        listOfBeacons.add(beaconDefinition);
    }

    public void stopRanging(){
        if (ranging){
            beaconManager.stopRanging(beaconRegion);
            ranging = false;
        }
    }

    public void startMonitoring(String identifier, UUID uuid, Integer major, Integer minor){
        SystemRequirementsChecker.checkWithDefaultDialogs(activity);
        beaconRegion = new BeaconRegion(identifier, uuid, major, minor);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(beaconRegion);
                monitoring = true;
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                for (Beacon beacon : beacons){
                    for (BeaconDefinition beaconDefinition : listOfBeacons){
                        if (beacon.getMajor() == beaconDefinition.getMajorNumber() && beacon.getMinor() == beaconDefinition.getMinorNumber() && !beaconDefinition.isCodeExecuted()){
                            //Nasel jsem svuj beacon
                            //Log.i("iBeacon", "Nasel jsem iBeacon: " + beacon.getMajor());
                            beaconDefinition.execute();
                            beaconDefinition.setCodeExecuted(true);
                        }
                    }
                }
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion) {
                
            }
        });
    }

    public void stopMonitoring(){
        if (monitoring){
            beaconManager.stopMonitoring(beaconRegion.getIdentifier());
            monitoring = false;
        }
    }
}
