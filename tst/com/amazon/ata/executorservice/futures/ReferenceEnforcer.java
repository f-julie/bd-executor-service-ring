package com.amazon.ata.executorservice.futures;

import com.amazon.ata.executorservice.coralgenerated.customer.GetCustomerDevicesRequest;
import com.amazon.ata.executorservice.coralgenerated.customer.GetCustomerDevicesResponse;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.GetDeviceSystemInfoRequest;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.GetDeviceSystemInfoResponse;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.RingDeviceFirmwareVersion;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.RingDeviceSystemInfo;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.UpdateDeviceFirmwareRequest;
import com.amazon.ata.executorservice.coralgenerated.devicecommunication.UpdateDeviceFirmwareResponse;
import com.amazon.ata.executorservice.customer.CustomerService;
import com.amazon.ata.executorservice.devicecommunication.RingDeviceCommunicatorService;
import com.amazon.ata.executorservice.util.KnownRingDeviceFirmwareVersions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Private, iterative version of the ComplianceEnforcer, created so that we can make comparisons.
 */
class ReferenceEnforcer {
    private final CustomerService customerService;
    private final RingDeviceCommunicatorService ringClient;
    private final ExecutorService executorService;

    /**
     * Constructor taking the services required by the UpdateFinder.
     * @param customerService The CustomerService client.
     * @param ringClient The RingDeviceCommunicatorService client.
     */
    public ReferenceEnforcer(CustomerService customerService, RingDeviceCommunicatorService ringClient) {
        this.customerService = customerService;
        this.ringClient = ringClient;
        executorService = null;
    }

    /**
     * Finds all the devices owned by the specified customer that are not updated to at least
     * the specified firmware version.
     * @param customerId The customer to find devices for.
     * @param approved The minimum approved version to compare with.
     * @return A list of non-compliant device IDs. If all the customer's devices are up-to-date,
     *         this list will be empty. It will never be null.
     */
    public List<String> findUpdatesForCustomer(String customerId, RingDeviceFirmwareVersion approved) {
        // Find all the Ring devices this customer owns
        List<String> deviceIds = getCustomerDevices(customerId);

        // Find the version of each device
        List<RingDeviceSystemInfo> deviceInfo = getInfoForDevices(deviceIds);

        // Check whether each device meets the minimum approved version
        List<String> nonCompliantDevices = collectNonCompliantDevices(deviceInfo, approved);

        // Return the list of non-compliant devices. May be empty. Never null.
        return nonCompliantDevices;
    }

    /**
     * Remotely triggers an update for the provided devices.
     * @param nonCompliantDeviceIds The list of devices to update.
     * @param latest The firmware version to update to.
     * @return A list of devices that could not be updated. If all updates were successful,
     *         this list will be empty. It will never be null.
     */
    public List<String> updateDevices(List<String> nonCompliantDeviceIds, RingDeviceFirmwareVersion latest) {

        // Update all the devices and keep track of their update responses.
        List<UpdateDeviceFirmwareResponse> updateStatuses = triggerUpdates(nonCompliantDeviceIds, latest);

        // Collect all the unsuccessful device IDs.
        List<String> unsuccessfulDevices = collectUnsuccessfulUpdates(updateStatuses);

        return unsuccessfulDevices;
    }

    /**
     * Helper method that retrieves all the devices for a single customer.
     */
    private List<String> getCustomerDevices(String customerId) {
        GetCustomerDevicesResponse response = customerService.getCustomerDevices(
                GetCustomerDevicesRequest.builder()
                        .withCustomerId(customerId)
                        .build());
        return response.getDeviceIds();
    }

    /**
     * Helper method that gets the system info for the provided devices.
     */
    private List<RingDeviceSystemInfo> getInfoForDevices(List<String> deviceIds) {
        List<RingDeviceSystemInfo> deviceInfo = new ArrayList<>();
        for (String deviceId : deviceIds) {
            GetDeviceSystemInfoRequest versionRequest = GetDeviceSystemInfoRequest.builder()
                    .withDeviceId(deviceId)
                    .build();
            GetDeviceSystemInfoResponse infoResponse = ringClient.getDeviceSystemInfo(versionRequest);
            deviceInfo.add(infoResponse.getSystemInfo());
        }
        return deviceInfo;
    }

    /**
     * Helper method that updates devices to a given version.
     */
    private List<String> collectNonCompliantDevices(List<RingDeviceSystemInfo> deviceInfo,
                                                   RingDeviceFirmwareVersion approved)
    {
        List<String> nonCompliantDevices = new ArrayList<>();
        for (RingDeviceSystemInfo info : deviceInfo) {
            RingDeviceFirmwareVersion version = info.getDeviceFirmwareVersion();
            if (KnownRingDeviceFirmwareVersions.needsUpdate(version, approved)) {
                nonCompliantDevices.add(info.getDeviceId());
            }
        }
        return nonCompliantDevices;
    }

    /**
     * Helper method that triggers updates for all the devices to the given version.
     */
    private List<UpdateDeviceFirmwareResponse> triggerUpdates(List<String> nonCompliantDeviceIds,
                                                              RingDeviceFirmwareVersion latest)
    {
        List<UpdateDeviceFirmwareResponse> updateStatuses = new ArrayList<>();
        for (String deviceId : nonCompliantDeviceIds) {
            UpdateDeviceFirmwareRequest updateRequest = UpdateDeviceFirmwareRequest.builder()
                    .withDeviceId(deviceId)
                    .withVersion(latest)
                    .build();
            UpdateDeviceFirmwareResponse updateResponse = ringClient.updateDeviceFirmware(updateRequest);
            updateStatuses.add(updateResponse);
        }
        return updateStatuses;
    }

    /**
     * Helper method that collects all the devices that don't meet the approved version.
     */
    private List<String> collectUnsuccessfulUpdates(List<UpdateDeviceFirmwareResponse> updateStatuses) {
        List<String> unsuccessfulDevices = new ArrayList<>();
        for (UpdateDeviceFirmwareResponse updateStatus : updateStatuses) {
            if (!updateStatus.isWasSuccessful()) {
                unsuccessfulDevices.add(updateStatus.getDeviceId());
            }
        }
        return unsuccessfulDevices;
    }

}
