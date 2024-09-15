package com.amazon.ata.executorservice.checker;

import com.amazon.ata.executorservice.coralgenerated.devicecommunication.GetDeviceSystemInfoRequest;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.GetDeviceSystemInfoResponse;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.RingDeviceFirmwareVersion;
import com.amazon.ata.executorservice.devicecommunication.RingDeviceCommunicatorService;
import com.amazon.ata.executorservice.util.KnownRingDeviceFirmwareVersions;

/**
 * A task to check a single device's version against a desired latest
 * version, requesting a firmware update if appropriate.
 *
 * PARTICIPANTS: Implement this class in Phase 1
 */
public class DeviceCheckTask implements Runnable {
    private RingDeviceCommunicatorService ringDeviceCommunicatorService;
    private DeviceChecker deviceChecker;
    private String deviceId;
    private RingDeviceFirmwareVersion targetVersion;

    /**
     * Constructs a DeviceCheckTask with the given dependencies and parameters.
     *
     * PARTICIPANTS: If you add constructor parameters, add them AFTER the DeviceChecker
     * argument. If you add parameters before the DeviceChecker, your tests will fail.
     *
     * @param deviceChecker The DeviceChecker to use while executing this task
     */
    public DeviceCheckTask(DeviceChecker deviceChecker, String deviceId, RingDeviceFirmwareVersion targetVersion) {
        this.ringDeviceCommunicatorService = deviceChecker.getRingDeviceCommunicatorService();
        this.deviceChecker = deviceChecker;
        this.deviceId = deviceId;
        this.targetVersion = targetVersion;
    }

    @Override
    public void run() {
        // get device's firmware version
        GetDeviceSystemInfoRequest request = GetDeviceSystemInfoRequest.builder().withDeviceId(this.deviceId).build();
        GetDeviceSystemInfoResponse response = this.ringDeviceCommunicatorService.getDeviceSystemInfo(request);
        RingDeviceFirmwareVersion firmwareVersion = response.getSystemInfo().getDeviceFirmwareVersion();
        // decide if it is out of date
        if (KnownRingDeviceFirmwareVersions.needsUpdate(firmwareVersion, this.targetVersion)) {
            // update device
            this.deviceChecker.updateDevice(this.deviceId, this.targetVersion);

        }
    }
}
