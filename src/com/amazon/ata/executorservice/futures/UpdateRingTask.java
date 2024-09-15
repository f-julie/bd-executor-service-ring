package com.amazon.ata.executorservice.futures;

import com.amazon.ata.executorservice.coralgenerated.devicecommunication.UpdateDeviceFirmwareRequest;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.UpdateDeviceFirmwareResponse;
import com.amazon.ata.executorservice.devicecommunication.RingDeviceCommunicatorService;

import java.util.concurrent.Callable;

public class UpdateRingTask implements Callable<UpdateDeviceFirmwareResponse> {
    private UpdateDeviceFirmwareRequest request;
    private final RingDeviceCommunicatorService ringClient;

    public UpdateRingTask(RingDeviceCommunicatorService ringClient, UpdateDeviceFirmwareRequest request) {
        this.ringClient = ringClient;
        this.request = request;
    }

    @Override
    public UpdateDeviceFirmwareResponse call() {
        return ringClient.updateDeviceFirmware(this.request);
    }
}
